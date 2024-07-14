package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    Booking booking1;
    Booking booking2;
    Booking booking3;
    Booking booking4;

    @BeforeEach
    void setUp() {
        user1 = makeUser("Пётр", "some@email.com");
        user2 = makeUser("НеПётр", "any@email.com");
        item1 = makeItem("Отвёртка");
        item2 = makeItem("Супер Отвёртка");
        booking1 = makeBooking(LocalDateTime.now().minusSeconds(2), LocalDateTime.now().minusSeconds(1), item1,
                BookingStatus.WAITING);
        booking2 = makeBooking(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), item1,
                BookingStatus.REJECTED);
        booking3 = makeBooking(LocalDateTime.now().minusSeconds(2), LocalDateTime.now().plusHours(2), item2,
                BookingStatus.APPROVED);
        booking4 = makeBooking(LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3), item2,
                BookingStatus.CANCELED);
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void testFindAllByBooker_IdAndStatusInOrderByStartDesc() {
        Long bookerId = user2.getId();
        Set<BookingStatus> statusSet = new HashSet<>(Set.of(BookingStatus.WAITING));
        Pageable page = PageRequest.of(0, 10);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        Page<Booking> pages = bookingRepository.findAllByBooker_IdAndStatusInOrderByStartDesc(
                1L, statusSet, page);
        assertEquals(0, pages.getContent().size());

        pages = bookingRepository.findAllByBooker_IdAndStatusInOrderByStartDesc(bookerId, statusSet, page);
        assertEquals(1, pages.getContent().size());

        statusSet.add(BookingStatus.APPROVED);
        statusSet.add(BookingStatus.REJECTED);
        statusSet.add(BookingStatus.CANCELED);
        pages = bookingRepository.findAllByBooker_IdAndStatusInOrderByStartDesc(bookerId, statusSet, page);
        List<Booking> bookings = pages.getContent();

        assertEquals(4, pages.getContent().size());
        assertEquals(booking4, pages.getContent().get(0));

        page = PageRequest.of(1, 2);
        pages = bookingRepository.findAllByBooker_IdAndStatusInOrderByStartDesc(bookerId, statusSet, page);
        assertEquals(2, pages.getContent().size());
        assertEquals(booking3, pages.getContent().get(0));
    }

    @Test
    void testExistsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn() {
        Long bookerId = user2.getId();
        Long itemId = item1.getId();
        Set<BookingStatus> statusSet = Set.of(BookingStatus.WAITING);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        boolean result = bookingRepository.existsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn(
                bookerId, itemId, LocalDateTime.now(), statusSet);
        assertFalse(result);

        statusSet = Set.of(BookingStatus.APPROVED);
        result = bookingRepository.existsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn(
                bookerId, itemId, LocalDateTime.now(), statusSet);
        assertTrue(result);
    }

    @Test
    void testFindPastBookingsByBooker_Id() {
        Long bookerId = user2.getId();
        Pageable page = PageRequest.of(0, 10);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        Page<Booking> pages = bookingRepository.findPastBookingsByBooker_Id(bookerId, page);
        assertEquals(1, pages.getContent().size());
        assertEquals(booking1, pages.getContent().get(0));

        page = PageRequest.of(1, 1);
        pages = bookingRepository.findPastBookingsByBooker_Id(bookerId, page);
        assertEquals(0, pages.getContent().size());
    }

    @Test
    void testFindPastBookingsByOwner_Id() {
        Long ownerId = user1.getId();
        Pageable page = PageRequest.of(0, 10);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        Page<Booking> pages = bookingRepository.findPastBookingsByOwner_Id(ownerId, page);
        assertEquals(1, pages.getContent().size());
        assertEquals(booking1, pages.getContent().get(0));

        page = PageRequest.of(1, 1);
        pages = bookingRepository.findPastBookingsByOwner_Id(ownerId, page);
        assertEquals(0, pages.getContent().size());
    }

    @Test
    void testFindCurrentBookingsByBooker_Id() {
        Long bookerId = user2.getId();
        Pageable page = PageRequest.of(0, 10);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        Page<Booking> pages = bookingRepository.findCurrentBookingsByBooker_Id(bookerId, page);
        assertEquals(1, pages.getContent().size());
        assertEquals(booking3, pages.getContent().get(0));

        page = PageRequest.of(1, 1);
        pages = bookingRepository.findCurrentBookingsByBooker_Id(bookerId, page);
        assertEquals(0, pages.getContent().size());
    }

    @Test
    void testFindCurrentBookingsByOwner_Id() {
        Long ownerId = user1.getId();
        Pageable page = PageRequest.of(0, 10);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        Page<Booking> pages = bookingRepository.findPastBookingsByOwner_Id(ownerId, page);
        assertEquals(1, pages.getContent().size());
        assertEquals(booking1, pages.getContent().get(0));

        page = PageRequest.of(1, 1);
        pages = bookingRepository.findPastBookingsByOwner_Id(ownerId, page);
        assertEquals(0, pages.getContent().size());
    }

    @Test
    void testFindByOwner_IdAndStatusIn() {
        Set<BookingStatus> statusSet = Set.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        Pageable page = PageRequest.of(0, 2);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        Page<Booking> pages = bookingRepository.findByOwner_IdAndStatusIn(user1.getId(), statusSet, page);
        assertEquals(2, pages.getContent().size());
        assertEquals(booking4, pages.getContent().get(0));
    }

    @Test
    void testFindLastBookingsByItem_IdsAndStatusNotIn() {
        Set<BookingStatus> statusSet = Set.of(BookingStatus.REJECTED, BookingStatus.CANCELED);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        BookingShort bookingShort1 = makeBookingShort(booking1);
        BookingShort bookingShort3 = makeBookingShort(booking3);

        List<BookingShort> pages = bookingRepository.findLastBookingsByItemIdsInAndStatusNotIn(
                Set.of(item1.getId()), statusSet);
        assertEquals(1, pages.size());
        assertEquals(bookingShort1, pages.get(0));

        pages = bookingRepository.findLastBookingsByItemIdsInAndStatusNotIn(
                Set.of(item1.getId(), item2.getId()), statusSet);
        assertEquals(2, pages.size());
    }

    @Test
    void testFindNextBookingsByItem_IdsAndStatusNotIn() {
        Set<BookingStatus> statusSet = Set.of(BookingStatus.WAITING);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        BookingShort bookingShort2 = makeBookingShort(booking2);
        BookingShort bookingShort4 = makeBookingShort(booking4);

        List<BookingShort> pages = bookingRepository.findNextBookingsByItemIdsInAndStatusNotIn(
                Set.of(item1.getId()), statusSet);
        assertEquals(1, pages.size());
        assertEquals(bookingShort2, pages.get(0));

        pages = bookingRepository.findNextBookingsByItemIdsInAndStatusNotIn(
                Set.of(item1.getId(), item2.getId()), statusSet);
        assertEquals(2, pages.size());
        assertEquals(bookingShort2, pages.get(0));
    }

    @Test
    void testFindLastBookingsByItem_IdAndStatusNotIn() {
        Set<BookingStatus> statusSet = Set.of(BookingStatus.REJECTED, BookingStatus.CANCELED);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        BookingShort bookingShort1 = makeBookingShort(booking1);
        BookingShort bookingShort3 = makeBookingShort(booking3);

        List<BookingShort> pages = bookingRepository.findLastBookingsByItemIdAndStatusNotIn(
                item1.getId(), statusSet);
        assertEquals(1, pages.size());
        assertEquals(bookingShort1, pages.get(0));

        pages = bookingRepository.findLastBookingsByItemIdAndStatusNotIn(item2.getId(), statusSet);
        assertEquals(1, pages.size());
        assertEquals(bookingShort3, pages.get(0));
    }

    @Test
    void testFindNextBookingsByItem_IdAndStatusNotIn() {
        Set<BookingStatus> statusSet = Set.of(BookingStatus.WAITING);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        BookingShort bookingShort2 = makeBookingShort(booking2);
        BookingShort bookingShort4 = makeBookingShort(booking4);

        List<BookingShort> pages = bookingRepository.findNextBookingsByItemIdAndStatusNotIn(
                item1.getId(), statusSet);
        assertEquals(1, pages.size());
        assertEquals(bookingShort2, pages.get(0));

        pages = bookingRepository.findNextBookingsByItemIdAndStatusNotIn(item2.getId(), statusSet);
        assertEquals(1, pages.size());
        assertEquals(bookingShort4, pages.get(0));
    }

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, Item item, BookingStatus status) {
        return Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(user2)
                .status(status)
                .build();
    }

    private BookingShort makeBookingShort(Booking booking) {
        return BookingShort.builder()
                .itemId(booking.getItem().getId())
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    private Item makeItem(String name) {
        return Item.builder()
                .name(name)
                .description("Аккумуляторная отвертка")
                .available(true)
                .owner(user1)
                .request(null)
                .build();
    }


    private User makeUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }
}