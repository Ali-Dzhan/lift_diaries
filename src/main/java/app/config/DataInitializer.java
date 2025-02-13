package app.config;

import app.entity.category.model.Category;
import app.entity.category.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(List.of(
                        Category.builder().name("Chest").build(),
                        Category.builder().name("Back").build(),
                        Category.builder().name("Legs").build(),
                        Category.builder().name("Shoulders").build(),
                        Category.builder().name("Arms").build(),
                        Category.builder().name("Other").build()
                ));
            }
        };
    }
}
