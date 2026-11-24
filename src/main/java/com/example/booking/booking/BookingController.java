package com.example.booking.booking;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookingController {

    static final String USER_HEADER = "X-User-Id";

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @RequestHeader(USER_HEADER) long userId, @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(userId, request);
    }

    @GetMapping("/bookings/{id}")
    public BookingResponse get(@PathVariable long id) {
        return bookingService.get(id);
    }

    @GetMapping("/rooms/{roomId}/bookings")
    public List<BookingResponse> forRoom(
            @PathVariable long roomId, @RequestParam Instant from, @RequestParam Instant to) {
        return bookingService.forRoom(roomId, from, to);
    }
}
