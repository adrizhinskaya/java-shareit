package org.example.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailContainingIgnoreCase(String emailSearch);
}