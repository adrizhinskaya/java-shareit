package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    private User userExistCheck(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Для операций бронирования нужно создать пользователя"));
    }

    @Override
    public Booking create(Long bookerId, BookingDto bookingDto) {
        User user = userExistCheck(bookerId);
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() ->
                new ItemNotFoundException("Вещь не найдена"));
        if (!item.getAvailable()) {
            throw new ItemBadRequestException("Попытка бронирования недоступной вещи");
        }
        if (bookerId.equals(item.getOwner().getId())) {
            throw new BookingNotFoundException("Владелец вещи не может её бронировать");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(BookingMapper.mapToBooking(bookingDto, item, user));
    }

    @Override
    public Booking changeStatus(Long itemOwnerId, Long bookingId, boolean approved) {
        userExistCheck(itemOwnerId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BookingNotFoundException("Бронирование не найдено"));
        if (!itemOwnerId.equals(booking.getItem().getOwner().getId())) {
            throw new OwnerNotFoundException(
                    "Попытка смены статуса бронирования от пользователя НЕ являющегося владельцем вещи");
        }
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        if (booking.getStatus().equals(status)) {
            throw new BookingStateBadRequestException("Такой статус уже присвоен");
        }
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long userId, Long bookingId) {
        userExistCheck(userId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BookingNotFoundException("Бронирование не найдено"));
        if (userId.equals(booking.getItem().getOwner().getId()) || userId.equals(booking.getBooker().getId())) {
            return booking;
        }
        throw new OwnerNotFoundException(
                "Попытка просмотра бронирования от пользователя НЕ являющегося owner или booker");
    }

    @Override
    public List<Booking> getAllByBooker(Long bookerId, BookingState state, int from, int size) {
        userExistCheck(bookerId);
        Pageable page = PageRequest.of(from / size, size);
        Set<BookingStatus> statusSet = new HashSet<>();
        switch (state) {
            case PAST:
                return bookingRepository.findPastBookingsByBooker_Id(bookerId, page).getContent();
            case CURRENT:
                return bookingRepository.findCurrentBookingsByBooker_Id(bookerId, page).getContent();
            default:
                stateSwitch(state, statusSet);
        }
        return bookingRepository.findAllByBooker_IdAndStatusInOrderByStartDesc(bookerId, statusSet, page).getContent();
    }

    @Override
    public List<Booking> getAllByOwner(Long ownerId, BookingState state, int from, int size) {
        userExistCheck(ownerId);
        Pageable page = PageRequest.of(from / size, size);
        Set<BookingStatus> statusSet = new HashSet<>();
        switch (state) {
            case PAST:
                return bookingRepository.findPastBookingsByOwner_Id(ownerId, page).getContent();
            case CURRENT:
                return bookingRepository.findCurrentBookingsByOwner_Id(ownerId, page).getContent();
            default:
                stateSwitch(state, statusSet);
        }
        return bookingRepository.findByOwner_IdAndStatusIn(ownerId, statusSet, page).getContent();
    }

    private void stateSwitch(BookingState state, Set<BookingStatus> statusSet) {
        switch (state) {
            case WAITING:
                statusSet.add(BookingStatus.WAITING);
                break;
            case REJECTED:
                statusSet.add(BookingStatus.REJECTED);
                break;
            case FUTURE:
                statusSet.add(BookingStatus.APPROVED);
                statusSet.add(BookingStatus.WAITING);
                break;
            case ALL:
                statusSet.add(BookingStatus.WAITING);
                statusSet.add(BookingStatus.APPROVED);
                statusSet.add(BookingStatus.REJECTED);
                statusSet.add(BookingStatus.CANCELED);
                break;
            default:
                throw new ItemNotFoundException("Unsupported state");
        }
    }
}