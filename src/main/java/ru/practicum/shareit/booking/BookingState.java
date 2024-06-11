package ru.practicum.shareit.booking;

public enum BookingState {
    ALL,
    CURRENT,// текущие
    PAST,// завершённые                APPROVED, REJECTED, CANCELED
    FUTURE,//  будущие                 WAITING, APPROVED
    WAITING,// ожидающие подтверждения WAITING
    REJECTED//отклонённые              REJECTED
}
