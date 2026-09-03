package com.topwatch.back_topwatch.config;

import com.topwatch.back_topwatch.domain.Category;
import com.topwatch.back_topwatch.domain.enums.Type;
import com.topwatch.back_topwatch.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds a starter set of categories per item Type on application startup, so
 * there is always something to pick from when creating items. Skipped if
 * categories already exist (e.g. if ddl-auto stops being create-drop).
 */
@Component
@RequiredArgsConstructor
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        categoryRepository.saveAll(List.of(
                category("Action", "Action movies", Type.MOVIES),
                category("Comedy", "Comedy movies", Type.MOVIES),
                category("Drama", "Drama movies", Type.MOVIES),
                category("Sci-Fi", "Science fiction movies", Type.MOVIES),
                category("Horror", "Horror movies", Type.MOVIES),

                category("Drama", "Drama series", Type.SERIES),
                category("Comedy", "Comedy series", Type.SERIES),
                category("Sci-Fi", "Science fiction series", Type.SERIES),
                category("Documentary", "Documentary series", Type.SERIES),

                category("Fantasy", "Fantasy books", Type.BOOKS),
                category("Mystery", "Mystery books", Type.BOOKS),
                category("Biography", "Biography books", Type.BOOKS),
                category("Non-Fiction", "Non-fiction books", Type.BOOKS),

                category("Rock", "Rock music", Type.MUSIC),
                category("Pop", "Pop music", Type.MUSIC),
                category("Jazz", "Jazz music", Type.MUSIC),
                category("Classical", "Classical music", Type.MUSIC)
        ));
    }

    private Category category(String name, String description, Type type) {
        return Category.builder()
                .name(name)
                .description(description)
                .type(type)
                .build();
    }

}
