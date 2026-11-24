package com.example.booking.booking;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record CreateRecurringBookingRequest(
        @NotNull Long roomId,
        @NotNull Instant start,
        @NotNull Instant end,
        @NotNull Integer weeks) {
}
