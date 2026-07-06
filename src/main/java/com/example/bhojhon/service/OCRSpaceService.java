package com.example.bhojhon.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

public class OCRSpaceService {

    private static final String API_KEY = "K85738659188957";
    private static final String API_URL = "https://api.ocr.space/parse/image";

    private final HttpClient httpClient;
    private final Gson gson;

    public OCRSpaceService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.gson = new Gson();
    }

    public String extractTextFromImage(File imageFile) throws IOException, InterruptedException {
        String boundary = "---boundary" + System.currentTimeMillis();
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

        // Build Multipart Body
        StringBuilder header = new StringBuilder();
        header.append("--").append(boundary).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"apikey\"\r\n\r\n");
        header.append(API_KEY).append("\r\n");

        header.append("--").append(boundary).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
        header.append("eng").append("\r\n");

        header.append("--").append(boundary).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"isOverlayRequired\"\r\n\r\n");
        header.append("false").append("\r\n");

        header.append("--").append(boundary).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"OCREngine\"\r\n\r\n");
        header.append("2").append("\r\n"); // Engine 2 is better for numbers/special chars

        header.append("--").append(boundary).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + imageFile.getName() + "\"\r\n");
        header.append("Content-Type: application/octet-stream\r\n\r\n");

        byte[] headerBytes = header.toString().getBytes();
        byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes();

        // Combine bytes
        byte[] body = new byte[headerBytes.length + imageBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(imageBytes, 0, body, headerBytes.length, imageBytes.length);
        System.arraycopy(footerBytes, 0, body, headerBytes.length + imageBytes.length, footerBytes.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API Request failed with status code: " + response.statusCode());
        }

        return parseResponse(response.body());
    }

    private String parseResponse(String jsonResponse) throws IOException {
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

        if (jsonObject.has("IsErroredOnProcessing") && jsonObject.get("IsErroredOnProcessing").getAsBoolean()) {
            String errorMessage = "Unknown API Error";
            if (jsonObject.has("ErrorMessage")) {
                JsonArray errors = jsonObject.getAsJsonArray("ErrorMessage");
                if (errors.size() > 0) {
                    errorMessage = errors.get(0).getAsString();
                }
            }
            throw new IOException("OCR API Error: " + errorMessage);
        }

        if (jsonObject.has("ParsedResults")) {
            JsonArray results = jsonObject.getAsJsonArray("ParsedResults");
            if (results.size() > 0) {
                JsonObject result = results.get(0).getAsJsonObject();
                if (result.has("ParsedText")) {
                    return result.get("ParsedText").getAsString();
                }
            }
        }

        return "";
    }
}
