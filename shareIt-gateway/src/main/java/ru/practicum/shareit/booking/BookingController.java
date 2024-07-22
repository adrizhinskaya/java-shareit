package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.exception.BookingStateBadRequestException;
import ru.practicum.shareit.exception.ColoredCRUDLogger;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Arrays;

@RestController
@Validated
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody BookingDto bookingDto) {
        ColoredCRUDLogger.logPost("GATEWAY /bookings", bookingDto.toString());
        return bookingClient.create(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long bookingId) {
        String url = String.format("GATEWAY /bookings/{%s}", bookingId);
        ColoredCRUDLogger.logGet(url, userId.toString());
        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                 @RequestParam(name = "from", defaultValue = "0")
                                                 @PositiveOrZero int from,
                                                 @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        if (Arrays.stream(BookingState.values()).noneMatch(e -> e.name().equals(state))) {
            throw new BookingStateBadRequestException(state);
        }

        String url = String.format("GATEWAY /bookings?state={%s}&from{%s}&size{%s}", state, from, size);
        ColoredCRUDLogger.logGet(url, userId.toString());
        return bookingClient.getAllByBooker(userId, BookingState.valueOf(state), from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                @RequestParam(name = "from", defaultValue = "0")
                                                @PositiveOrZero int from,
                                                @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        if (Arrays.stream(BookingState.values()).noneMatch(e -> e.name().equals(state))) {
            throw new BookingStateBadRequestException(state);
        }

        String url = String.format("GATEWAY /bookings/owner?state={%s}&from{%s}&size{%s}", state, from, size);
        ColoredCRUDLogger.logGet(url, userId.toString());
        return bookingClient.getAllByOwner(userId, BookingState.valueOf(state), from, size);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @PathVariable Long bookingId,
                                               @RequestParam(name = "approved") boolean approved) {
        String url = String.format("GATEWAY /bookings/{%s}?approved={%s}", bookingId, approved);
        ColoredCRUDLogger.logPatch(url, userId.toString());
        return bookingClient.changeStatus(userId, bookingId, approved);
    }
}
