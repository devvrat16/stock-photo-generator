package com.mockai.service;

import com.mockai.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);
    private final AppConfig appConfig;

    public ImageStorageService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public String saveImage(byte[] imageBytes, String filename) throws IOException {
        Path storagePath = Paths.get(appConfig.getStoragePath());
        Path filePath = storagePath.resolve(filename);
        Files.write(filePath, imageBytes);
        log.info("Image saved: {}", filePath.toAbsolutePath());
        return filePath.toString();
    }

    public java.util.Optional<byte[]> loadImage(String filename) {
        try {
            Path filePath = Paths.get(appConfig.getStoragePath(), filename);
            if (Files.exists(filePath)) {
                return java.util.Optional.of(Files.readAllBytes(filePath));
            }
        } catch (IOException e) {
            log.error("Failed to load image: {}", filename, e);
        }
        return java.util.Optional.empty();
    }

    public boolean deleteImage(String filename) {
        try {
            Path filePath = Paths.get(appConfig.getStoragePath(), filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete image: {}", filename, e);
            return false;
        }
    }

    public Path getStoragePath() {
        return Paths.get(appConfig.getStoragePath());
    }
}
