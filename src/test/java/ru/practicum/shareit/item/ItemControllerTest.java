package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemGetDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    private CommentDto commentDto;
    private ItemDto itemDto;
    private ItemGetDto itemGetDto;
    private List<ItemDto> itemDtos;
    private List<ItemGetDto> itemGetDtos;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(ErrorHandler.class)
                .build();

        mapper.registerModule(new JavaTimeModule());

        commentDto = makeCommentDto();
        itemDto = makeItemDto();
        itemGetDto = makeItemGetDto(itemDto);
        itemDtos = List.of(itemDto);
        itemGetDtos = List.of(itemGetDto);
    }

    @Test
    void createItem() throws Exception {
        when(itemService.create(1L, itemDto))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));
    }

    @Test
    void addItemComment() throws Exception {
        when(itemService.addComment(1L, 1L, commentDto))
                .thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated().format(formatter))));
    }

    @Test
    void getAllByUserId() throws Exception {
        CommentDto comm = itemGetDto.getComments().get(0);
        when(itemService.getAllByOwnerId(1L, 0, 0))
                .thenReturn(itemGetDtos);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(1))))
                .andExpect(jsonPath("$[0].id", is(itemGetDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemGetDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemGetDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemGetDto.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking.id",
                        is(itemGetDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId",
                        is(itemGetDto.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].comments", hasSize(1)))
                .andExpect(jsonPath("$[0].comments[0].id", is(comm.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(comm.getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName", is(comm.getAuthorName())))
                .andExpect(jsonPath("$[0].comments[0].created", is(comm.getCreated().format(formatter))));
    }

    @Test
    void getFromSearch() throws Exception {
        when(itemService.getFromSearch(1L, "Отвертка", 0, 0))
                .thenReturn(itemDtos);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "Отвертка")
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(1))))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(itemDto.getRequestId()), Long.class));
    }

    @Test
    void getById() throws Exception {
        CommentDto comm = itemGetDto.getComments().get(0);
        when(itemService.getById(1L, 1L))
                .thenReturn(itemGetDto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemGetDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemGetDto.getName())))
                .andExpect(jsonPath("$.description", is(itemGetDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemGetDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking.id", is(itemGetDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(itemGetDto.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id", is(comm.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(comm.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(comm.getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(comm.getCreated().format(formatter))));
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.update(1L, 1L, itemDto))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));
    }

    @Test
    void createItemWithItemRequestNotFoundException() throws Exception {
        when(itemService.create(1L, itemDto))
                .thenThrow(ItemRequestNotFoundException.class);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void addItemCommentWithItemBadRequestException() throws Exception {
        when(itemService.addComment(1L, 1L, commentDto))
                .thenThrow(ItemBadRequestException.class);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void getAllByOwnerIdWithOwnerNotFoundException() throws Exception {
        when(itemService.getAllByOwnerId(1L, 0, 0))
                .thenThrow(OwnerNotFoundException.class);

        mvc.perform(get("/items")
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
    void getAllByOwnerIdWithException() throws Exception {
        when(itemService.getAllByOwnerId(1L, 0, 0))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(get("/items")
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
    void getFromSearchWithException() throws Exception {
        when(itemService.getFromSearch(1L, "Отвертка", 0, 0))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "Отвертка")
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void getByIdWithItemNotFoundException() throws Exception {
        CommentDto comm = itemGetDto.getComments().get(0);
        when(itemService.getById(1L, 1L))
                .thenThrow(ItemNotFoundException.class);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void getByIdWithException() throws Exception {
        CommentDto comm = itemGetDto.getComments().get(0);
        when(itemService.getById(1L, 1L))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void updateItemWithException() throws Exception {
        when(itemService.update(1L, 1L, itemDto))
                .thenThrow(UserNotFoundException.class);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    private ItemDto makeItemDto() {
        return ItemDto.builder()
                .id(1L)
                .name("Отвертка")
                .description("Аккумуляторная отвертка")
                .available(true)
                .requestId(1L)
                .build();
    }

    private ItemGetDto makeItemGetDto(ItemDto item) {
        return ItemGetDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(BookingShort.builder().id(1L).bookerId(1L).build())
                .nextBooking(BookingShort.builder().id(1L).bookerId(1L).build())
                .comments(List.of(makeCommentDto()))
                .build();
    }

    private CommentDto makeCommentDto() {
        return CommentDto.builder()
                .id(1L)
                .text("Коммент")
                .authorName("Пётр")
                .created(LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter))
                .build();
    }
}