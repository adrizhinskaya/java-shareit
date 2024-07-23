package org.example.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.example.exception.ColoredCRUDLogger;
import org.example.user.model.UserDto;
import org.example.user.model.UserUpdateDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto user) {
        ColoredCRUDLogger.logPost("/users", user.toString());
        var result = userService.create(user);
        ColoredCRUDLogger.logPostComplete("/users", result.toString());
        return result;
    }

    @GetMapping
    public List<UserDto> getAll() {
        ColoredCRUDLogger.logGet("/users", null);
        var result = userService.getAll();
        ColoredCRUDLogger.logGetComplete("/users", "size=" + result.size());
        return result;
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        String url = String.format("/users/{%s}", id);
        ColoredCRUDLogger.logGet(url, null);
        var result = userService.getById(id);
        ColoredCRUDLogger.logGetComplete(url, result.toString());
        return result;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @RequestBody UserUpdateDto userUpdateDto) {
        String url = String.format("/users/{%s}", userId);
        ColoredCRUDLogger.logPatch(url, userUpdateDto.toString());
        var result = userService.update(userId, userUpdateDto);
        ColoredCRUDLogger.logPatchComplete(url, result.toString());
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ColoredCRUDLogger.logDelete("/users", id.toString());
        userService.delete(id);
        ColoredCRUDLogger.logDeleteComplete("/users", id.toString());
    }
}