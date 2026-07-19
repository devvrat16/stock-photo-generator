package com.mockai.controller;

import com.mockai.model.ImageRequest;
import com.mockai.model.ImageResponse;
import com.mockai.service.ImageGenerationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);
    private final ImageGenerationService generationService;

    public ImageController(ImageGenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@Valid @RequestBody ImageRequest request) {
        try {
            log.info("Image generation requested: {}", request.getPrompt());
            ImageResponse response = generationService.generate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Image generation failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Image generation failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ImageResponse>> getAllImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ImageResponse> allImages = generationService.getAllImages();
        int start = page * size;
        int end = Math.min(start + size, allImages.size());
        List<ImageResponse> paged = start < allImages.size()
                ? allImages.subList(start, end)
                : List.of();
        return ResponseEntity.ok(paged);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImage(@PathVariable String id) {
        ImageResponse image = generationService.getImage(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(image);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getImageFile(@PathVariable String id) {
        byte[] imageBytes = generationService.getImageBytes(id);
        if (imageBytes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(Objects.requireNonNull(MediaType.IMAGE_PNG))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + id + ".png\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(imageBytes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ImageResponse>> searchImages(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ImageResponse> results = generationService.searchImages(q);
        int start = page * size;
        int end = Math.min(start + size, results.size());
        List<ImageResponse> paged = start < results.size()
                ? results.subList(start, end)
                : List.of();
        return ResponseEntity.ok(paged);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable String id) {
        boolean deleted = generationService.deleteImage(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
