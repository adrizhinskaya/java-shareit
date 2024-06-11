package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingShort;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findAllByBooker_IdAndStatusIn(Long bookerId, Set<BookingStatus> status, Sort sort);
    boolean existsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn(Long bookerId, Long itemId, LocalDateTime now, @Param("states") Set<BookingStatus> states);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status IN :states order by b.start desc")
    Collection<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId, @Param("states") Set<BookingStatus> states);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.end DESC")
    Collection<Booking> findPastBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.end DESC")
    Collection<Booking> findPastBookingsByBooker_Id(@Param("bookerId") Long bookerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.start < CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP " +
            "ORDER BY b.start ASC")
    Collection<Booking> findCurrentBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.start < CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP " +
            "ORDER BY b.start ASC")
    Collection<Booking> findCurrentBookingsByBooker_Id(@Param("bookerId") Long bookerId);

    @Query("SELECT new ru.practicum.shareit.booking.model.BookingShort(b.id, b.booker.id) " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.status NOT IN :states AND b.start < CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    Collection<BookingShort> findAllByItem_IdFromStartAsc(@Param("itemId") Long itemId, @Param("states") Set<BookingStatus> states);

    @Query("SELECT new ru.practicum.shareit.booking.model.BookingShort(b.id, b.booker.id ) " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.status NOT IN :states AND b.start > CURRENT_TIMESTAMP " +
            "ORDER BY b.start ASC")
    Collection<BookingShort> findAllByItem_IdAfterNowAsc(@Param("itemId") Long itemId, @Param("states") Set<BookingStatus> states);
}
