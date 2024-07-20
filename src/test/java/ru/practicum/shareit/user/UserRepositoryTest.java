package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testExistsByEmailContainingIgnoreCase() {
        assertFalse(userRepository.existsByEmailContainingIgnoreCase("emailSearch"));
        User user = makeUser();
        userRepository.save(user);
        assertTrue(userRepository.existsByEmailContainingIgnoreCase(user.getEmail()));
    }

    private User makeUser() {
        return User.builder()
                .name("Пётр")
                .email("some@email.com")
                .build();
    }
}