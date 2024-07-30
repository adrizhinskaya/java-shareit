package org.example.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.exception.ColoredCRUDLogger;
import org.example.user.model.UserDto;
import org.example.user.model.UserUpdateDto;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserClient userClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto user) {
        ColoredCRUDLogger.logPost("GATEWAY /users", user.toString());
        return userClient.create(user);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        ColoredCRUDLogger.logGet("/users", null);
        return userClient.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        String url = String.format("GATEWAY /users/{%s}", id);
        ColoredCRUDLogger.logGet(url, null);
        return userClient.getById(id);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable Long userId,
                                         @Valid @RequestBody UserUpdateDto userUpdateDto) {
        String url = String.format("GATEWAY /users/{%s}", userId);
        ColoredCRUDLogger.logPatch(url, userUpdateDto.toString());
        return userClient.update(userId, userUpdateDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        ColoredCRUDLogger.logDelete("GATEWAY /users", id.toString());
        return userClient.delete(id);
    }
}