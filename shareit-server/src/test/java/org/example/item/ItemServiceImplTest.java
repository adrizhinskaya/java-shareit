package org.example.item;

import lombok.RequiredArgsConstructor;
import org.example.booking.BookingRepository;
import org.example.booking.BookingService;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingDto;
import org.example.booking.model.BookingShort;
import org.example.exception.UserNotFoundException;
import org.example.item.comment.CommentDto;
import org.example.item.comment.CommentRepository;
import org.example.item.model.ItemDto;
import org.example.item.model.ItemGetDto;
import org.example.user.UserService;
import org.example.user.model.UserDto;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class ItemServiceImplTest {
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final EasyRandom generator = new EasyRandom();

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

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
                .itemId(item.getId())
                .id(bookDto2.getId())
                .bookerId(bookDto2.getBooker().getId())
                .build()));
        assertThat(item.getNextBooking(), equalTo(null));
        assertThat(item.getComments().size(), equalTo(1));
        assertThat(item.getComments().get(0), equalTo(comm2));

        assertThrows(UserNotFoundException.class, () -> {
            itemService.getAllByOwnerId(99L, 1, 1);
        });
        Mockito.verify(bookingRepository, Mockito.never())
                .findLastBookingsByItemIdsInAndStatusNotIn(any(Set.class), any(Set.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findNextBookingsByItemIdsInAndStatusNotIn(any(Set.class), any(Set.class));
        Mockito.verify(commentRepository, Mockito.never())
                .findAllByItemIdIn(any(Set.class));
    }

    @Test
    public void getAllItemsByOwnerIdPaginationTest() {
        UserDto user1 = userService.create(generator.nextObject(UserDto.class));
        UserDto user2 = userService.create(generator.nextObject(UserDto.class));

        ItemDto item1 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель1",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item2 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель2",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item3 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель3",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item4 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель4",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item5 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель5",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item6 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель6",
                "Аккумуляторная дрель + аккумулятор", true, null));

        List<ItemGetDto> itemList = itemService.getAllByOwnerId(user1.getId(), 3, 3);
        assertThat(itemList.size(), equalTo(3));
        assertThat(itemList.get(0).getId(), equalTo(item4.getId()));

        itemList = itemService.getAllByOwnerId(user1.getId(), 5, 6);
        assertThat(itemList.size(), equalTo(1));
        assertThat(itemList.get(0).getId(), equalTo(item6.getId()));

        itemList = itemService.getAllByOwnerId(user1.getId(), 5, 3);
        assertThat(itemList.size(), equalTo(1));
        assertThat(itemList.get(0).getId(), equalTo(item6.getId()));

        itemList = itemService.getAllByOwnerId(user1.getId(), 2, 3);
        assertThat(itemList.size(), equalTo(3));
        assertThat(itemList.get(0).getId(), equalTo(item3.getId()));

        itemList = itemService.getAllByOwnerId(user1.getId(), 7, 6);
        assertThat(itemList.size(), equalTo(0));
    }

    @Test
    public void getFromSearchPaginationTest() {
        UserDto user1 = userService.create(generator.nextObject(UserDto.class));
        UserDto user2 = userService.create(generator.nextObject(UserDto.class));

        ItemDto item1 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель1",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item2 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель2",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item3 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель3",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item4 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель4",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item5 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель5",
                "Аккумуляторная дрель + аккумулятор", true, null));
        ItemDto item6 = itemService.create(user1.getId(), makeItemDto("Аккумуляторная дрель6",
                "Аккумуляторная дрель + аккумулятор", true, null));

        List<ItemDto> itemList = itemService.getFromSearch(user1.getId(), "АкК", 3, 3);
        assertThat(itemList.size(), equalTo(3));
        assertThat(itemList.get(0).getId(), equalTo(item4.getId()));

        itemList = itemService.getFromSearch(user1.getId(), "АкК", 5, 6);
        assertThat(itemList.size(), equalTo(1));
        assertThat(itemList.get(0).getId(), equalTo(item6.getId()));

        itemList = itemService.getFromSearch(user1.getId(), "АкК", 5, 3);
        assertThat(itemList.size(), equalTo(1));
        assertThat(itemList.get(0).getId(), equalTo(item6.getId()));

        itemList = itemService.getFromSearch(user1.getId(), "АкК", 2, 3);
        assertThat(itemList.size(), equalTo(3));
        assertThat(itemList.get(0).getId(), equalTo(item3.getId()));

        itemList = itemService.getFromSearch(user1.getId(), "АкК", 7, 6);
        assertThat(itemList.size(), equalTo(0));
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }

    private CommentDto makeCommentDto(String text, String authorName) {
        return CommentDto.builder()
                .text(text)
                .authorName(authorName)
                .created(LocalDateTime.now())
                .build();
    }

    private ItemDto makeItemDto(String name, String description, Boolean available, Long requestId) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .requestId(requestId)
                .build();
    }

    private BookingDto makeBookingDto(Long itemId) {
        return BookingDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusNanos(10))
                .itemId(itemId)
                .build();
    }
}