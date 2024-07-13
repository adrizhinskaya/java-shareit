package ru.practicum.shareit.booking;

import io.vavr.Function3;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import java.util.function.BiFunction;

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
                return callFunctionWithPagination(
                        from, size, bookingRepository::findPastBookingsByBooker_Id, bookerId);
            case CURRENT:
                return callFunctionWithPagination(
                        from, size, bookingRepository::findCurrentBookingsByBooker_Id, bookerId);
            default:
                stateSwitch(state, statusSet);
        }
        return callFunctionWithPagination(
                from, size, bookingRepository::findAllByBooker_IdAndStatusInOrderByStartDesc, bookerId, statusSet);
    }

    @Override
    public List<Booking> getAllByOwner(Long ownerId, BookingState state, int from, int size) {
        userExistCheck(ownerId);
        Set<BookingStatus> statusSet = new HashSet<>();
        switch (state) {
            case PAST:
                return callFunctionWithPagination(
                        from, size, bookingRepository::findPastBookingsByOwner_Id, ownerId);
            case CURRENT:
                return callFunctionWithPagination(
                        from, size, bookingRepository::findCurrentBookingsByOwner_Id, ownerId);
            default:
                stateSwitch(state, statusSet);
        }
        return callFunctionWithPagination(from, size, bookingRepository::findByOwner_IdAndStatusIn, ownerId, statusSet);
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
        }
    }

    private List<Booking> callFunctionWithPagination(int from, int size, BiFunction<Long,
            Pageable, Page<Booking>> repositoryMethod, Long userId) {
        if (from % size == 0) {
            return repositoryMethod.apply(userId, PageRequest.of(from / size, size)).getContent();
        }

        int startPage = from / size;
        double nextPageElPercent = (double) from / size - startPage;
        int countOfNextPageEl = (int) Math.ceil(nextPageElPercent * size);
        int countOfStartPageEl = size - countOfNextPageEl;

        Pageable page = PageRequest.of(startPage, size);
        Page<Booking> bookingsPage = repositoryMethod.apply(userId, page);
        List<Booking> itemsList = bookingsPage.getContent();
        List<Booking> startPageBookings = itemsList.subList(itemsList.size() - countOfStartPageEl,
                itemsList.size() - 1);

        if (bookingsPage.hasNext()) {
            page = PageRequest.of(startPage + 1, size);
            bookingsPage = repositoryMethod.apply(userId, page);
            List<Booking> nextPageItems = bookingsPage.getContent().subList(0, countOfNextPageEl - 1);
            startPageBookings.addAll(nextPageItems);
        }
        return startPageBookings;
    }

    private List<Booking> callFunctionWithPagination(int from, int size, Function3<Long,
            Set<BookingStatus>, Pageable, Page<Booking>> repositoryMethod, Long userId, Set<BookingStatus> statusSet) {
        if (from % size == 0) {
            return repositoryMethod.apply(userId, statusSet, PageRequest.of(from / size, size)).getContent();
        }

        int startPage = from / size;
        double nextPageElPercent = (double) from / size - startPage;
        int countOfNextPageEl = (int) Math.ceil(nextPageElPercent * size);
        int countOfStartPageEl = size - countOfNextPageEl;

        Pageable page = PageRequest.of(startPage, size);
        Page<Booking> bookingsPage = repositoryMethod.apply(userId, statusSet, page);
        List<Booking> itemsList = bookingsPage.getContent();
        List<Booking> startPageBookings = itemsList.subList(itemsList.size() - countOfStartPageEl,
                itemsList.size() - 1);

        if (bookingsPage.hasNext()) {
            page = PageRequest.of(startPage + 1, size);
            bookingsPage = repositoryMethod.apply(userId, statusSet, page);
            List<Booking> nextPageItems = bookingsPage.getContent().subList(0, countOfNextPageEl - 1);
            startPageBookings.addAll(nextPageItems);
        }

        return startPageBookings;
    }
}