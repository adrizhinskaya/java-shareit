package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    User create(User user);

    Collection<User> getAll();

    User getById(Long id);

    User update(Long userId, UserDto userDto);

    void delete(Long id);
}
