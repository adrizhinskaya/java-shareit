package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.model.UserUpdateDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService userService;
    String postColor = "\u001b[33m" + "POST";
    String patchColor = "\u001b[35m" + "PATCH";
    String getColor = "\u001b[32m" + "GET";
    String deleteColor = "\u001b[31m" + "DELETE";
    String resetColor = "\u001b[0m";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto user) {
        log.info("{} /users: {}{}", postColor, user.toString(), resetColor);
        var result = userService.create(user);
        log.info("completion POST /users: {}", result.toString());
        return result;
    }

    @GetMapping
    public Collection<UserDto> getAll() {
        log.info("{} /users{}", getColor, resetColor);
        var result = userService.getAll();
        log.info("completion GET /users: size {}", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        log.info("{} /users/{}: {}", getColor, id, resetColor);
        var result = userService.getById(id);
        log.info("completion GET /users/{id}: {}", result.toString());
        return result;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("{} /users/{}: {}{}", patchColor, userId, userUpdateDto.toString(), resetColor);
        var result = userService.update(userId, userUpdateDto);
        log.info("completion PATCH /users/{userId}: {}", result.toString());
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("{} /users/{}{}", deleteColor, id, resetColor);
        userService.delete(id);
        log.info("completion DELETE /users/{id}: {} success", id);
    }
}