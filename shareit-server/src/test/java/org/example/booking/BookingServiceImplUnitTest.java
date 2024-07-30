package org.example.booking;

import org.example.booking.model.Booking;
import org.example.booking.model.BookingDto;
import org.example.exception.*;
import org.example.item.ItemRepository;
import org.example.item.model.Item;
import org.example.user.UserRepository;
import org.example.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class BookingServiceImplUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        booking = makeBooking();
        bookingDto = BookingMapper.mapToBookingDto(booking);
    }

    @Test
    public void testCreateNotFound() {
        assertThrows(UserNotFoundException.class, () -> bookingService.create(1L, bookingDto));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getBooker()));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> bookingService.create(1L, bookingDto));

        booking.getItem().setAvailable(false);
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getItem()));
        assertThrows(ItemBadRequestException.class, () -> bookingService.create(1L, bookingDto));

        booking.getItem().setAvailable(true);
        assertThrows(BookingNotFoundException.class, () -> bookingService.create(2L, bookingDto));
        Mockito.verify(bookingRepository, Mockito.never())
                .save(any(Booking.class));
    }

    @Test
    public void testCreate() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getBooker()));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getItem()));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        Booking resultBooking = bookingService.create(1L, bookingDto);

        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(any(Booking.class));

        assertEquals(booking.getId(), resultBooking.getId());
        assertEquals(booking.getStatus(), resultBooking.getStatus());
        assertEquals(bookingDto.getItemId(), resultBooking.getItem().getId());
        assertEquals(bookingDto.getStart(), resultBooking.getStart());
        assertEquals(bookingDto.getEnd(), resultBooking.getEnd());
    }

    @Test
    public void testChangeStatusNotFound() {
        assertThrows(UserNotFoundException.class, () -> bookingService.changeStatus(
                2L, 1L, false));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getBooker()));
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> bookingService.changeStatus(
                2L, 1L, false));

        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        assertThrows(OwnerNotFoundException.class, () -> bookingService.changeStatus(
                1L, 1L, false));

        assertThrows(BookingStateBadRequestException.class, () -> bookingService.changeStatus(
                2L, 1L, true));
        Mockito.verify(bookingRepository, Mockito.never())
                .save(any(Booking.class));
    }

    @Test
    public void testChangeStatus() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getBooker()));
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        Booking resultBooking = bookingService.changeStatus(2L, 1L, false);

        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(any(Booking.class));
        assertEquals(booking.getId(), resultBooking.getId());
        assertEquals(BookingStatus.REJECTED, resultBooking.getStatus());
        assertEquals(bookingDto.getItemId(), resultBooking.getItem().getId());
        assertEquals(bookingDto.getStart(), resultBooking.getStart());
        assertEquals(bookingDto.getEnd(), resultBooking.getEnd());
    }

    @Test
    public void testGetByIdNotFound() {
        assertThrows(UserNotFoundException.class, () -> bookingService.getById(2L, 1L));

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getBooker()));
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> bookingService.getById(2L, 1L));

        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        assertThrows(OwnerNotFoundException.class, () -> bookingService.getById(3L, 1L));
    }

    @Test
    public void testGetById() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getBooker()));
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        Booking resultBooking = bookingService.getById(2L, 1L);

        assertEquals(booking.getId(), resultBooking.getId());
        assertEquals(booking.getStatus(), resultBooking.getStatus());
        assertEquals(bookingDto.getItemId(), resultBooking.getItem().getId());
        assertEquals(bookingDto.getStart(), resultBooking.getStart());
        assertEquals(bookingDto.getEnd(), resultBooking.getEnd());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(any(Long.class));
    }

    @Test
    public void testGetAllByOwnerNotFound() {
        assertThrows(UserNotFoundException.class, () -> bookingService.getAllByOwner(
                2L, BookingState.ALL, 0, 1));
    }

    @Test
    public void testGetAllByOwner() {
        List<Booking> bookingList = List.of(booking);
        Page<Booking> emptyPage = new PageImpl<>(bookingList, PageRequest.of(3, 1), bookingList.size());
        Page<Booking> bookingPage = new PageImpl<>(bookingList, PageRequest.of(0, 1), bookingList.size());

        //BookingState.PAST
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(booking.getBooker()));
        Mockito.when(bookingRepository.findPastBookingsByOwner_Id(
                Mockito.any(), Mockito.any())).thenReturn(bookingPage);
        Mockito.when(bookingRepository.findCurrentBookingsByOwner_Id(
                Mockito.any(), Mockito.any())).thenReturn(emptyPage);
        Mockito.when(bookingRepository.findByOwner_IdAndStatusIn(
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(emptyPage);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        List<Booking> resultBookings = bookingService.getAllByOwner(2L, BookingState.PAST, 0, 1);

        assertEquals(bookingList.size(), resultBookings.size());
        assertEquals(booking.getId(), resultBookings.get(0).getId());
        assertEquals(bookingDto.getStatus(), resultBookings.get(0).getStatus());
        assertEquals(bookingDto.getItemId(), resultBookings.get(0).getItem().getId());
        assertEquals(bookingDto.getStart(), resultBookings.get(0).getStart());
        assertEquals(bookingDto.getEnd(), resultBookings.get(0).getEnd());

        //BookingState.CURRENT
        Mockito.when(bookingRepository.findPastBookingsByOwner_Id(
                Mockito.any(), Mockito.any())).thenReturn(emptyPage);
        Mockito.when(bookingRepository.findCurrentBookingsByOwner_Id(
                Mockito.any(), Mockito.any())).thenReturn(bookingPage);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        resultBookings = bookingService.getAllByOwner(2L, BookingState.CURRENT, 0, 1);

        assertEquals(bookingList.size(), resultBookings.size());
        assertEquals(booking.getId(), resultBookings.get(0).getId());
        assertEquals(bookingDto.getStatus(), resultBookings.get(0).getStatus());
        assertEquals(bookingDto.getItemId(), resultBookings.get(0).getItem().getId());
        assertEquals(bookingDto.getStart(), resultBookings.get(0).getStart());
        assertEquals(bookingDto.getEnd(), resultBookings.get(0).getEnd());

        //BookingState.WAITING
        Mockito.when(bookingRepository.findCurrentBookingsByOwner_Id(
                Mockito.any(), Mockito.any())).thenReturn(emptyPage);
        Mockito.when(bookingRepository.findByOwner_IdAndStatusIn(
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(bookingPage);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        resultBookings = bookingService.getAllByOwner(2L, BookingState.WAITING, 0, 1);

        assertEquals(bookingList.size(), resultBookings.size());
        assertEquals(booking.getId(), resultBookings.get(0).getId());
        assertEquals(bookingDto.getStatus(), resultBookings.get(0).getStatus());
        assertEquals(bookingDto.getItemId(), resultBookings.get(0).getItem().getId());
        assertEquals(bookingDto.getStart(), resultBookings.get(0).getStart());
        assertEquals(bookingDto.getEnd(), resultBookings.get(0).getEnd());

        //BookingState.REJECTED
        Mockito.when(bookingRepository.findCurrentBookingsByOwner_Id(
                Mockito.any(), Mockito.any())).thenReturn(emptyPage);
        Mockito.when(bookingRepository.findByOwner_IdAndStatusIn(
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(bookingPage);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        resultBookings = bookingService.getAllByOwner(2L, BookingState.REJECTED, 0, 1);

        assertEquals(bookingList.size(), resultBookings.size());
        assertEquals(booking.getId(), resultBookings.get(0).getId());
        assertEquals(bookingDto.getStatus(), resultBookings.get(0).getStatus());
        assertEquals(bookingDto.getItemId(), resultBookings.get(0).getItem().getId());
        assertEquals(bookingDto.getStart(), resultBookings.get(0).getStart());
        assertEquals(bookingDto.getEnd(), resultBookings.get(0).getEnd());

        //BookingState.FUTURE
        Mockito.when(bookingRepository.findCurrentBookingsByOwner_Id(
                Mockito.any(), Mockito.any())).thenReturn(emptyPage);
        Mockito.when(bookingRepository.findByOwner_IdAndStatusIn(
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(bookingPage);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        resultBookings = bookingService.getAllByOwner(2L, BookingState.FUTURE, 0, 1);

        assertEquals(bookingList.size(), resultBookings.size());
        assertEquals(booking.getId(), resultBookings.get(0).getId());
        assertEquals(bookingDto.getStatus(), resultBookings.get(0).getStatus());
        assertEquals(bookingDto.getItemId(), resultBookings.get(0).getItem().getId());
        assertEquals(bookingDto.getStart(), resultBookings.get(0).getStart());
        assertEquals(bookingDto.getEnd(), resultBookings.get(0).getEnd());

        //BookingState.ALL
        Mockito.when(bookingRepository.findCurrentBookingsByOwner_Id(
                Mockito.any(), Mockito.any())).thenReturn(emptyPage);
        Mockito.when(bookingRepository.findByOwner_IdAndStatusIn(
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(bookingPage);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        resultBookings = bookingService.getAllByOwner(2L, BookingState.ALL, 0, 1);

        assertEquals(bookingList.size(), resultBookings.size());
        assertEquals(booking.getId(), resultBookings.get(0).getId());
        assertEquals(bookingDto.getStatus(), resultBookings.get(0).getStatus());
        assertEquals(bookingDto.getItemId(), resultBookings.get(0).getItem().getId());
        assertEquals(bookingDto.getStart(), resultBookings.get(0).getStart());
        assertEquals(bookingDto.getEnd(), resultBookings.get(0).getEnd());
    }

    private Booking makeBooking() {
        User itemOwner = User.builder()
                .id(2L)
                .name("Пётр")
                .email("user@user.com")
                .build();

        User itemBooker = User.builder()
                .id(1L)
                .name("Пъедро")
                .email("other@other.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Отвёртка")
                .description("Аккумуляторная отвертка")
                .available(true)
                .owner(itemOwner)
                .request(null)
                .build();

        return Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .item(item)
                .booker(itemBooker)
                .status(BookingStatus.APPROVED)
                .build();
    }
}
