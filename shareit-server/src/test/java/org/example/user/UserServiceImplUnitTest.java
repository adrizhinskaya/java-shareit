package org.example.user;

import org.example.exception.UserNotFoundException;
import org.example.user.model.User;
import org.example.user.model.UserDto;
import org.example.user.model.UserUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserServiceImplUnitTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = makeUserDto("John", "john@example.com");
        user = makeUser(1L, "John", "john@example.com");
    }

    @Test
    public void testCreate() {
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        UserDto resultUser = userService.create(userDto);

        assertEquals(user.getId(), resultUser.getId());
        assertEquals(userDto.getName(), resultUser.getName());
        assertEquals(userDto.getEmail(), resultUser.getEmail());
    }

    @Test
    public void testGetByIdNotFound() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    public void testGetById() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto resultUser = userService.getById(user.getId());

        assertEquals(user.getId(), resultUser.getId());
        assertEquals(user.getName(), resultUser.getName());
        assertEquals(user.getEmail(), resultUser.getEmail());
    }

    @Test
    public void testUpdateNotFound() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(null, "NEWJohn", "NEWjohn@example.com");
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.update(99L, userUpdateDto));
    }

    @Test
    public void testUpdate() {
        User newUser = makeUser(user.getId(), "NEWJohn", "NEWjohn@example.com");
        UserUpdateDto userUpdateDto = new UserUpdateDto(null, "NEWJohn", "NEWjohn@example.com");

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(newUser);

        UserDto resultUser = userService.update(user.getId(), userUpdateDto);

        assertEquals(user.getId(), resultUser.getId());
        assertEquals(userUpdateDto.getName(), resultUser.getName());
        assertEquals(userUpdateDto.getEmail(), resultUser.getEmail());
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }

    private User makeUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }
}
