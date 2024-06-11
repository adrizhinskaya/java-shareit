package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserBadRequestException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.model.UserUpdateDto;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User user = userRepository.save(UserMapper.mapToUser(userDto));
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public Collection<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return UserMapper.mapToUserDto(users);
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserBadRequestException("Пользователь не найден"));
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto update(Long userId, UserUpdateDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserBadRequestException("Попытка обновления несуществующего пользователя"));
        if(userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        User updatedUser = userRepository.save(user);
        return UserMapper.mapToUserDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}