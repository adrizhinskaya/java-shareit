package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.model.UserUpdateDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto userDto);

    Collection<UserDto> getAll();

    UserDto getById(Long id);

    UserDto update(Long userId, UserUpdateDto userDto);

    void delete(Long id);
}