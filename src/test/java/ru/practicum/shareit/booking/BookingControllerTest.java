package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

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
class BookingControllerTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    private Booking booking;
    private BookingDto bookingDto;
    private List<Booking> bookings;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(ErrorHandler.class)
                .build();

        mapper.registerModule(new JavaTimeModule());

        booking = makeBooking();
        bookingDto = makeBookingDto(booking);
        bookings = List.of(booking);
    }

    @Test
    void createBooking() throws Exception {
        when(bookingService.create(1L, bookingDto))
                .thenReturn(booking);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(booking.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(booking.getEnd().format(formatter))))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(booking.getItem().getName())));
    }

    @Test
    void changeStatus() throws Exception {
        when(bookingService.changeStatus(1L, 1L, true))
                .thenReturn(booking);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(booking.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(booking.getEnd().format(formatter))))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(booking.getItem().getName())));
    }

    @Test
    void getByIdWithBookingNotFoundException() throws Exception {
        when(bookingService.getById(1L, 1L))
                .thenThrow(BookingNotFoundException.class);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void getById() throws Exception {
        when(bookingService.getById(1L, 1L))
                .thenReturn(booking);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(1L))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(booking.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(booking.getEnd().format(formatter))))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(booking.getItem().getName())));
    }

    @Test
    void getAllByBookerBadRequest() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "uNsuPPorted")
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByBooker() throws Exception {
        when(bookingService.getAllByBooker(1L, BookingState.ALL, 0, 0))
                .thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(1))))
                .andExpect(jsonPath("$[0].id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(booking.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].end", is(booking.getEnd().format(formatter))))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$[0].item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(booking.getItem().getName())));
    }

    @Test
    void getAllByOwnerBadRequest() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "uNsuPPorted")
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingService.getAllByOwner(2L, BookingState.ALL, 0, 0))
                .thenReturn(bookings);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$", hasSize(1))))
                .andExpect(jsonPath("$[0].id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(booking.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].end", is(booking.getEnd().format(formatter))))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$[0].item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(booking.getItem().getName())));
    }

    private BookingDto makeBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .status(booking.getStatus())
                .build();
    }

    private Booking makeBooking() {
        User itemOwner = User.builder()
                .id(2L)
                .name("Пётр")
                .email("user@user.com")
                .build();

        User itemBooker = User.builder()
                .id(1L)
                .name("Пъедро")
                .email("other@other.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Отвёртка")
                .description("Аккумуляторная отвертка")
                .available(true)
                .owner(itemOwner)
                .request(null)
                .build();

        return Booking.builder()
                .id(1L)
                .start(LocalDateTime.parse(LocalDateTime.now().plusHours(1).format(formatter), formatter))
                .end(LocalDateTime.parse(LocalDateTime.now().plusHours(2).format(formatter), formatter))
                .item(item)
                .booker(itemBooker)
                .status(BookingStatus.APPROVED)
                .build();
    }
}