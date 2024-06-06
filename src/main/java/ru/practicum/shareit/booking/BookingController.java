package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    @PostMapping
    public void create() {

    }

    @PatchMapping("/{bookingId}?approved={approved}")
    public void changeStatus() {

    }

    @GetMapping("/{bookingId}")
    public void getById() {

    }

    @GetMapping("?state={state}")
    public void getAll() {

    }

    @GetMapping("/owner?state={state}")
    public void getAllByOwner() {

    }
}
