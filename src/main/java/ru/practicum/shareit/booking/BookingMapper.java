package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {
    public static BookingDto mapToBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .status(booking.getStatus())
                .build();
    }

    public static Collection<BookingDto> mapToBookingDto(Iterable<Booking> bookings) {
        Collection<BookingDto> dtos = new ArrayList<>();
        for (Booking booking : bookings) {
            dtos.add(mapToBookingDto(booking));
        }
        return dtos;
    }

    public static Booking mapToBooking(BookingDto bookingDto, Item item, User owner) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(owner)
                .status(bookingDto.getStatus())
                .build();
    }
}
