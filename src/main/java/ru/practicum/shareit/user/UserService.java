package ru.practicum.shareit.user;

import ru.practicum.shareit.user.UserDto;

import java.util.Collection;

public interface UserService {
    public UserDto create(UserDto userDto);

    public Collection<UserDto> getAll();

    public UserDto getById(Long id);

    public UserDto update(Long userId, UserDto userDto);

    public void delete(Long id);
}