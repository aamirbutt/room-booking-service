package com.example.booking.booking;

import java.time.Instant;

public record BookingResponse(long id, long roomId, String roomName, long userId, Instant start, Instant end) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getRoom().getName(),
                booking.getUserId(),
                booking.getStartTime(),
                booking.getEndTime());
    }
}
