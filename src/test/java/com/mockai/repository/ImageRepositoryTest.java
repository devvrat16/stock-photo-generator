package com.mockai.repository;

import com.mockai.model.GeneratedImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ImageRepositoryTest {

    private ImageRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ImageRepository();
    }

    @Test
    void saveAndFindById_roundTripsImage() {
        GeneratedImage image = GeneratedImage.builder()
                .id("img-1")
                .prompt("sunset")
                .filename("img-1.png")
                .filePath("/tmp/img-1.png")
                .size("1024x1024")
                .style("vivid")
                .quality("standard")
                .mockGenerated(true)
                .fileSizeBytes(123)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(image);

        Optional<GeneratedImage> found = repository.findById("img-1");

        assertTrue(found.isPresent());
        assertEquals("img-1", found.get().getId());
        assertEquals("sunset", found.get().getPrompt());
    }

    @Test
    void findAll_returnsImagesSortedByNewestFirst() {
        GeneratedImage older = imageWithIdAndTime("img-1", LocalDateTime.of(2024, 1, 1, 0, 0));
        GeneratedImage newer = imageWithIdAndTime("img-2", LocalDateTime.of(2024, 1, 2, 0, 0));

        repository.save(older);
        repository.save(newer);

        List<GeneratedImage> all = repository.findAll();

        assertEquals(List.of("img-2", "img-1"), all.stream().map(GeneratedImage::getId).toList());
    }

    @Test
    void search_returnsMatchingPromptsCaseInsensitively() {
        repository.save(imageWithIdAndTime("img-1", LocalDateTime.of(2024, 1, 1, 0, 0)));
        repository.save(imageWithIdAndTime("img-2", LocalDateTime.of(2024, 1, 2, 0, 0)));

        List<GeneratedImage> results = repository.search("sun");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(image -> image.getPrompt().toLowerCase().contains("sun")));
    }

    @Test
    void delete_removesImageAndReturnsIt() {
        GeneratedImage image = imageWithIdAndTime("img-1", LocalDateTime.of(2024, 1, 1, 0, 0));
        repository.save(image);

        Optional<GeneratedImage> deleted = repository.delete("img-1");

        assertTrue(deleted.isPresent());
        assertEquals("img-1", deleted.get().getId());
        assertTrue(repository.findById("img-1").isEmpty());
    }

    @Test
    void countAndClear_manageStoreSize() {
        repository.save(imageWithIdAndTime("img-1", LocalDateTime.now()));
        repository.save(imageWithIdAndTime("img-2", LocalDateTime.now()));

        assertEquals(2, repository.count());

        repository.clear();

        assertEquals(0, repository.count());
    }

    private GeneratedImage imageWithIdAndTime(String id, LocalDateTime createdAt) {
        return GeneratedImage.builder()
                .id(id)
                .prompt("sunset over ocean")
                .filename(id + ".png")
                .filePath("/tmp/" + id + ".png")
                .size("1024x1024")
                .style("natural")
                .quality("standard")
                .mockGenerated(true)
                .fileSizeBytes(100)
                .createdAt(createdAt)
                .build();
    }
}
