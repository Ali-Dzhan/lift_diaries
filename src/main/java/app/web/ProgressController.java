package app.web;

import app.exercise.model.Exercise;
import app.exercise.service.ExerciseService;
import app.progress.model.Progress;
import app.progress.service.ProgressService;
import app.user.model.User;
import app.user.service.UserService;
import app.workout.model.Workout;
import app.workout.service.WorkoutService;
import app.security.AuthenticationMetadata;
import app.web.dto.WorkoutProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final WorkoutService workoutService;
    private final ExerciseService exerciseService;
    private final UserService userService;

    @Autowired
    public ProgressController(ProgressService progressService,
                              WorkoutService workoutService, ExerciseService exerciseService, UserService userService) {
        this.progressService = progressService;
        this.workoutService = workoutService;
        this.exerciseService = exerciseService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView viewProgress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);
        List<Progress> progressList = progressService.getUserProgressSummary(userId);

        List<WorkoutProgress> workoutProgressList = progressList.stream()
                .collect(Collectors.groupingBy(Progress::getWorkout))
                .entrySet().stream()
                .map(entry -> new WorkoutProgress(entry.getKey(), entry.getValue()))
                .sorted((w1, w2) -> w2.getLatestTimestamp().compareTo(w1.getLatestTimestamp()))
                .toList();

        long streak = progressService.calculateWorkoutStreak(userId);
        int totalWorkouts = workoutProgressList.size();

        ModelAndView modelAndView = new ModelAndView("progress");
        modelAndView.addObject("workoutProgressList", workoutProgressList);
        modelAndView.addObject("streak", streak);
        modelAndView.addObject("totalWorkouts", totalWorkouts);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/delete/{workoutId}")
    public String deleteWorkout(@PathVariable UUID workoutId) {
        workoutService.deleteWorkoutAndProgress(workoutId);
        return "redirect:/progress";
    }

    @PostMapping("/repeat/{workoutId}")
    public String repeatWorkout(@PathVariable UUID workoutId,
                                @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        Workout newWorkout = workoutService.repeatWorkout(workoutId, authenticationMetadata);
        UUID newWorkoutId = newWorkout.getId();

        List<UUID> selectedExerciseIds = newWorkout.getExercises()
                .stream()
                .map(Exercise::getId)
                .toList();
        exerciseService.storeUserSelectedExercises(authenticationMetadata.getUserId(), selectedExerciseIds);

        return "redirect:/workout/startWorkout?workoutId=" + newWorkoutId;
    }
}
