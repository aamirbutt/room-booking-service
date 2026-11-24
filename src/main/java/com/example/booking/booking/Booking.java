package com.example.booking.booking;

import java.time.Instant;

import com.example.booking.room.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.ACTIVE;

    protected Booking() {
    }

    public Booking(Room room, Long userId, Instant startTime, Instant endTime) {
        this.room = room;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }
}
