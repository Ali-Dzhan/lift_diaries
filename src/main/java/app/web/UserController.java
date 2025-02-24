package app.web;

import app.entity.user.model.User;
import app.entity.user.service.UserService;
import app.security.AuthenticationMetadata;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getCurrent")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        if (authenticationMetadata == null) {
            return Map.of("authenticated", false);
        }

        return Map.of(
                "authenticated", true,
                "id", authenticationMetadata.getUserId()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsers(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getProfileMenu(@PathVariable UUID id) {

        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));
        return modelAndView;
    }

    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id,
                                          @Valid UserEditRequest userEditRequest,
                                          BindingResult bindingResult) {
        User user = userService.getById(id);
        ModelAndView modelAndView = new ModelAndView("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", userEditRequest);

        if (bindingResult.hasErrors()) {
            return modelAndView;
        }

        userService.editUserDetails(id, userEditRequest);
        modelAndView.addObject("user", userService.getById(id));
        modelAndView.addObject("successMessage", "Profile updated successfully!");

        return modelAndView;
    }

    @PutMapping("/{id}/status")
    public String switchUserStatus(@PathVariable UUID id) {

        userService.switchStatus(id);

        return "redirect:/users";
    }

    @PutMapping("/{id}/role")
    public String switchUserRole(@PathVariable UUID id) {

        userService.switchRole(id);

        return "redirect:/users";
    }

}
