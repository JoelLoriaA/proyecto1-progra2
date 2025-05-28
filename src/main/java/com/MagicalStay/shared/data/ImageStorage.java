package com.MagicalStay.shared.data;

import java.io.*;
import java.nio.file.*;

public class ImageStorage {
    private static final String IMAGE_DIRECTORY = "server/images/";

    public ImageStorage() {
        try {
            Files.createDirectories(Paths.get(IMAGE_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Error al crear directorio de imágenes: " + e.getMessage());
        }
    }

    public String saveImage(byte[] imageData, String fileName) throws IOException {
        String fullPath = IMAGE_DIRECTORY + fileName;
        Files.write(Paths.get(fullPath), imageData);
        return fileName;
    }

    public byte[] loadImage(String fileName) throws IOException {
        String fullPath = IMAGE_DIRECTORY + fileName;
        return Files.readAllBytes(Paths.get(fullPath));
    }

    public void deleteImage(String fileName) throws IOException {
        String fullPath = IMAGE_DIRECTORY + fileName;
        Files.deleteIfExists(Paths.get(fullPath));
    }
}