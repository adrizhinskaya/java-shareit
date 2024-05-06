package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
@Builder
@AllArgsConstructor
public class UserDto {
    private String name;
    @Email
    private String email;
}