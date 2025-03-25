package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUnauthenticatedRequestToGetCurrentUser_shouldReturnFalse() throws Exception {

        MockHttpServletRequestBuilder request = get("/users/getCurrent");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void getAuthenticatedRequestToGetCurrentUser_shouldReturnTrueAndId() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata
                (userId, "User123", "pass", UserRole.USER, true);

        MockHttpServletRequestBuilder request = get("/users/getCurrent")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void getAllUsers_asAdmin_shouldReturnUsersViewWithModel() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata
                (userId, "admin", "12345678", UserRole.ADMIN, true);

        User user = User.builder().id(userId).username("admin").build();
        when(userService.getById(userId)).thenReturn(user);
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users", "user"));
    }

    @Test
    void getUserProfile_shouldReturnProfileView() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("testUser").role(UserRole.USER).build();

        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{id}/profile", userId)
                        .with(user(new AuthenticationMetadata(
                                userId, "testUser", "12345678", UserRole.USER, true))))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "userEditRequest"));
    }

    @Test
    void putUpdateUserProfile_withValidData_shouldUpdateAndReturnSuccessMessage() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("testUser").role(UserRole.USER).build();

        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(put("/users/{id}/profile", userId)
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john.doe@mail.com")
                        .param("profilePicture", "https://example.com/avatar.jpg")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(
                                userId, "testUser", "12345678", UserRole.USER, true))))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "userEditRequest", "successMessage"));

        verify(userService).editUserDetails(eq(userId), any());
    }

    @Test
    void putUnauthorizedRequestToSwitchStatus_shouldReturn404AndNotFoundView() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata
                (UUID.randomUUID(), "User123", "12345678", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/status", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
    }

    @Test
    void putAuthorizedRequestToSwitchStatus_shouldRedirectToUsers() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata
                (UUID.randomUUID(), "AdminUser", "pass", UserRole.ADMIN, true);
        UUID targetId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = put("/users/{id}/status", targetId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).switchStatus(targetId);
    }

    @Test
    void putAuthorizedRequestToSwitchRole_shouldRedirectToUsers() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata
                (UUID.randomUUID(), "AdminUser", "pass", UserRole.ADMIN, true);
        UUID targetId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = put("/users/{id}/role", targetId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).switchRole(targetId);
    }
}
