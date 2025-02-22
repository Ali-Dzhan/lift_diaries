package app.web;

import app.entity.exercise.model.Exercise;
import app.entity.progress.model.Progress;
import app.entity.progress.service.ProgressService;
import app.entity.workout.model.Workout;
import app.entity.workout.service.WorkoutService;
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

    @Autowired
    public ProgressController(ProgressService progressService,
                              WorkoutService workoutService) {
        this.progressService = progressService;
        this.workoutService = workoutService;
    }

    @GetMapping
    public ModelAndView viewProgress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();

        List<Progress> progressList = progressService.getUserProgressSummary(userId);
        List<WorkoutProgress> workoutProgressList = progressList.stream()
                .collect(Collectors.groupingBy(Progress::getWorkout))
                .entrySet().stream()
                .map(entry -> new WorkoutProgress(entry.getKey(), entry.getValue()))
                .toList();

        long streak = progressService.calculateWorkoutStreak(userId);

        ModelAndView modelAndView = new ModelAndView("progress");
        modelAndView.addObject("workoutProgressList", workoutProgressList);
        modelAndView.addObject("streak", streak);

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

        return "redirect:/workout/startWorkout?workoutId=" + newWorkoutId;
    }
}
