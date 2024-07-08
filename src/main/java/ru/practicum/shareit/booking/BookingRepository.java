package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingShort;

import java.time.LocalDateTime;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBooker_IdAndStatusInOrderByStartDesc(Long bookerId,
                                                                Set<BookingStatus> statusSet,
                                                                Pageable pageable);

    boolean existsBookingByBooker_IdAndItem_IdAndEndBeforeAndStatusNotIn(Long bookerId,
                                                                         Long itemId,
                                                                         LocalDateTime now,
                                                                         Set<BookingStatus> statusSet);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.end DESC")
    Page<Booking> findPastBookingsByBooker_Id(@Param("bookerId") Long bookerId,
                                              Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.end DESC")
    Page<Booking> findPastBookingsByOwner_Id(@Param("ownerId") Long ownerId,
                                             Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.start < CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP " +
            "ORDER BY b.start ASC")
    Page<Booking> findCurrentBookingsByBooker_Id(@Param("bookerId") Long bookerId,
                                                 Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.start < CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP " +
            "ORDER BY b.start ASC")
    Page<Booking> findCurrentBookingsByOwner_Id(@Param("ownerId") Long ownerId,
                                                Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.status IN :statuses " +
            "ORDER BY b.start DESC")
    Page<Booking> findByOwner_IdAndStatusIn(@Param("ownerId") Long ownerId,
                                            @Param("statuses") Set<BookingStatus> statusSet,
                                            Pageable pageable);


    @Query("SELECT new ru.practicum.shareit.booking.model.BookingShort(b.id, b.booker.id) " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.status NOT IN :statuses AND b.start < CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    Page<BookingShort> findLastBookingsByItem_IdAndStatusNotIn(@Param("itemId") Long itemId,
                                                               @Param("statuses") Set<BookingStatus> statusSet,
                                                               Pageable pageable);

    @Query("SELECT new ru.practicum.shareit.booking.model.BookingShort(b.id, b.booker.id ) " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.status NOT IN :statuses AND b.start > CURRENT_TIMESTAMP " +
            "ORDER BY b.start ASC")
    Page<BookingShort> findNextBookingsByItem_IdAndStatusNotIn(@Param("itemId") Long itemId,
                                                               @Param("statuses") Set<BookingStatus> statusSet,
                                                               Pageable pageable);
}
