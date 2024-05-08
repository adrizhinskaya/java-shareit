package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class InMemoryUserRepository implements UserRepository {
    private Long id = 0L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User getById(Long id) {
        return users.getOrDefault(id, null);
    }

    @Override
    public User update(Long userId, UserDto userDto) {
        User user = getById(userId);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        users.put(userId, user);
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public boolean userExists(Long id) {
        return getAll().stream()
                .anyMatch(user -> user.getId().equals(id));
    }

    @Override
    public boolean emailExists(Long userId, String email) {
        return getAll().stream()
                .anyMatch(user -> user.getEmail().equals(email) && !Objects.equals(user.getId(), userId));
    }

    private Long generateId() {
        return ++id;
    }
}