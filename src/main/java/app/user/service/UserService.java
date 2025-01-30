package app.user.service;

import app.exception.DomainException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.property.UserProperty;
import app.user.repository.UserRepository;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserProperty userProperty;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       UserProperty userProperty) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userProperty = userProperty;
    }

    public User login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        if (userOptional.isEmpty()) {
            throw new DomainException("User with username=[%s] does not exist.".formatted(loginRequest.getUsername()), HttpStatus.BAD_REQUEST);
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new DomainException("Login attempt with incorrect password for user with id [%s].".formatted(user.getId()), HttpStatus.BAD_REQUEST);
        }

        return user;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {
        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());
        if (userOptional.isPresent()) {
            throw new DomainException("User with username=[%s] already exists.".formatted(registerRequest.getUsername()), HttpStatus.BAD_REQUEST);
        }

        User user = initializeNewUserAccount(registerRequest);
        userRepository.save(user);

        log.info("Successfully created new user for username [%s] with id [%s].".formatted(user.getUsername(), user.getId()));

        return user;
    }

    private User initializeNewUserAccount(RegisterRequest dto) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(userProperty.getDefaultRole())
                .isActive(userProperty.isActiveByDefault())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public User getById(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(userId), HttpStatus.NOT_FOUND));
    }

    public User editUser(UUID userId, UserEditRequest userEditRequest) {
        User user = getById(userId);

        user.setFirstName(userEditRequest.getFirstName().trim());
        user.setLastName(userEditRequest.getLastName().trim());
        user.setEmail(userEditRequest.getEmail().trim());
        user.setProfilePicture(userEditRequest.getProfilePicture());
        user.setUpdatedOn(LocalDateTime.now());

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void switchStatus(UUID userId) {
        User user = getById(userId);

        user.setUpdatedOn(LocalDateTime.now());
        user.setActive(!user.isActive());

        userRepository.save(user);
    }

    public void changeUserRole(UUID userId) {
        User user = getById(userId);

        user.setUpdatedOn(LocalDateTime.now());
        if (user.getRole() == UserRole.ADMIN) {
            user.setRole(UserRole.USER);
        } else {
            user.setRole(UserRole.ADMIN);
        }

        userRepository.save(user);
    }
}
