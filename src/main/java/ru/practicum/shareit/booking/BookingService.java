package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;

import java.util.Collection;

public interface BookingService {
    Booking create(Long userId, BookingDto bookingDto);

    Booking changeStatus(Long userId, Long bookingId, boolean approved);

    Booking getById(Long userId, Long bookingId);

    Collection<Booking> getAllByBooker(Long userId, BookingState state);

    Collection<Booking> getAllByOwner(Long userId, BookingState state);
}
