package app.entity.user.repository;

import app.entity.user.model.User;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Fetch(FetchMode.JOIN)
    Optional<User> findByUsername(String username);
}
