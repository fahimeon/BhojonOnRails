package com.example.bhojhon.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUtil {
    private static final String UPLOAD_DIR = "uploads/menu-items";

    /**
     * Copies a file to the uploads directory and returns the absolute URI.
     */
    public static String uploadImage(File sourceFile) throws IOException {
        if (sourceFile == null)
            return null;

        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String extension = "";
        String fileName = sourceFile.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i);
        }

        String newFileName = UUID.randomUUID().toString() + extension;
        File destFile = new File(uploadFolder, newFileName);

        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return destFile.toURI().toString();
    }
}
