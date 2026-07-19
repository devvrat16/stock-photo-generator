package com.mockai.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ImageRequest {

    @NotBlank(message = "Prompt is required")
    @Size(min = 3, max = 500, message = "Prompt must be between 3 and 500 characters")
    private String prompt;
    private String size = "1024x1024";
    private String style = "vivid";
    private String quality = "standard";

    public ImageRequest() {}

    public ImageRequest(String prompt, String size, String style, String quality) {
        this.prompt = prompt;
        this.size = size;
        this.style = style;
        this.quality = quality;
    }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
}
