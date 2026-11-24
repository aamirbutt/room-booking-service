package com.example.booking.booking;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Long roomId,
        @NotNull Instant start,
        @NotNull Instant end) {
}
