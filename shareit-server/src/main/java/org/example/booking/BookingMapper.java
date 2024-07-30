package org.example.booking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingDto;
import org.example.item.model.Item;
import org.example.user.model.User;

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

    public static Booking mapToBooking(BookingDto bookingDto, Item item, User booker) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(bookingDto.getStatus())
                .build();
    }
}
