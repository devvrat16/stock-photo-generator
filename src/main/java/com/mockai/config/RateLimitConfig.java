package com.mockai.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private final Map<String, RateLimitEntry> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 30;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor())
                .addPathPatterns("/api/**");
    }

    private class RateLimitInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
            String clientIp = request.getRemoteAddr();
            long now = System.currentTimeMillis();

            RateLimitEntry entry = requestCounts.compute(clientIp, (key, existing) -> {
                if (existing == null || now - existing.windowStart > 60000) {
                    return new RateLimitEntry(now);
                }
                return existing;
            });

            if (entry.counter.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                try {
                    response.getWriter().write("{\"error\": \"Rate limit exceeded. Max " +
                            MAX_REQUESTS_PER_MINUTE + " requests per minute.\"}");
                } catch (Exception e) {
                    // ignore
                }
                return false;
            }
            return true;
        }
    }

    private static class RateLimitEntry {
        final long windowStart;
        final AtomicInteger counter;

        RateLimitEntry(long windowStart) {
            this.windowStart = windowStart;
            this.counter = new AtomicInteger(0);
        }
    }
}
