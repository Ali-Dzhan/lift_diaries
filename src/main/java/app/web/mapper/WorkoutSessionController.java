package app.web.mapper;

import app.entity.category.model.Category;
import app.entity.category.repository.CategoryRepository;
import app.entity.exercise.model.Exercise;
import app.entity.exercise.service.ExerciseService;
import app.entity.user.model.User;
import app.entity.user.service.UserService;
import app.entity.workout.model.Workout;
import app.entity.workout.service.WorkoutService;
import app.web.dto.CategoryDTO;
import app.web.dto.ExerciseDTO;
import app.web.dto.SelectedExercisesRequest;
import app.web.dto.WorkoutRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workout")
public class WorkoutSessionController {

    private final UserService userService;
    private final WorkoutService workoutService;
    private final ExerciseService exerciseService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public WorkoutSessionController(UserService userService, WorkoutService workoutService, ExerciseService exerciseService, CategoryRepository categoryRepository) {
        this.userService = userService;
        this.workoutService = workoutService;
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
                        exercise.getId(),
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getGifUrl(),
                        exercise.getSets(),
                        exercise.getReps()))
                .collect(Collectors.toList());
    }

    @PostMapping("/selectExercises")
    public ResponseEntity<String> selectExercises(@RequestBody SelectedExercisesRequest request, HttpSession session) {
        if (request.getSelectedExercises() == null || request.getSelectedExercises().isEmpty()) {
            return ResponseEntity.badRequest().body("No exercises selected.");
        }
        session.setAttribute("selectedExercises", request.getSelectedExercises());
        return ResponseEntity.ok("Exercises saved successfully.");
    }

    @GetMapping("/startWorkout")
    public ModelAndView startWorkout(HttpSession session) {
        Object attribute = session.getAttribute("selectedExercises");
        if (!(attribute instanceof List<?> rawList)) {
            return new ModelAndView("redirect:/workout");
        }
        List<UUID> selectedExerciseIds = rawList.stream()
                .filter(item -> item instanceof UUID)
                .map(UUID.class::cast)
                .toList();

        List<Exercise> exercises = exerciseService.getExercisesByIds(selectedExerciseIds);
        if (exercises == null || exercises.isEmpty()) {
            return new ModelAndView("redirect:/workout");
        }

        UUID sessionId = UUID.randomUUID();
        session.setAttribute("workoutSessionId", sessionId);

        ModelAndView modelAndView = new ModelAndView("startWorkout");
        modelAndView.addObject("sessionId", sessionId);
        modelAndView.addObject("exercises", exercises);

        return modelAndView;
    }

    @PostMapping("/saveWorkout")
    public ResponseEntity<String> saveWorkout(@RequestBody WorkoutRequest workoutRequest) {
        System.out.println("DEBUG: Received workout save request - Name: " + workoutRequest.getWorkoutName());

        if (workoutRequest.getExerciseIds() == null || workoutRequest.getExerciseIds().isEmpty()) {
            System.out.println("ERROR: No exercise IDs provided!");
            return ResponseEntity.badRequest().body("No exercise IDs provided.");
        }

        for (UUID id : workoutRequest.getExerciseIds()) {
            if (id == null) {
                System.out.println("ERROR: Found null exercise ID!");
                return ResponseEntity.badRequest().body("Null exercise ID detected.");
            }
        }

        User user = userService.getById(workoutRequest.getUserId());
        if (user == null) {
            System.out.println("DEBUG: User not found.");
            return ResponseEntity.badRequest().body("User not found.");
        }

        System.out.println("DEBUG: Fetching exercises for IDs: " + workoutRequest.getExerciseIds());

        List<Exercise> exercises = exerciseService.getExercisesByIds(workoutRequest.getExerciseIds());

        if (exercises.isEmpty()) {
            System.out.println("DEBUG: No exercises found for provided IDs.");
            return ResponseEntity.badRequest().body("No exercises found.");
        }

        System.out.println("DEBUG: Found " + exercises.size() + " exercises, creating workout...");

        Workout savedWorkout = workoutService.createWorkout(
                workoutRequest.getWorkoutName(),
                user.getId(),
                exercises);

        savedWorkout.setCompleted(true);

        return ResponseEntity.ok("Workout saved successfully with ID: " + savedWorkout.getId());
    }


    @GetMapping("/complete")
    public ModelAndView completeWorkout() {
        return new ModelAndView("workout/completed");
    }

    @PostMapping("/updateWorkout")
    public String updateWorkout(@ModelAttribute("exercises") List<ExerciseDTO> exercises) {
        workoutService.updateWorkoutExercises(exercises);
        return "redirect:/workout";
    }
}

