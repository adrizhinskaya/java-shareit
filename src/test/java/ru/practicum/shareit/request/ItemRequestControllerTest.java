package ru.practicum.shareit.request;

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
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.ItemShort;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestGetDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Mock
    private ItemRequestService requestService;

    @InjectMocks
    private ItemRequestController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    private ItemRequestDto requestDto;
    private ItemRequestGetDto requestGetDto;
    private List<ItemRequestDto> requestDtos;
    private List<ItemRequestGetDto> requestGetDtos;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(ErrorHandler.class)
                .build();

        mapper.registerModule(new JavaTimeModule());

        requestDto = makeItemRequestDto();
        requestGetDto = makeItemRequestGetDto(requestDto);
        requestDtos = List.of(requestDto);
        requestGetDtos = List.of(requestGetDto);
    }

    @Test
    void createItemRequest() throws Exception {
        when(requestService.create(1L, requestDto))
                .thenReturn(requestDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.requesterId", is(requestDto.getRequesterId()), Long.class))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().format(formatter))));
    }

    @Test
    void getAllByRequesterId() throws Exception {
        ItemShort item = requestGetDto.getItems().get(0);
        when(requestService.getAllByRequesterId(1L))
                .thenReturn(requestGetDtos);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(1))))
                .andExpect(jsonPath("$[0].id", is(requestGetDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestGetDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestGetDto.getCreated().format(formatter))))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].items[0].requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].available", is(item.getAvailable())));
    }

    @Test
    void getAll() throws Exception {
        ItemShort item = requestGetDto.getItems().get(0);
        when(requestService.getAll(1L, 0, 0))
                .thenReturn(requestGetDtos);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(1))))
                .andExpect(jsonPath("$[0].id", is(requestGetDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestGetDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestGetDto.getCreated().format(formatter))))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].items[0].requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].available", is(item.getAvailable())));
    }

    @Test
    void getById() throws Exception {
        ItemShort item = requestGetDto.getItems().get(0);
        when(requestService.getById(1L, 1L))
                .thenReturn(requestGetDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestGetDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestGetDto.getDescription())))
                .andExpect(jsonPath("$.created", is(requestGetDto.getCreated().format(formatter))))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.items[0].name", is(item.getName())))
                .andExpect(jsonPath("$.items[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$.items[0].requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$.items[0].available", is(item.getAvailable())));
    }

    @Test
    void createItemRequestWithException() throws Exception {
        when(requestService.create(any(), any()))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void getAllByRequesterIdWithException() throws Exception {
        when(requestService.getAllByRequesterId(1L))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void getAllWithException() throws Exception {
        when(requestService.getAll(1L, 0, 0))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void getAllByIdWithException() throws Exception {
        when(requestService.getById(any(), any()))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    private ItemRequestDto makeItemRequestDto() {
        return ItemRequestDto.builder()
                .id(1L)
                .description("Хотел бы воспользоваться щёткой для обуви")
                .requesterId(1L)
                .created(LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter))
                .build();
    }

    private ItemRequestGetDto makeItemRequestGetDto(ItemRequestDto request) {
        List<ItemShort> items = List.of(ItemShort.builder()
                .id(1L)
                .name("Item1")
                .description("Description 1")
                .requestId(1L)
                .available(true)
                .build());
        return ItemRequestGetDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .build();
    }
}