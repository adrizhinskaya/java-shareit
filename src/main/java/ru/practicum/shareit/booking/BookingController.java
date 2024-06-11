package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.exception.BookingStateBadRequestException;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    String postColor = "\u001b[33m" + "POST";
    String patchColor = "\u001b[35m" + "PATCH";
    String getColor = "\u001b[32m" + "GET";
    String deleteColor = "\u001b[31m" + "DELETE";
    String resetColor = "\u001b[0m";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody BookingDto bookingDto) {
        log.info("{} /bookings: {}{}", postColor, bookingDto.toString(), resetColor);
        var result = bookingService.create(userId, bookingDto);
        log.info("completion POST /bookings: {}", result.toString());
        return result;
    }

    @PatchMapping("/{bookingId}")
    public Booking changeStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @PathVariable Long bookingId,
                                @RequestParam boolean approved) {
        log.info("{} /bookings/{}?approved={}: {}{}", patchColor, bookingId, approved, userId, resetColor);
        var result = bookingService.changeStatus(userId, bookingId, approved);
        log.info("completion PATCH /bookings/{}?approved={}: {}", bookingId, approved, result.toString());
        return result;
    }

    @GetMapping("/{bookingId}")
    public Booking getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long bookingId) {
        log.info("{} /bookings/{}: {}{}", getColor, bookingId, userId, resetColor);
        var result = bookingService.getById(userId, bookingId);
        log.info("completion GET /bookings/{}: {}", bookingId, result.toString());
        return result;
    }

    @GetMapping()
    public Collection<Booking> getAllByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(required = false, defaultValue = "ALL") String state) {
        if (!Arrays.asList(BookingState.values()).stream().anyMatch(e -> e.name().equals(state))) {
            throw new BookingStateBadRequestException(state);
        }

        log.info("{} /bookings/{}: {}{}", getColor, state, userId, resetColor);
        var result = bookingService.getAllByBooker(userId, BookingState.valueOf(state));
        log.info("completion GET /bookings/{}: {}", state, result.size());
        return result;
    }

    @GetMapping("/owner")
    public Collection<Booking> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(required = false, defaultValue = "ALL") String state) {
        if (!Arrays.asList(BookingState.values()).stream().anyMatch(e -> e.name().equals(state))) {
            throw new BookingStateBadRequestException(state);
        }

        log.info("{} /bookings/owner/{}: {}{}", getColor, state, userId, resetColor);
        var result = bookingService.getAllByOwner(userId, BookingState.valueOf(state));
        log.info("completion GET /bookings/owner/{}: {}", state, result.size());
        return result;
    }
}
