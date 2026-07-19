package com.mockai.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AppConfig {

    @Value("${app.image.storage-path:./generated-images}")
    private String storagePath;

    @PostConstruct
    public void init() throws IOException {
        Path path = Paths.get(storagePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public String getStoragePath() {
        return storagePath;
    }
}
