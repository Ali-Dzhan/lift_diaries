package app.user;

<<<<<<< HEAD
import app.exception.DomainException;
=======
>>>>>>> d383567 (Add banner image and update README)
import app.exception.UsernameAlreadyExistException;
import app.notification.service.NotificationService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
<<<<<<< HEAD
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
=======
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

>>>>>>> d383567 (Add banner image and update README)
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUTest {

    @Mock
<<<<<<< HEAD
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
=======
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;
>>>>>>> d383567 (Add banner image and update README)

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

<<<<<<< HEAD
    @ParameterizedTest
    @MethodSource("provideRoleSwitchCases")
    void switchRole_ShouldToggleBetweenUserAndAdmin(UserRole initialRole, UserRole expectedRole) {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(initialRole)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertEquals(expectedRole, user.getRole());
        verify(userRepository).save(user);
    }

    private static Stream<Arguments> provideRoleSwitchCases() {
        return Stream.of(
                Arguments.of(UserRole.USER, UserRole.ADMIN),
                Arguments.of(UserRole.ADMIN, UserRole.USER)
        );
    }

    @Test
    void switchStatus_ShouldSetActiveToFalse_IfInitiallyTrue() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isActive(true)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchStatus(userId);

        // Then
        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void switchStatus_ShouldSetActiveToTrue_IfInitiallyFalse() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isActive(false)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchStatus(userId);

        // Then
        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest("test@example.com", "testUser", "12345678");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).saveNotificationPreference(any(UUID.class), anyBoolean(), anyString());
=======
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
>>>>>>> d383567 (Add banner image and update README)
    }

    @Test
    void register_ShouldCreateUser_WhenUsernameDoesNotExist() {
<<<<<<< HEAD
        // Given
        RegisterRequest request = new RegisterRequest("new@example.com", "newUser", "12345678");
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345678")).thenReturn("encodedSecret");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newUser")
                .password("encodedSecret")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.register(request);

        // Then
        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        assertEquals("encodedSecret", result.getPassword());
        verify(notificationService).saveNotificationPreference(savedUser.getId(), false, null);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void editUserDetails_ShouldThrowException_IfUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        UserEditRequest dto = UserEditRequest.builder().build();

        // When & Then
        assertThrows(DomainException.class, () -> userService.editUserDetails(userId, dto));
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).saveNotificationPreference(any(), anyBoolean(), anyString());
    }

    @Test
    void editUserDetails_ShouldUpdateFieldsAndNotify_IfEmailNotBlank() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isActive(true)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserEditRequest dto = UserEditRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("johnPic.png")
                .build();

        // When
        userService.editUserDetails(userId, dto);

        // Then
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("johnPic.png", user.getProfilePicture());
        verify(notificationService).saveNotificationPreference(userId, true, "john@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void editUserDetails_ShouldUpdateFieldsAndNotifyFalse_IfEmailIsBlank() {
        // Given
        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Ivan")
                .lastName("Dimitrov")
                .email("")
                .profilePicture("www.image.com")
                .build();
        User user = User.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.editUserDetails(userId, dto);

        // Then
        assertEquals("Ivan", user.getFirstName());
        assertEquals("Dimitrov", user.getLastName());
        assertEquals("", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());
        verify(notificationService, times(1)).saveNotificationPreference(userId, false, null);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getAllUsers_ShouldReturnListFromRepository() {
        List<User> userList = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(userList);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
=======
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
>>>>>>> d383567 (Add banner image and update README)
        verify(userRepository).findAll();
    }

    @Test
    void getById_ShouldReturnUser_WhenUserExists() {
<<<<<<< HEAD
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
=======
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
>>>>>>> d383567 (Add banner image and update README)

        User result = userService.getById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void getById_ShouldThrowException_WhenUserDoesNotExist() {
<<<<<<< HEAD
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> userService.getById(userId));
    }

    @Test
    void loadUserByUsername_ShouldReturnAuthenticationMetadata_IfUserExists() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .password("12345678")
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testUser");

        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertEquals("ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_IfUserDoesNotExist() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> userService.loadUserByUsername("unknownUser"));
    }
public class UserServiceUTest {
=======
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
>>>>>>> d383567 (Add banner image and update README)
}
