package com.vinaris.reservation_system;

import java.time.Instant;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;

public record Reservation(
        Long id,
        Long userId,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        ReservationStatus status

) {

    public Object getId() {
        return null;
    }

    public Object getRoomId() {
        return null;
    }

    public Instant getStartDate() {
        return null;
    }

    public ChronoLocalDate getEndDate() {
        return null;
    }
}
