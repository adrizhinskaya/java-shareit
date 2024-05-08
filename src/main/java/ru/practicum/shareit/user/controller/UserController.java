package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users: {}", user.toString());
        var result = userService.create(user);
        log.info("completion POST /users: {}", result.toString());
        return result;
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("GET /users");
        var result = userService.getAll();
        log.info("completion GET /users: size {}", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        log.info("GET /users/{id}: {}", id);
        var result = userService.getById(id);
        log.info("completion GET /users/{id}: {}", result.toString());
        return result;
    }

    @PatchMapping("/{userId}")
    public User update(@PathVariable Long userId,
                       @Valid @RequestBody UserDto userDto) {
        log.info("PATCH /users/{userId}: {}, {}", userId, userDto.toString());
        var result = userService.update(userId, userDto);
        log.info("completion PATCH /users/{userId}: {}", result.toString());
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("DELETE /users/{id}: {}", id);
        userService.delete(id);
        log.info("completion DELETE /users/{id}: {} success", id);
    }
}