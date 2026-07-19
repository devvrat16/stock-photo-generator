package com.mockai.service;

import com.mockai.config.AppConfig;
import com.mockai.model.ImageRequest;
import com.mockai.model.ImageResponse;
import com.mockai.repository.ImageRepository;
import com.mockai.service.provider.PollinationsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageGenerationServiceTest {

    private ImageGenerationService service;
    private ImageRepository repository;
    private MockImageService mockImageService;
    private ImageStorageService storageService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        repository = new ImageRepository();
        mockImageService = new MockImageService();
        tempDir = Files.createTempDirectory("mockai-test");
        storageService = new ImageStorageService(new TestAppConfig(tempDir.toString()));
        PollinationsProvider pollinationsProvider = new PollinationsProvider();
        service = new ImageGenerationService(mockImageService, storageService, repository, pollinationsProvider);
    }

    @Test
    void generate_createsImage() throws IOException {
        ImageRequest request = new ImageRequest("A beautiful sunset", "1024x1024", "vivid", "standard");

        ImageResponse response = service.generate(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("A beautiful sunset", response.getPrompt());
        assertEquals("1024x1024", response.getSize());
        assertEquals("vivid", response.getStyle());
        assertNotNull(response.getProvider());
        assertNotNull(response.getCreatedAt());
        assertTrue(response.getImageUrl().startsWith("/api/images/"));
    }

    @Test
    void generate_storesImageMetadata() throws IOException {
        ImageRequest request = new ImageRequest("Test image", "512x512", "natural", "standard");

        ImageResponse response = service.generate(request);

        ImageResponse retrieved = service.getImage(response.getId());
        assertNotNull(retrieved);
        assertEquals("Test image", retrieved.getPrompt());
    }

    @Test
    void generate_createsFileOnDisk() throws IOException {
        ImageRequest request = new ImageRequest("File test", "1024x1024", "vivid", "standard");

        ImageResponse response = service.generate(request);

        byte[] imageBytes = service.getImageBytes(response.getId());
        assertNotNull(imageBytes);
        assertTrue(imageBytes.length > 0);
    }

    @Test
    void getAllImages_returnsAllImages() throws IOException {
        service.generate(new ImageRequest("Image 1", "1024x1024", "vivid", "standard"));
        service.generate(new ImageRequest("Image 2", "1024x1024", "natural", "standard"));
        service.generate(new ImageRequest("Image 3", "512x512", "vivid", "hd"));

        var allImages = service.getAllImages();
        assertEquals(3, allImages.size());
    }

    @Test
    void searchImages_findsByPrompt() throws IOException {
        service.generate(new ImageRequest("Golden retriever in park", "1024x1024", "vivid", "standard"));
        service.generate(new ImageRequest("Mountain landscape", "1024x1024", "vivid", "standard"));
        service.generate(new ImageRequest("Golden sunset over ocean", "1024x1024", "vivid", "standard"));

        var results = service.searchImages("golden");
        assertEquals(2, results.size());
    }

    @Test
    void searchImages_noResults() throws IOException {
        service.generate(new ImageRequest("Blue sky", "1024x1024", "vivid", "standard"));

        var results = service.searchImages("dragon");
        assertEquals(0, results.size());
    }

    @Test
    void deleteImage_removesImage() throws IOException {
        ImageResponse response = service.generate(new ImageRequest("To delete", "1024x1024", "vivid", "standard"));

        boolean deleted = service.deleteImage(response.getId());

        assertTrue(deleted);
        assertNull(service.getImage(response.getId()));
    }

    @Test
    void deleteImage_nonExistent_returnsFalse() {
        boolean deleted = service.deleteImage("non-existent-id");
        assertFalse(deleted);
    }

    @Test
    void getImage_nonExistent_returnsNull() {
        ImageResponse result = service.getImage("non-existent-id");
        assertNull(result);
    }

    @Test
    void getImageBytes_nonExistent_returnsNull() {
        byte[] result = service.getImageBytes("non-existent-id");
        assertNull(result);
    }

    static class TestAppConfig extends AppConfig {
        private final String path;

        TestAppConfig(String path) {
            this.path = path;
        }

        @Override
        public String getStoragePath() {
            return path;
        }
    }
}
