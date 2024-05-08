package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserBadRequestException;
import ru.practicum.shareit.exception.UserEmailConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        if (userRepository.emailExists(user.getId(), user.getEmail())) {
            throw new UserEmailConflictException("Попытка создания пользователя с дублирующимся email");
        }
        return userRepository.create(user);
    }

    @Override
    public Collection<User> getAll() {
        return userRepository.getAll();
    }

    @Override
    public User getById(Long id) {
        return userRepository.getById(id);
    }

    @Override
    public User update(Long userId, UserDto userDto) {
        if (userDto.getEmail() != null && userRepository.emailExists(userId, userDto.getEmail())) {
            throw new UserEmailConflictException("Попытка создания пользователя с дублирующимся email");
        }
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка обновления несуществующего пользователя");
        }
        return userRepository.update(userId, userDto);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }
}