package com.example.booking.booking;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select count(b) > 0 from Booking b
            where b.room.id = :roomId
              and b.status = com.example.booking.booking.BookingStatus.ACTIVE
              and b.startTime >= :start
              and b.startTime < :end
            """)
    boolean hasActiveBookingBetween(
            @Param("roomId") long roomId, @Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select b from Booking b
            where b.room.id = :roomId
              and b.status = com.example.booking.booking.BookingStatus.ACTIVE
              and b.startTime < :to
              and b.endTime > :from
            order by b.startTime
            """)
    List<Booking> findForRoom(@Param("roomId") long roomId, @Param("from") Instant from, @Param("to") Instant to);
}
