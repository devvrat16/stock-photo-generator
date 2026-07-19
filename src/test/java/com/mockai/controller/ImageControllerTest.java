package com.mockai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockai.model.ImageRequest;
import com.mockai.model.ImageResponse;
import com.mockai.repository.ImageRepository;
import com.mockai.service.ImageGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Objects;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImageGenerationService generationService;

    @Autowired
    private ImageRepository repository;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(repository).clear();
    }

    @Test
    void generate_validRequest_returnsOk() throws Exception {
        ImageRequest request = new ImageRequest("A beautiful sunset", "1024x1024", "vivid", "standard");

        mockMvc.perform(post("/api/images/generate")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompt").value("A beautiful sunset"))
                .andExpect(jsonPath("$.size").value("1024x1024"))
                .andExpect(jsonPath("$.mockGenerated").value(true))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.imageUrl").exists());
    }

    @Test
    void generate_emptyPrompt_returnsBadRequest() throws Exception {
        ImageRequest request = new ImageRequest("", "1024x1024", "vivid", "standard");

        mockMvc.perform(post("/api/images/generate")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_shortPrompt_returnsBadRequest() throws Exception {
        ImageRequest request = new ImageRequest("ab", "1024x1024", "vivid", "standard");

        mockMvc.perform(post("/api/images/generate")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllImages_returnsList() throws Exception {
        // Generate an image first
        generationService.generate(new ImageRequest("Test gallery", "1024x1024", "vivid", "standard"));

        mockMvc.perform(get("/api/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getImage_existingId_returnsOk() throws Exception {
        ImageResponse response = generationService.generate(
                new ImageRequest("Find me", "1024x1024", "vivid", "standard"));

        mockMvc.perform(get("/api/images/" + response.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompt").value("Find me"));
    }

    @Test
    void getImage_nonExistentId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/images/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImageFile_existingId_returnsImage() throws Exception {
        ImageResponse response = generationService.generate(
                new ImageRequest("Image file test", "1024x1024", "vivid", "standard"));

        mockMvc.perform(get("/api/images/" + response.getId() + "/file"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));
    }

    @Test
    void searchImages_findsMatching() throws Exception {
        generationService.generate(new ImageRequest("Dog in park", "1024x1024", "vivid", "standard"));
        generationService.generate(new ImageRequest("Cat on sofa", "1024x1024", "vivid", "standard"));

        mockMvc.perform(get("/api/images/search").param("q", "dog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].prompt").value("Dog in park"));
    }

    @Test
    void deleteImage_existingId_returnsOk() throws Exception {
        ImageResponse response = generationService.generate(
                new ImageRequest("Delete me", "1024x1024", "vivid", "standard"));

        mockMvc.perform(delete("/api/images/" + response.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image deleted successfully"));
    }

    @Test
    void deleteImage_nonExistentId_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/images/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generate_differentSizes() throws Exception {
        String[] sizes = {"512x512", "1024x1024", "1792x1024", "1024x1792"};

        for (String size : sizes) {
            ImageRequest request = new ImageRequest("Size test " + size, size, "vivid", "standard");

            mockMvc.perform(post("/api/images/generate")
                            .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                            .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(size));
        }
    }
}
