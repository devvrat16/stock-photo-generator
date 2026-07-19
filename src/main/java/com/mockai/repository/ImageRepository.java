package com.mockai.repository;

import com.mockai.model.GeneratedImage;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ImageRepository {

    private final Map<String, GeneratedImage> store = new ConcurrentHashMap<>();

    public GeneratedImage save(GeneratedImage image) {
        store.put(image.getId(), image);
        return image;
    }

    public Optional<GeneratedImage> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<GeneratedImage> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing((GeneratedImage img) -> img == null ? null : img.getCreatedAt(),
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    public List<GeneratedImage> search(String query) {
        String lowerQuery = query.toLowerCase();
        return store.values().stream()
                .filter(img -> img.getPrompt().toLowerCase().contains(lowerQuery))
                .sorted(Comparator.comparing((GeneratedImage img) -> img == null ? null : img.getCreatedAt(),
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    public Optional<GeneratedImage> delete(String id) {
        return Optional.ofNullable(store.remove(id));
    }

    public long count() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
