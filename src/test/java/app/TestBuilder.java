package app;

import app.category.model.Category;
import app.exercise.model.Exercise;
import app.user.model.User;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

import static app.user.model.UserRole.USER;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {
        String randomSuffix = UUID.randomUUID().toString().substring(0,8);
        return User.builder()
                .username("User_" + randomSuffix)
                .email("user" + randomSuffix + "@test.com")
                .password("encodedpassword")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .isActive(true)
                .role(USER)
                .build();
    }

    public static Category aRandomCategory() {
        String randomSuffix = UUID.randomUUID().toString().substring(0,8);
        return Category.builder()
                .name("Core" + randomSuffix)
                .imageUrl("url")
                .build();
    }

    public static Exercise aRandomExercise(Category category) {
        return Exercise.builder()
                .name("Push-up")
                .description("Push desc")
                .gifUrl("pushup.gif")
                .sets(3)
                .reps(10)
                .category(category)
                .build();
    }

}
