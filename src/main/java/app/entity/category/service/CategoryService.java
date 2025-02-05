package app.entity.category.service;

import app.entity.category.model.Category;
import app.entity.category.repository.CategoryRepository;
import app.exception.DomainException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(String name, String description) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new DomainException("Category with name [%s] already exists.".formatted(name));
        }

        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();

        return categoryRepository.save(category);
    }

    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new DomainException("Category with ID [%s] not found.".formatted(id)));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category updateCategory(UUID id, String name, String description) {
        Category category = getCategoryById(id);

        category.setName(name);
        category.setDescription(description);

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}
