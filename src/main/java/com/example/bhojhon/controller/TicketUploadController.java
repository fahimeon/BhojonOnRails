package com.example.bhojhon.controller;

import com.example.bhojhon.data.DatabaseHelper;
import com.example.bhojhon.model.TicketInfo;
import com.example.bhojhon.util.BaseController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TicketUploadController extends BaseController {

    @FXML
    private ImageView ticketImageView;

    @FXML
    private Label placeholderLabel;

    @FXML
    private Button processButton;

    @FXML
    private VBox statusBox;

    @FXML
    private Label statusLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private TextArea debugArea;

    @FXML
    private VBox manualEntryBox;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField pnrField;
    @FXML
    private TextField trainNoField;
    @FXML
    private TextField seatField;
    @FXML
    private TextField dateField;

    private File selectedImageFile;
    private final DatabaseHelper dbHelper;
    private final com.example.bhojhon.service.OCRSpaceService ocrService;

    public TicketUploadController() {
        this.dbHelper = new DatabaseHelper();
        this.ocrService = new com.example.bhojhon.service.OCRSpaceService();
    }

    @Override
    public void initialize() {
        // Initialize if needed, e.g. reset UI
        placeholderLabel.setVisible(true);
        processButton.setDisable(true);
        errorLabel.setVisible(false);
        debugArea.setVisible(false);
        statusBox.setVisible(false);
        manualEntryBox.setVisible(false);
        manualEntryBox.setManaged(false);
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Ticket Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        // Try initial directory if possible
        File initialDir = new File(System.getProperty("user.home"));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File file = fileChooser.showOpenDialog(ticketImageView.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            Image image = new Image(file.toURI().toString());
            ticketImageView.setImage(image);
            placeholderLabel.setVisible(false);
            processButton.setDisable(false);
            errorLabel.setVisible(false);
            debugArea.setVisible(false);
            manualEntryBox.setVisible(false);
            manualEntryBox.setManaged(false);
        }
    }

    @FXML
    private void handleProcessTicket() {
        if (selectedImageFile == null)
            return;

        processButton.setDisable(true);
        statusBox.setVisible(true);
        statusLabel.setText("Scanning ticket... Please wait.");
        errorLabel.setVisible(false);
        manualEntryBox.setVisible(false);
        manualEntryBox.setManaged(false);

        // Run OCR in background thread
        new Thread(() -> {
            try {
                String extractedText = performOCR(selectedImageFile);

                Platform.runLater(() -> {
                    // statusLabel.setText("Extracting details...");
                    debugArea.setVisible(true);
                    debugArea.setText(extractedText); // Show raw text for debugging

                    TicketInfo ticketInfo = extractTicketInfo(extractedText);

                    // Populate UI
                    populateFields(ticketInfo);

                    statusLabel.setText("Scan Complete. Please verify details below.");
                    statusBox.setVisible(false);

                    // Show Form
                    manualEntryBox.setVisible(true);
                    manualEntryBox.setManaged(true);
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> {
                    processButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleConfirm() {
        TicketInfo info = new TicketInfo();
        info.setPassengerName(nameField.getText().trim());
        info.setPhoneNumber(phoneField.getText().trim());
        info.setPnr(pnrField.getText().trim());
        info.setTrainNumber(trainNoField.getText().trim());
        info.setSeatNumber(seatField.getText().trim());
        info.setJourneyDate(dateField.getText().trim());

        if (info.getPassengerName().isEmpty() || info.getPnr().isEmpty()) {
            showError("Name and PNR are required.");
            return;
        }

        if (saveToDatabase(info)) {
            goToFoodMenu();
        } else {
            showError("Failed to save ticket details to database.");
        }
    }

    private void populateFields(TicketInfo info) {
        nameField.setText(info.getPassengerName() != null ? info.getPassengerName() : "");
        phoneField.setText(info.getPhoneNumber() != null ? info.getPhoneNumber() : "");
        pnrField.setText(info.getPnr() != null ? info.getPnr() : "");
        trainNoField.setText(info.getTrainNumber() != null ? info.getTrainNumber() : "");
        seatField.setText(info.getSeatNumber() != null ? info.getSeatNumber() : "");
        dateField.setText(info.getJourneyDate() != null ? info.getJourneyDate() : "");
    }

    private String performOCR(File imageFile) throws Exception {
        // Check file size and compress if needed (Limit: 1MB for free API)
        File fileToScan = imageFile;
        // Check if > 1MB
        if (fileToScan.length() > 1024 * 1024) {
            Platform.runLater(() -> statusLabel.setText("Compressing image..."));
            fileToScan = compressImage(imageFile);
        }
        return ocrService.extractTextFromImage(fileToScan);
    }

    private File compressImage(File inputFile) throws java.io.IOException {
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(inputFile);

        // Resize logic: target width 1024px, maintain aspect ratio
        int targetWidth = 1024;
        if (image.getWidth() <= targetWidth)
            return inputFile; // No need to resize if small enough

        int targetHeight = (int) (image.getHeight() * ((double) targetWidth / image.getWidth()));

        java.awt.image.BufferedImage resized = new java.awt.image.BufferedImage(targetWidth, targetHeight,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        File tempFile = File.createTempFile("compressed_ticket", ".jpg");
        javax.imageio.ImageIO.write(resized, "jpg", tempFile);
        return tempFile;
    }

    private TicketInfo extractTicketInfo(String text) {
        TicketInfo info = new TicketInfo();

        // Clean text common noise
        // text = text.replaceAll("[^\\x00-\\x7F]", ""); // Remove non-ASCII if needed,
        // but Bengali names might exist?
        // Let's keep it simple for now.

        // Regex Patterns - Refined

        // Passenger Name: Look for "Name:" or "Passenger Name"
        // Also capture lines that look like names if label is missing (heuristic: all
        // caps, length > 3, no numbers)
        String name = extractValue(text, "(?i)(?:Name|Passenger)\\s*[:\\-]?\\s*([A-Za-z\\s\\.]{3,30})");
        if (name == null) {
            // Fallback: Look for lines with typical name structure
            // This is risky but helps with noisy OCR
        }
        info.setPassengerName(name);

        // Phone: "01xxxxxxxxx" or "+8801xxxxxxxxx"
        // More flexible: allow spaces/dashes
        String phoneObj = extractValue(text, "(?:\\+88)?\\s*(01\\d{9})"); // Simplest valid form
        if (phoneObj == null) {
            phoneObj = extractValue(text, "(?:\\+88)?\\s*(01\\d[\\d\\s-]{8,})"); // With separators
            if (phoneObj != null)
                phoneObj = phoneObj.replaceAll("[\\s-]", "");
        }
        info.setPhoneNumber(phoneObj);

        // PNR/Ticket
        String pnr = extractValue(text, "(?i)(?:PNR|Ticket|Number)\\s*[:\\-]?\\s*(\\d{8,14})");
        if (pnr == null) {
            // High confidence fallback: 10-14 digit number standing alone
            pnr = extractValue(text, "\\b(\\d{10,14})\\b");
        }
        info.setPnr(pnr);

        // Train Number/Name
        // Try to match specific number first
        String trainNo = extractValue(text, "(?i)(?:Train|Trn)\\s*[:\\-]?\\s*(?:No)?\\s*(\\d{3,5})");
        if (trainNo == null) {
            // Try to find known train names? (e.g. SONAR, PARABAT) - skipped for now
        }
        info.setTrainNumber(trainNo);

        // Seat
        // Look for "Seat", "Berth", or patterns like "KA-12", "S_CH-15"
        String seat = extractValue(text, "(?i)(?:Seat|Berth)\\s*[:\\-]?\\s*([A-Za-z]+[-_ ]?\\d+)");
        if (seat == null) {
            // Fallback pattern: [Letter]-[Number]
            seat = extractValue(text, "\\b([A-Za-z]{1,4}[-_]\\d{1,3})\\b");
        }
        info.setSeatNumber(seat);

        // Journey Date
        // dd-MM-yyyy, dd/MM/yyyy, dd.MM.yyyy
        info.setJourneyDate(extractValue(text, "(\\d{2}[-/.]\\d{2}[-/.]\\d{4})"));

        System.out.println("Extracted Info (JSON): "
                + new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(info));
        return info;
    }

    private String extractValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            // Group 1 is usually the value we want
            return matcher.group(matcher.groupCount() >= 1 ? 1 : 0).trim();
        }
        return null;
    }

    private boolean saveToDatabase(TicketInfo ticket) {
        // Validation now happens in handleConfirm
        return dbHelper.saveTicket(ticket);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
