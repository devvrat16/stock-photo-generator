package com.mockai.model;

import java.time.LocalDateTime;

public class GeneratedImage {

    private String id;
    private String prompt;
    private String filename;
    private String filePath;
    private String size;
    private String style;
    private String quality;
    private String provider;
    private boolean mockGenerated;
    private long fileSizeBytes;
    private LocalDateTime createdAt;

    public GeneratedImage() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final GeneratedImage image = new GeneratedImage();
        public Builder id(String id) { image.id = id; return this; }
        public Builder prompt(String prompt) { image.prompt = prompt; return this; }
        public Builder filename(String filename) { image.filename = filename; return this; }
        public Builder filePath(String filePath) { image.filePath = filePath; return this; }
        public Builder size(String size) { image.size = size; return this; }
        public Builder style(String style) { image.style = style; return this; }
        public Builder quality(String quality) { image.quality = quality; return this; }
        public Builder provider(String provider) { image.provider = provider; return this; }
        public Builder mockGenerated(boolean mockGenerated) { image.mockGenerated = mockGenerated; return this; }
        public Builder fileSizeBytes(long fileSizeBytes) { image.fileSizeBytes = fileSizeBytes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { image.createdAt = createdAt; return this; }
        public GeneratedImage build() { return image; }
    }

    public String getId() { return id; }
    public String getPrompt() { return prompt; }
    public String getFilename() { return filename; }
    public String getFilePath() { return filePath; }
    public String getSize() { return size; }
    public String getStyle() { return style; }
    public String getQuality() { return quality; }
    public String getProvider() { return provider; }
    public boolean isMockGenerated() { return mockGenerated; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
