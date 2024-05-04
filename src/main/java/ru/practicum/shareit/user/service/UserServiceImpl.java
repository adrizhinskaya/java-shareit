package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserBadRequestException;
import ru.practicum.shareit.exception.UserEmailConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        log.info("POST /users");
        if (userRepository.emailExists(user.getId(), user.getEmail())) {
            throw new UserEmailConflictException("Попытка создания пользователя с дублирующимся email");
        }
        User result = userRepository.create(user);
        log.info("Создан пользователь {}", result.toString());
        return result;
    }

    @Override
    public Collection<User> getAll() {
        log.info("GET /users");
        Collection<User> result = userRepository.getAll();
        log.info("Получен список пользователей из {} элементов", result.size());
        return result;
    }

    @Override
    public User getById(Long id) {
        log.info("GET /users/{id}");
        User result = userRepository.getById(id);
        log.info("Получен пользователь {}", result.toString());
        return result;
    }

    @Override
    public User update(Long userId, UserDto userDto) {
        log.info("PATCH /users/{userId}");
        if (userDto.getEmail() != null && userRepository.emailExists(userId, userDto.getEmail())) {
            throw new UserEmailConflictException("Попытка создания пользователя с дублирующимся email");
        }
        if (!userRepository.userExists(userId)) {
            throw new UserBadRequestException("Попытка обновления несуществующего пользователя");
        }
        User result = userRepository.update(userId, userDto);
        log.info("Обновлён пользователь {}", result.toString());
        return result;
    }

    @Override
    public void delete(Long id) {
        log.info("DELETE /users/{id}");
        userRepository.delete(id);
        log.info("Удалён пользователь с id {}", id);
    }
}
