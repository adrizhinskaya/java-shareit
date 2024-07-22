package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.exception.BookingStateBadRequestException;
import ru.practicum.shareit.exception.ColoredCRUDLogger;

import java.util.Arrays;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody BookingDto bookingDto) {
        ColoredCRUDLogger.logPost("/bookings", bookingDto.toString());
        var result = bookingService.create(userId, bookingDto);
        ColoredCRUDLogger.logPostComplete("/bookings", result.toString());
        return result;
    }

    @GetMapping("/{bookingId}")
    public Booking getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long bookingId) {
        String url = String.format("/bookings/{%s}", bookingId);
        ColoredCRUDLogger.logGet(url, userId.toString());
        var result = bookingService.getById(userId, bookingId);
        ColoredCRUDLogger.logGetComplete(url, result.toString());
        return result;
    }

    @GetMapping()
    public Collection<Booking> getAllByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(name = "state") String state,
                                              @RequestParam(name = "from") int from,
                                              @RequestParam(name = "size") int size) {
        if (Arrays.stream(BookingState.values()).noneMatch(e -> e.name().equals(state))) {
            throw new BookingStateBadRequestException(state);
        }

        String url = String.format("/bookings?state={%s}&from{%s}&size{%s}", state, from, size);
        ColoredCRUDLogger.logGet(url, userId.toString());
        var result = bookingService.getAllByBooker(userId, BookingState.valueOf(state), from, size);
        ColoredCRUDLogger.logGetComplete(url, "size=" + result.size());
        return result;
    }

    @GetMapping("/owner")
    public Collection<Booking> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(name = "state") String state,
                                             @RequestParam(name = "from") int from,
                                             @RequestParam(name = "size") int size) {
        if (Arrays.stream(BookingState.values()).noneMatch(e -> e.name().equals(state))) {
            throw new BookingStateBadRequestException(state);
        }

        String url = String.format("/bookings/owner?state={%s}&from{%s}&size{%s}", state, from, size);
        ColoredCRUDLogger.logGet(url, userId.toString());
        var result = bookingService.getAllByOwner(userId, BookingState.valueOf(state), from, size);
        ColoredCRUDLogger.logGetComplete(url, "size=" + result.size());
        return result;
    }

    @PatchMapping("/{bookingId}")
    public Booking changeStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @PathVariable Long bookingId,
                                @RequestParam(name = "approved") boolean approved) {
        String url = String.format("/bookings/{%s}?approved={%s}", bookingId, approved);
        ColoredCRUDLogger.logPatch(url, userId.toString());
        var result = bookingService.changeStatus(userId, bookingId, approved);
        ColoredCRUDLogger.logPatchComplete(url, result.toString());
        return result;
    }
}
