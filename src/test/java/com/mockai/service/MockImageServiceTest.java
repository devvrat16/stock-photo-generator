package com.mockai.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MockImageServiceTest {

    private final MockImageService mockImageService = new MockImageService();

    @Test
    void generateMockImage_returnsValidPng() throws IOException {
        byte[] imageBytes = mockImageService.generateMockImage("Test prompt", 512, 512);

        assertNotNull(imageBytes);
        assertTrue(imageBytes.length > 0);

        // Verify PNG magic bytes
        assertEquals((byte) 0x89, imageBytes[0]);
        assertEquals((byte) 0x50, imageBytes[1]); // P
        assertEquals((byte) 0x4E, imageBytes[2]); // N
        assertEquals((byte) 0x47, imageBytes[3]); // G
    }

    @Test
    void generateMockImage_differentSizes() throws IOException {
        byte[] small = mockImageService.generateMockImage("Small", 256, 256);
        byte[] large = mockImageService.generateMockImage("Large", 1024, 1024);

        assertTrue(large.length > small.length);
    }

    @Test
    void generateMockImage_differentPrompts() throws IOException {
        byte[] img1 = mockImageService.generateMockImage("Ocean sunset", 512, 512);
        byte[] img2 = mockImageService.generateMockImage("Mountain peak", 512, 512);

        // Different prompts should produce different images (different random elements)
        assertNotEquals(img1.length, img2.length);
    }

    @Test
    void parseSize_validFormat() {
        int[] size = mockImageService.parseSize("1024x1024");
        assertEquals(1024, size[0]);
        assertEquals(1024, size[1]);
    }

    @Test
    void parseSize_landscapeFormat() {
        int[] size = mockImageService.parseSize("1792x1024");
        assertEquals(1792, size[0]);
        assertEquals(1024, size[1]);
    }

    @Test
    void parseSize_invalidFormat_returnsDefault() {
        int[] size = mockImageService.parseSize("invalid");
        assertEquals(1024, size[0]);
        assertEquals(1024, size[1]);
    }

    @Test
    void parseSize_emptyString_returnsDefault() {
        int[] size = mockImageService.parseSize("");
        assertEquals(1024, size[0]);
        assertEquals(1024, size[1]);
    }
}
