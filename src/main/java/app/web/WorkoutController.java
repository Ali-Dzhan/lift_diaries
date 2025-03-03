package app.web;

import app.entity.category.model.Category;
import app.entity.category.repository.CategoryRepository;
import app.entity.exercise.model.Exercise;
import app.entity.exercise.service.ExerciseService;
import app.entity.progress.service.ProgressService;
import app.entity.user.model.User;
import app.entity.user.service.UserService;
import app.entity.workout.model.Workout;
import app.entity.workout.service.WorkoutService;
import app.security.AuthenticationMetadata;
import app.web.dto.CategoryDTO;
import app.web.dto.ExerciseDTO;
import app.web.dto.SelectedExercisesRequest;
import app.web.dto.WorkoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workout")
@Slf4j
public class WorkoutController {

    private final UserService userService;
    private final WorkoutService workoutService;
    private final ExerciseService exerciseService;
    private final CategoryRepository categoryRepository;
    private final ProgressService progressService;

    @Autowired
    public WorkoutController(UserService userService,
                             WorkoutService workoutService,
                             ExerciseService exerciseService,
                             CategoryRepository categoryRepository, ProgressService progressService) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.exerciseService = exerciseService;
        this.categoryRepository = categoryRepository;
        this.progressService = progressService;
    }

    @GetMapping
    public ModelAndView selectCategories( @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);

        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryDTO(category.getName(), category.getImageUrl()))
                .collect(Collectors.toList());

        ModelAndView modelAndView = new ModelAndView("workout");
        modelAndView.addObject("categories", categoryDTOs);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/exercise")
    @ResponseBody
    public List<ExerciseDTO> getExercisesByCategory(@RequestParam String categoryName) {
        List<Exercise> exercises = exerciseService.getExercisesByCategory(categoryName);
        return exercises.stream()
                .map(exercise -> new ExerciseDTO(
                        exercise.getId(),
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getGifUrl(),
                        exercise.getSets(),
                        exercise.getReps()))
                .collect(Collectors.toList());
    }

    @PostMapping("/selectExercises")
    public ResponseEntity<String> selectExercises(@RequestBody SelectedExercisesRequest request,
                                                  @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        if (request.getSelectedExercises() == null || request.getSelectedExercises().isEmpty()) {
            return ResponseEntity.badRequest().body("No exercises selected.");
        }

        UUID userId = authenticationMetadata.getUserId();
        exerciseService.storeUserSelectedExercises(userId, request.getSelectedExercises());

        return ResponseEntity.ok("Exercises saved successfully.");
    }

    @GetMapping("/startWorkout")
    public ModelAndView startWorkout(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);

        List<UUID> selectedExerciseIds = exerciseService.getUserSelectedExercises(userId);
        if (selectedExerciseIds.isEmpty()) {
            log.warn("No exercises found for user ID: {}", userId);
            return new ModelAndView("redirect:/workout");
        }

        List<Exercise> exercises = exerciseService.getExercisesByIds(selectedExerciseIds);
        UUID sessionId = UUID.randomUUID();
        exerciseService.storeWorkoutSessionId(userId, sessionId);

        ModelAndView modelAndView = new ModelAndView("startWorkout");
        modelAndView.addObject("sessionId", sessionId);
        modelAndView.addObject("exercises", exercises);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/saveWorkout")
    public ResponseEntity<String> saveWorkout(@RequestBody WorkoutRequest workoutRequest,
                                              @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);

        List<Exercise> exercises = exerciseService.getExercisesByIds(workoutRequest.getExerciseIds());
        Workout savedWorkout = workoutService.createWorkout(
                workoutRequest.getWorkoutName(),
                authenticationMetadata,
                exercises,
                workoutRequest.isCompleted()
        );
        workoutService.markWorkoutAsCompleted(savedWorkout.getId());

        if (workoutRequest.isCompleted()) {
            for (UUID exerciseId : workoutRequest.getExerciseIds()) {
                progressService.saveWorkoutCompletion(user.getId(), savedWorkout.getId(), exerciseId);
            }
        }

        return ResponseEntity.ok("Workout saved successfully with ID: " + savedWorkout.getId());
    }

    @GetMapping("/complete")
    public ModelAndView completeWorkout(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView("completed");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping("/updateWorkout")
    public String updateWorkout(@ModelAttribute("exercises") List<ExerciseDTO> exercises) {
        workoutService.updateWorkoutExercises(exercises);
        return "redirect:/workout";
    }
}

