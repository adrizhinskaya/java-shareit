package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashSet;
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
    public Booking create(Long userId, BookingDto bookingDto) {
        User user = userExistCheck(userId);
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
        if (!item.getAvailable()) {
            throw new ItemBadRequestException("Попытка бронирования недоступной вещи");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new BookingNotFoundException("Владелец вещи не может её бронировать");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(BookingMapper.mapToBooking(bookingDto, item, user));
    }

    @Override
    public Booking changeStatus(Long userId, Long bookingId, boolean approved) {
        userExistCheck(userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено"));
        if (!userId.equals(booking.getItem().getOwner().getId())) {
            throw new OwnerNotFoundException("Попытка смены статуса бронирования от пользователя " +
                    "НЕ являющегося владельцем");
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
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено"));
        if (userId.equals(booking.getItem().getOwner().getId()) || userId.equals(booking.getBooker().getId())) {
            return booking;
        }
        throw new OwnerNotFoundException("Попытка просмотра бронирования от пользователя " +
                "НЕ являющегося owner или booker");
    }

    @Override
    public Collection<Booking> getAllByBooker(Long bookerId, BookingState state) {
        userExistCheck(bookerId);

        Set<BookingStatus> stateSet = new HashSet<>();
        switch (state) {
            case WAITING:
                stateSet.add(BookingStatus.WAITING);
                break;
            case REJECTED:
                stateSet.add(BookingStatus.REJECTED);
                break;
            case FUTURE:
                stateSet.add(BookingStatus.APPROVED);
                stateSet.add(BookingStatus.WAITING);
                break;
            case PAST:
                return bookingRepository.findPastBookingsByBooker_Id(bookerId);
            case CURRENT:
                return bookingRepository.findCurrentBookingsByBooker_Id(bookerId);
            case ALL:
                stateSet.add(BookingStatus.WAITING);
                stateSet.add(BookingStatus.APPROVED);
                stateSet.add(BookingStatus.REJECTED);
                stateSet.add(BookingStatus.CANCELED);
                break;
            default:
                throw new ItemNotFoundException("Unsupported state");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        return bookingRepository.findAllByBooker_IdAndStatusIn(bookerId, stateSet, sort);
    }

    @Override
    public Collection<Booking> getAllByOwner(Long ownerId, BookingState state) {
        userExistCheck(ownerId);

        Set<BookingStatus> stateSet = new HashSet<>();
        switch (state) {
            case WAITING:
                stateSet.add(BookingStatus.WAITING);
                break;
            case REJECTED:
                stateSet.add(BookingStatus.REJECTED);
                break;
            case FUTURE:
                stateSet.add(BookingStatus.APPROVED);
                stateSet.add(BookingStatus.WAITING);
                break;
            case PAST:
                return bookingRepository.findPastBookingsByOwnerId(ownerId);
            case CURRENT:
                return bookingRepository.findCurrentBookingsByOwnerId(ownerId);
            case ALL:
                stateSet.add(BookingStatus.WAITING);
                stateSet.add(BookingStatus.APPROVED);
                stateSet.add(BookingStatus.REJECTED);
                stateSet.add(BookingStatus.CANCELED);
                break;
            default:
                throw new ItemNotFoundException("Unsupported state");
        }
        return bookingRepository.findAllByOwnerId(ownerId, stateSet);
    }
}