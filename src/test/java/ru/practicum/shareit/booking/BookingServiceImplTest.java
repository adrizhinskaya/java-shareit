package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class BookingServiceImplTest {
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @Test
    public void getAllByBooker() {
        UserDto user1 = userService.create(makeUserDto("2Пётр", "some2@email.com"));
        UserDto user2 = userService.create(makeUserDto("3НеПётр", "any3@email.com"));
        ItemDto item1 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item2 = itemService.create(user1.getId(), makeItemDto("Щётка для обуви",
                "Стандартная щётка для обуви", true, null));
        Booking book1 = bookingService.create(user2.getId(), makeBookingDto(item1.getId(),
                LocalDateTime.now().minusSeconds(2), LocalDateTime.now().minusSeconds(1)));
        Booking book11 = bookingService.create(user2.getId(), makeBookingDto(item1.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)));
        Booking book2 = bookingService.create(user2.getId(), makeBookingDto(item2.getId(),
                LocalDateTime.now().minusSeconds(2), LocalDateTime.now().plusHours(2)));
        Booking book22 = bookingService.create(user2.getId(), makeBookingDto(item2.getId(),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)));
        bookingService.changeStatus(user1.getId(), book11.getId(), false);
        bookingService.changeStatus(user1.getId(), book2.getId(), true);
        bookingService.changeStatus(user1.getId(), book22.getId(), false);

        List<Booking> bookList = bookingService.getAllByBooker(user1.getId(), BookingState.ALL, 0, 10);
        assertThat(bookList.size(), equalTo(0));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.PAST, 0, 10);
        Booking book = bookList.get(0);
        assertThat(bookList.size(), equalTo(1));
        assertThat(book, equalTo(book1));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.WAITING, 0, 20);
        book = bookList.get(0);
        assertThat(bookList.size(), equalTo(1));
        assertThat(book, equalTo(book1));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.CURRENT, 0, 10);
        book = bookList.get(0);
        assertThat(bookList.size(), equalTo(1));
        assertThat(book, equalTo(book2));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.REJECTED, 0, 20);
        assertThat(bookList.size(), equalTo(2));
        assertThat(bookList.get(0), equalTo(book22));
        assertThat(bookList.get(1), equalTo(book11));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.FUTURE, 0, 10);
        assertThat(bookList.size(), equalTo(2));
        assertThat(bookList.get(0), equalTo(book2));
        assertThat(bookList.get(1), equalTo(book1));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.ALL, 0, 10);
        assertThat(bookList.size(), equalTo(4));
        assertThat(bookList.get(0), equalTo(book22));
        assertThat(bookList.get(1), equalTo(book11));
        assertThat(bookList.get(2), equalTo(book2));
        assertThat(bookList.get(3), equalTo(book1));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.ALL, 2, 2);
        assertThat(bookList.size(), equalTo(2));
        assertThat(bookList.get(0), equalTo(book2));
        assertThat(bookList.get(1), equalTo(book1));

        assertThrows(UserNotFoundException.class, () -> {
            bookingService.getAllByBooker(99L, BookingState.ALL, 0, 0);
        });
    }

    @Test
    public void getAllByBookerPaginationTest() {
        UserDto user1 = userService.create(makeUserDto("2Пётр", "some2@email.com"));
        UserDto user2 = userService.create(makeUserDto("3НеПётр", "any3@email.com"));

        ItemDto item1 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item2 = itemService.create(user1.getId(), makeItemDto("Щётка для обуви",
                "Стандартная щётка для обуви", true, null));

        Booking book1 = bookingService.create(user2.getId(), makeBookingDto(item1.getId(),
                LocalDateTime.now().minusSeconds(6), LocalDateTime.now().plusHours(1)));
        Booking book2 = bookingService.create(user2.getId(), makeBookingDto(item1.getId(),
                LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusHours(2)));
        Booking book3 = bookingService.create(user2.getId(), makeBookingDto(item1.getId(),
                LocalDateTime.now().minusSeconds(4), LocalDateTime.now().plusHours(3)));
        Booking book4 = bookingService.create(user2.getId(), makeBookingDto(item2.getId(),
                LocalDateTime.now().minusSeconds(3), LocalDateTime.now().plusHours(4)));
        Booking book5 = bookingService.create(user2.getId(), makeBookingDto(item2.getId(),
                LocalDateTime.now().minusSeconds(2), LocalDateTime.now().plusHours(5)));
        Booking book6 = bookingService.create(user2.getId(), makeBookingDto(item2.getId(),
                LocalDateTime.now().minusSeconds(1), LocalDateTime.now().plusHours(6)));

        List<Booking> bookList = bookingService.getAllByBooker(user2.getId(), BookingState.ALL, 3, 3);
        assertThat(bookList.size(), equalTo(3));
        assertThat(bookList.get(0).getId(), equalTo(book3.getId()));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.ALL, 5, 6);
        assertThat(bookList.size(), equalTo(1));
        assertThat(bookList.get(0).getId(), equalTo(book1.getId()));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.ALL, 5, 3);
        assertThat(bookList.size(), equalTo(1));
        assertThat(bookList.get(0).getId(), equalTo(book1.getId()));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.ALL, 2, 3);
        assertThat(bookList.size(), equalTo(3));
        assertThat(bookList.get(0).getId(), equalTo(book4.getId()));

        bookList = bookingService.getAllByBooker(user2.getId(), BookingState.ALL, 7, 6);
        assertThat(bookList.size(), equalTo(0));
    }

    @Test
    public void getAllByOwnerPaginationTest() {
        UserDto user1 = userService.create(makeUserDto("2Пётр", "some2@email.com"));
        UserDto user2 = userService.create(makeUserDto("3НеПётр", "any3@email.com"));

        ItemDto item1 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item2 = itemService.create(user1.getId(), makeItemDto("Щётка для обуви",
                "Стандартная щётка для обуви", true, null));

        Booking book1 = bookingService.create(user2.getId(), makeBookingDto(item1.getId(),
                LocalDateTime.now().minusSeconds(6), LocalDateTime.now().plusHours(1)));
        Booking book2 = bookingService.create(user2.getId(), makeBookingDto(item1.getId(),
                LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusHours(2)));
        Booking book3 = bookingService.create(user2.getId(), makeBookingDto(item1.getId(),
                LocalDateTime.now().minusSeconds(4), LocalDateTime.now().plusHours(3)));
        Booking book4 = bookingService.create(user2.getId(), makeBookingDto(item2.getId(),
                LocalDateTime.now().minusSeconds(3), LocalDateTime.now().plusHours(4)));
        Booking book5 = bookingService.create(user2.getId(), makeBookingDto(item2.getId(),
                LocalDateTime.now().minusSeconds(2), LocalDateTime.now().plusHours(5)));
        Booking book6 = bookingService.create(user2.getId(), makeBookingDto(item2.getId(),
                LocalDateTime.now().minusSeconds(1), LocalDateTime.now().plusHours(6)));

        List<Booking> bookList = bookingService.getAllByOwner(user1.getId(), BookingState.ALL, 3, 3);
        assertThat(bookList.size(), equalTo(3));
        assertThat(bookList.get(0).getId(), equalTo(book3.getId()));

        bookList = bookingService.getAllByOwner(user1.getId(), BookingState.ALL, 5, 6);
        assertThat(bookList.size(), equalTo(1));
        assertThat(bookList.get(0).getId(), equalTo(book1.getId()));

        bookList = bookingService.getAllByOwner(user1.getId(), BookingState.ALL, 5, 3);
        assertThat(bookList.size(), equalTo(1));
        assertThat(bookList.get(0).getId(), equalTo(book1.getId()));

        bookList = bookingService.getAllByOwner(user1.getId(), BookingState.ALL, 2, 3);
        assertThat(bookList.size(), equalTo(3));
        assertThat(bookList.get(0).getId(), equalTo(book4.getId()));

        bookList = bookingService.getAllByOwner(user1.getId(), BookingState.ALL, 7, 6);
        assertThat(bookList.size(), equalTo(0));
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }

    public static ItemDto makeItemDto(String name, String description, Boolean available, Long requestId) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .requestId(requestId)
                .build();
    }

    public static BookingDto makeBookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        return BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
    }
}