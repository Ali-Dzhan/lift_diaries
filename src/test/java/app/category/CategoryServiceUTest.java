package app.category;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.category.service.CategoryService;
import app.progress.model.Progress;
import app.progress.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private CategoryService categoryService;

    private UUID userId;
    private Category category1;
    private Category category2;
    private Category category3;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        category1 = new Category();
        category1.setName("Chest");

        category2 = new Category();
        category2.setName("Back");

        category3 = new Category();
        category3.setName("Legs");
    }

    @Test
    void givenNoProgressHistory_whenGetNextMuscleGroup_thenReturnFirstCategory() {
        List<Category> allCategories = Arrays.asList(category1, category2, category3);
        when(progressService.getUserProgressSummary(userId)).thenReturn(Collections.emptyList());
        when(categoryRepository.findAll()).thenReturn(allCategories);

        Category result = categoryService.getNextMuscleGroup(userId);

        assertThat(result).isEqualTo(category1);
        verify(progressService, times(1)).getUserProgressSummary(userId);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void givenProgressHistory_whenGetNextMuscleGroup_thenReturnLeastTrainedCategory() {
        List<Category> allCategories = Arrays.asList(category1, category2, category3);
        when(categoryRepository.findAll()).thenReturn(allCategories);

        Progress progress1 = new Progress();
        progress1.setTimestamp(LocalDateTime.now().minusDays(2));
        progress1.setExercise(new app.exercise.model.Exercise());
        progress1.getExercise().setCategory(category1);

        Progress progress2 = new Progress();
        progress2.setTimestamp(LocalDateTime.now().minusDays(5));
        progress2.setExercise(new app.exercise.model.Exercise());
        progress2.getExercise().setCategory(category2);

        when(progressService.getUserProgressSummary(userId)).thenReturn(List.of(progress1, progress2));

        Category result = categoryService.getNextMuscleGroup(userId);

        assertThat(result).isEqualTo(category3);
        verify(progressService, times(1)).getUserProgressSummary(userId);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void givenAllCategoriesTrainedRecently_whenGetNextMuscleGroup_thenReturnLeastRecentlyTrainedCategory() {
        List<Category> allCategories = Arrays.asList(category1, category2, category3);
        when(categoryRepository.findAll()).thenReturn(allCategories);

        Progress progress1 = new Progress();
        progress1.setTimestamp(LocalDateTime.now().minusDays(1));
        progress1.setExercise(new app.exercise.model.Exercise());
        progress1.getExercise().setCategory(category1);

        Progress progress2 = new Progress();
        progress2.setTimestamp(LocalDateTime.now().minusDays(3));
        progress2.setExercise(new app.exercise.model.Exercise());
        progress2.getExercise().setCategory(category2);

        Progress progress3 = new Progress();
        progress3.setTimestamp(LocalDateTime.now().minusDays(2));
        progress3.setExercise(new app.exercise.model.Exercise());
        progress3.getExercise().setCategory(category3);

        when(progressService.getUserProgressSummary(userId)).thenReturn(List.of(progress1, progress2, progress3));

        Category result = categoryService.getNextMuscleGroup(userId);

        assertThat(result).isEqualTo(category2);
        verify(progressService, times(1)).getUserProgressSummary(userId);
        verify(categoryRepository, times(1)).findAll();
    }
}
