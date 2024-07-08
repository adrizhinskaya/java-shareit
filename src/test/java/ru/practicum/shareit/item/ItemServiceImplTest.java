package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemGetDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.UserDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class ItemServiceImplTest {
    private final EntityManager em;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @Test
    public void getAllItemsByOwnerId() {
        UserDto user1 = userService.create(makeUserDto("Пётр", "some@email.com"));
        UserDto user2 = userService.create(makeUserDto("НеПётр", "any@email.com"));

        List<ItemGetDto> itemList = itemService.getAllByOwnerId(user1.getId(), 0, 10);
        assertThat(itemList.size(), equalTo(0));

        ItemDto item1 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item2 = itemService.create(user1.getId(), makeItemDto("Щётка для обуви",
                "Стандартная щётка для обуви", true, null));
        Booking book1 = bookingService.create(user2.getId(), makeBookingDto(item1.getId()));
        Booking book2 = bookingService.create(user2.getId(), makeBookingDto(item2.getId()));
        Booking bookDto1 = bookingService.changeStatus(user1.getId(), book1.getId(), true);
        Booking bookDto2 = bookingService.changeStatus(user1.getId(), book2.getId(), true);
        CommentDto comm1 = itemService.addComment(user2.getId(), item1.getId(),
                makeCommentDto("Коммент на дрель", user2.getName()));
        CommentDto comm2 = itemService.addComment(user2.getId(), item2.getId(),
                makeCommentDto("Коммент на щётку", user2.getName()));

        itemList = itemService.getAllByOwnerId(user2.getId(), 0, 10);
        assertThat(itemList.size(), equalTo(0));

        itemList = itemService.getAllByOwnerId(user1.getId(), 0, 20);
        assertThat(itemList.size(), equalTo(2));

        itemList = itemService.getAllByOwnerId(user1.getId(), 1, 1);
        ItemGetDto item = itemList.get(0);
        assertThat(itemList.size(), equalTo(1));
        assertThat(item.getId(), equalTo(item2.getId()));
        assertThat(item.getName(), equalTo(item2.getName()));
        assertThat(item.getDescription(), equalTo(item2.getDescription()));
        assertThat(item.getAvailable(), equalTo(item2.getAvailable()));
        assertThat(item.getLastBooking(), equalTo(BookingShort.builder()
                .id(bookDto2.getId())
                .bookerId(bookDto2.getBooker().getId())
                .build()));
        assertThat(item.getNextBooking(), equalTo(null));
        assertThat(item.getComments().size(), equalTo(1));
        assertThat(item.getComments().get(0), equalTo(comm2));

        assertThrows(UserNotFoundException.class, () -> {
            itemService.getAllByOwnerId(99L, 1, 1);
        });
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }

    public static CommentDto makeCommentDto(String text, String authorName) {
        return CommentDto.builder()
                .text(text)
                .authorName(authorName)
                .created(LocalDateTime.now())
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

    public static BookingDto makeBookingDto(Long itemId) {
        return BookingDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusNanos(10))
                .itemId(itemId)
                .build();
    }
}