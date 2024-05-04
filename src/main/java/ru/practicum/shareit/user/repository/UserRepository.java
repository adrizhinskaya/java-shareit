package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {
    public User create(User user);

    public Collection<User> getAll();

    public User getById(Long id);

    public User update(Long userId, UserDto userDto);

    public void delete(Long id);

    public boolean userExists(Long id);

    public boolean emailExists(Long userId, String email);
}
