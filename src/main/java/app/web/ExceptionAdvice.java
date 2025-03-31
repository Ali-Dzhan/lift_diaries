package app.web;

import app.exception.NotificationServiceFeignCallException;
import app.exception.UsernameAlreadyExistException;
import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@ControllerAdvice
public class ExceptionAdvice {

    private final UserService userService;

    @Autowired
    public ExceptionAdvice(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExist(HttpServletRequest request, RedirectAttributes redirectAttributes, UsernameAlreadyExistException exception) {
        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", message);
        return "redirect:/register";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions(Exception exception) {

        return new ModelAndView("not-found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());
        modelAndView.addObject("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleBadRequest(IllegalArgumentException exception) {

        ModelAndView modelAndView = new ModelAndView("bad-request-error");
        modelAndView.addObject("errorMessage", exception.getMessage());
        modelAndView.addObject("errorCode", HttpStatus.BAD_REQUEST.value());

        return modelAndView;
    }

    @ExceptionHandler(NotificationServiceFeignCallException.class)
    public ModelAndView handleNotificationFeignCallException(
            NotificationServiceFeignCallException exception,
            @AuthenticationPrincipal AuthenticationMetadata auth
    ) {
        NotificationPreference emptyPreference = new NotificationPreference();
        List<Notification> emptyHistory = List.of();

        User user = userService.getById(auth.getUserId());

        ModelAndView model = new ModelAndView("notifications");
        model.addObject("user", user);
        model.addObject("notificationPreference", emptyPreference);
        model.addObject("notificationHistory", emptyHistory);
        model.addObject("feignCallErrorMessage", exception.getMessage());
        return model;
    }
}
