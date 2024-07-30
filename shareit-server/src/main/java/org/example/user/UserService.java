package org.example.user;

import org.example.user.model.UserDto;
import org.example.user.model.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    List<UserDto> getAll();

    UserDto getById(Long id);

    UserDto update(Long userId, UserUpdateDto userDto);

    void delete(Long id);
}