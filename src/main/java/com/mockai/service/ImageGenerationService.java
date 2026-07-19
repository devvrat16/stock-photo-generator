package com.mockai.service;

import com.mockai.model.GeneratedImage;
import com.mockai.model.ImageRequest;
import com.mockai.model.ImageResponse;
import com.mockai.repository.ImageRepository;
import com.mockai.service.provider.PollinationsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ImageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ImageGenerationService.class);

    private final MockImageService mockImageService;
    private final ImageStorageService storageService;
    private final ImageRepository repository;
    private final PollinationsProvider pollinationsProvider;

    public ImageGenerationService(MockImageService mockImageService,
                                  ImageStorageService storageService,
                                  ImageRepository repository,
                                  PollinationsProvider pollinationsProvider) {
        this.mockImageService = mockImageService;
        this.storageService = storageService;
        this.repository = repository;
        this.pollinationsProvider = pollinationsProvider;
    }

    /**
     * Generate an image using Pollinations.ai (free).
     * Falls back to mock generation if Pollinations fails.
     */
    public ImageResponse generate(ImageRequest request) throws IOException {
        String id = UUID.randomUUID().toString();
        String filename = id + ".png";
        byte[] imageBytes;
        String usedProvider;

        try {
            log.info("Generating image with Pollinations.ai — prompt: \"{}\"", request.getPrompt());
            imageBytes = pollinationsProvider.generate(request);
            usedProvider = "pollinations";
            log.info("Successfully generated image: {} bytes", imageBytes.length);
        } catch (Exception e) {
            log.warn("Pollinations failed: {}. Using mock.", e.getMessage());
            imageBytes = generateMock(request);
            usedProvider = "mock";
        }

        storageService.saveImage(imageBytes, filename);

        GeneratedImage image = GeneratedImage.builder()
                .id(id)
                .prompt(request.getPrompt())
                .filename(filename)
                .filePath(storageService.getStoragePath().resolve(filename).toString())
                .size(request.getSize())
                .style(request.getStyle())
                .quality(request.getQuality())
                .provider(usedProvider)
                .mockGenerated(false)
                .fileSizeBytes(imageBytes.length)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(image);
        log.info("Image saved: id={}, provider={}", id, usedProvider);

        return toResponse(image);
    }

    public ImageResponse getImage(String id) {
        return repository.findById(id).map(this::toResponse).orElse(null);
    }

    public List<ImageResponse> getAllImages() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ImageResponse> searchImages(String query) {
        return repository.search(query).stream().map(this::toResponse).toList();
    }

    public boolean deleteImage(String id) {
        return repository.findById(id).map(image -> {
            storageService.deleteImage(image.getFilename());
            repository.delete(id);
            return true;
        }).orElse(false);
    }

    public byte[] getImageBytes(String id) {
        return repository.findById(id)
                .flatMap(image -> storageService.loadImage(image.getFilename()))
                .orElse(null);
    }

    private byte[] generateMock(ImageRequest request) throws IOException {
        int[] dims = mockImageService.parseSize(request.getSize());
        return mockImageService.generateMockImage(request.getPrompt(), dims[0], dims[1]);
    }

    private ImageResponse toResponse(GeneratedImage image) {
        return ImageResponse.builder()
                .id(image.getId())
                .prompt(image.getPrompt())
                .filename(image.getFilename())
                .imageUrl("/api/images/" + image.getId() + "/file")
                .size(image.getSize())
                .style(image.getStyle())
                .provider(image.getProvider())
                .mockGenerated(image.isMockGenerated())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
