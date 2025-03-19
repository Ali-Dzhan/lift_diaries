package app.category.service;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.progress.model.Progress;
import app.progress.service.ProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProgressService progressService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,
                           ProgressService progressService) {
        this.categoryRepository = categoryRepository;
        this.progressService = progressService;
    }

    public Category getNextMuscleGroup(UUID userId) {
        List<Progress> progressHistory = progressService.getUserProgressSummary(userId);
        List<Category> allCategories = categoryRepository.findAll();

        if (progressHistory.isEmpty()) {
            return allCategories.get(0);
        }

        Map<String, LocalDateTime> lastTrainedMap = new HashMap<>();

        for (Progress progress : progressHistory) {
            String muscleGroup = progress.getExercise().getCategory().getName();
            LocalDateTime workoutDate = progress.getTimestamp();
            lastTrainedMap.put(muscleGroup, workoutDate);
        }

        return allCategories.stream()
                .min(Comparator.comparing(category -> lastTrainedMap.getOrDefault(category.getName(), LocalDateTime.MIN)))
                .orElse(allCategories.get(0));
    }
}
