package com.mockai.model;

import java.time.LocalDateTime;

public class ImageResponse {

    private String id;
    private String prompt;
    private String filename;
    private String imageUrl;
    private String size;
    private String style;
    private String provider;       // which AI provider generated this
    private boolean mockGenerated; // true if fell back to mock
    private LocalDateTime createdAt;

    public ImageResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ImageResponse response = new ImageResponse();
        public Builder id(String id) { response.id = id; return this; }
        public Builder prompt(String prompt) { response.prompt = prompt; return this; }
        public Builder filename(String filename) { response.filename = filename; return this; }
        public Builder imageUrl(String imageUrl) { response.imageUrl = imageUrl; return this; }
        public Builder size(String size) { response.size = size; return this; }
        public Builder style(String style) { response.style = style; return this; }
        public Builder provider(String provider) { response.provider = provider; return this; }
        public Builder mockGenerated(boolean mockGenerated) { response.mockGenerated = mockGenerated; return this; }
        public Builder createdAt(LocalDateTime createdAt) { response.createdAt = createdAt; return this; }
        public ImageResponse build() { return response; }
    }

    public String getId() { return id; }
    public String getPrompt() { return prompt; }
    public String getFilename() { return filename; }
    public String getImageUrl() { return imageUrl; }
    public String getSize() { return size; }
    public String getStyle() { return style; }
    public String getProvider() { return provider; }
    public boolean isMockGenerated() { return mockGenerated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
