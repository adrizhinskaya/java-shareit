package ru.practicum.shareit.user.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserUpdateDtoValidTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(ErrorHandler.class)
                .build();

        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createUserUpdateDtoWithBlankName() throws Exception {
        UserDto userDto = makeUserDto("Пётр", "some@email.com");
        UserUpdateDto userUpdateDto = makeUserUpdateDto("   ", "some@email.com");

        when(userService.update(1L, userUpdateDto))
                .thenReturn(userDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(1L))
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(200));
    }

    @Test
    void createUserUpdateDtoWithNullName() throws Exception {
        UserDto userDto = makeUserDto("Пётр", "some@email.com");
        UserUpdateDto userUpdateDto = makeUserUpdateDto(null, "some@email.com");

        when(userService.update(1L, userUpdateDto))
                .thenReturn(userDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(1L))
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(200));
    }

    @Test
    void createUserUpdateDtoWithNullEmail() throws Exception {
        UserDto userDto = makeUserDto("Пётр", "some@email.com");
        UserUpdateDto userUpdateDto = makeUserUpdateDto("Пётр", null);

        when(userService.update(1L, userUpdateDto))
                .thenReturn(userDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(1L))
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(200));
    }

    @Test
    void createUserUpdateDtoWithNotValidEmail() throws Exception {
        UserUpdateDto userUpdateDto = makeUserUpdateDto("Пётр", "someemail.com");
        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(1L))
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .id(1L)
                .name(name)
                .email(email)
                .build();
    }

    private UserUpdateDto makeUserUpdateDto(String name, String email) {
        return UserUpdateDto.builder()
                .name(name)
                .email(email)
                .build();
    }
}