package app.user;

import app.exception.UsernameAlreadyExistException;
import app.notification.service.NotificationService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .username("testUser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    @Test
    void register_ShouldCreateUser_WhenUsernameDoesNotExist() {
        RegisterRequest request = new RegisterRequest("testUser", "password", "test@example.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User result = userService.register(request);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).save(any(User.class));
        verify(notificationService).saveNotificationPreference(any(UUID.class), eq(false), isNull());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("testUser", "password", "test@example.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(mockUser));

        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(request));

        verify(userRepository, never()).save(any(User.class));
    }

//    @Test
//    void editUserDetails_ShouldUpdateUser_WhenUserExists() {
//        UserEditRequest editRequest = new UserEditRequest("John", "Doe", "new@example.com", "profilePic.png");
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
//
//        userService.editUserDetails(userId, editRequest);
//
//        assertEquals("John", mockUser.getFirstName());
//        assertEquals("Doe", mockUser.getLastName());
//        assertEquals("new@example.com", mockUser.getEmail());
//        verify(userRepository).save(any(User.class));
//        verify(notificationService).saveNotificationPreference(userId, true, "new@example.com");
//    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        List<User> users = userService.getAllUsers();

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        verify(userRepository).findAll();
    }

    @Test
    void getById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        User result = userService.getById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void getById_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameAlreadyExistException.class, () -> userService.getById(userId));
    }

    @Test
    void switchStatus_ShouldToggleUserActiveStatus() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        userService.switchStatus(userId);

        assertFalse(mockUser.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void switchRole_ShouldToggleUserRole() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        userService.switchRole(userId);

        assertEquals(UserRole.ADMIN, mockUser.getRole());
        verify(userRepository).save(any(User.class));

        userService.switchRole(userId);

        assertEquals(UserRole.USER, mockUser.getRole());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        var userDetails = userService.loadUserByUsername("testUser");

        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        assertThrows(UsernameAlreadyExistException.class, () -> userService.loadUserByUsername("unknownUser"));
    }
}
