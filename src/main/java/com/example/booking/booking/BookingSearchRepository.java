package com.example.booking.booking;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class BookingSearchRepository {

    private final EntityManager entityManager;

    public BookingSearchRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<Booking> search(String roomName, Instant from, Instant to) {
        var sql = "SELECT b.* FROM booking b JOIN room r ON r.id = b.room_id"
                + " WHERE b.status = 'ACTIVE' AND b.start_time < :to AND b.end_time > :from";
        if (roomName != null && !roomName.isBlank()) {
            sql += " AND LOWER(r.name) LIKE '%" + roomName.toLowerCase() + "%'";
        }
        sql += " ORDER BY b.start_time";
        return entityManager.createNativeQuery(sql, Booking.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }
}
