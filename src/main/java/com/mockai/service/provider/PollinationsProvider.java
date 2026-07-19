package com.mockai.service.provider;

import com.mockai.model.ImageRequest;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Pollinations.ai — free image generation, no API key required.
 * Uses OkHttp with HTTP/2 and browser-like headers for Cloudflare compatibility.
 *
 * API: GET https://image.pollinations.ai/prompt/{encoded_prompt}?width=W&height=H&nologo=true
 * Returns: JPEG image bytes directly
 */
@Component
public class PollinationsProvider {

    private static final Logger log = LoggerFactory.getLogger(PollinationsProvider.class);

    private final OkHttpClient httpClient;

    public PollinationsProvider() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(90))
                .protocols(List.of(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .build();
    }

    /**
     * Generate an image using Pollinations.ai (free, no key needed).
     * @return image bytes (JPEG)
     */
    public byte[] generate(ImageRequest request) throws Exception {
        int[] dims = parseSize(request.getSize());
        String encodedPrompt = request.getPrompt()
                .replace(" ", "%20")
                .replace(",", "%2C");

        String url = String.format("https://image.pollinations.ai/prompt/%s?width=%d&height=%d&nologo=true",
                encodedPrompt, dims[0], dims[1]);

        log.info("Generating image with Pollinations.ai — prompt: \"{}\", size: {}x{}",
                request.getPrompt(), dims[0], dims[1]);

        Request httpRequest = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .header("Sec-Fetch-Dest", "image")
                .header("Sec-Fetch-Mode", "no-cors")
                .header("Sec-Fetch-Site", "cross-site")
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "no body";
                throw new RuntimeException("Pollinations returned HTTP " + response.code() + ": " +
                        body.substring(0, Math.min(200, body.length())));
            }

            byte[] imageBytes = response.body().bytes();
            if (imageBytes == null || imageBytes.length == 0) {
                throw new RuntimeException("Empty response from Pollinations.ai");
            }

            log.info("Pollinations.ai returned image: {} bytes", imageBytes.length);
            return imageBytes;
        }
    }

    public String getName() {
        return "pollinations";
    }

    public boolean isAvailable() {
        return true; // Always available — free, no API key
    }

    private int[] parseSize(String size) {
        try {
            String[] parts = size.split("x");
            int w = Integer.parseInt(parts[0]);
            int h = Integer.parseInt(parts[1]);
            // Pollinations supports max 768px
            if (w > 768) w = 768;
            if (h > 768) h = 768;
            return new int[]{w, h};
        } catch (Exception e) {
            return new int[]{512, 512};
        }
    }
}
