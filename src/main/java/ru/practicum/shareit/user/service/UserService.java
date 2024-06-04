package ru.practicum.shareit.user.service;

import ru.practicum.shareit.exception.UserBadRequestException;
import ru.practicum.shareit.exception.UserEmailConflictException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;

public interface UserService {
    public UserDto create(UserDto userDto);

    public Collection<UserDto> getAll();

    public UserDto getById(Long id);

    public UserDto update(Long userId, UserDto userDto);

    public void delete(Long id);
}