package app.web.mapper;

import app.entity.category.model.Category;
import app.entity.category.repository.CategoryRepository;
import app.entity.exercise.model.Exercise;
import app.entity.exercise.service.ExerciseService;
import app.web.dto.CategoryDTO;
import app.web.dto.ExerciseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workout")
public class WorkoutSessionController {

    private final ExerciseService exerciseService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public WorkoutSessionController(ExerciseService exerciseService, CategoryRepository categoryRepository) {
        this.exerciseService = exerciseService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ModelAndView selectCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryDTO(category.getName(), category.getImageUrl()))
                .collect(Collectors.toList());

        ModelAndView modelAndView = new ModelAndView("workout");
        modelAndView.addObject("categories", categoryDTOs);
        return modelAndView;
    }

    @GetMapping("/exercise")
    @ResponseBody
    public List<ExerciseDTO> getExercisesByCategory(@RequestParam String categoryName) {
        List<Exercise> exercises = exerciseService.getExercisesByCategory(categoryName);
        return exercises.stream()
                .map(exercise -> new ExerciseDTO(
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getGifUrl(),
                        exercise.getSets(),
                        exercise.getReps()))
                .collect(Collectors.toList());
    }

    @PostMapping("/start")
    public ModelAndView startWorkout(@RequestParam("selectedExercises") List<UUID> selectedExerciseIds) {
        List<Exercise> selectedExercises = exerciseService.getExercisesByIds(selectedExerciseIds);

        List<ExerciseDTO> exerciseDTOs = selectedExercises.stream()
                .map(exercise -> new ExerciseDTO(
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getGifUrl(),
                        exercise.getSets(),
                        exercise.getReps()
                ))
                .collect(Collectors.toList());

        ModelAndView modelAndView = new ModelAndView("workout/startWorkout");
        modelAndView.addObject("exercises", exerciseDTOs);
        return modelAndView;
    }

    @PostMapping("/complete")
    public ModelAndView completeWorkout() {
        return new ModelAndView("workout/completed");
    }
}

