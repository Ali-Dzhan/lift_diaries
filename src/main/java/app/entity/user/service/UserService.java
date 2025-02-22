package app.entity.user.service;

import app.entity.user.model.User;
import app.entity.user.model.UserRole;
import app.entity.user.repository.UserRepository;
import app.exception.DomainException;
import app.security.AuthenticationMetadata;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionUser = userRepository.findByUsername(registerRequest.getUsername());
        if (optionUser.isPresent()) {
            throw new DomainException("*Username [%s] already exist.*".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));

        log.info("Successfully create new user account for username [%s] and id [%s]"
                .formatted(user.getUsername(), user.getId()));

        return user;
    }

    @CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());

        userRepository.save(user);
    }

    private User initializeUser(RegisterRequest registerRequest) {

        return User.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    @Cacheable("users")
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(()
                -> new DomainException("User with id [%s] does not exist.".formatted(id)));
    }

    @CacheEvict(value = "users", allEntries = true)
    public void switchStatus(UUID userId) {

        User user = getById(userId);

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void switchRole(UUID userId) {

        User user = getById(userId);

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new DomainException("User with this username does not exist."));

        return new AuthenticationMetadata(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }
}
