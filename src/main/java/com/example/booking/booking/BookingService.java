package com.example.booking.booking;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.example.booking.api.ConflictException;
import com.example.booking.api.InvalidRequestException;
import com.example.booking.api.NotFoundException;
import com.example.booking.room.RoomRepository;
import com.example.booking.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    static final Duration MAX_BOOKING_DURATION = Duration.ofHours(12);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse create(long userId, CreateBookingRequest request) {
        requireUser(userId);
        requireValidWindow(request.start(), request.end());
        var room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new NotFoundException("Room " + request.roomId() + " not found"));
        if (bookingRepository.overlaps(room.getId(), request.start(), request.end())) {
            throw new ConflictException("Room " + room.getName() + " is already booked in that window");
        }
        var booking = bookingRepository.save(new Booking(room, userId, request.start(), request.end()));
        log.info("Booking {} created in room {} by user {}", booking.getId(), room.getId(), userId);
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(BookingResponse::from)
                .orElseThrow(() -> new NotFoundException("Booking " + bookingId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> forRoom(long roomId, Instant from, Instant to) {
        if (!roomRepository.existsById(roomId)) {
            throw new NotFoundException("Room " + roomId + " not found");
        }
        return bookingRepository.findForRoom(roomId, from, to).stream().map(BookingResponse::from).toList();
    }

    private void requireUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User " + userId + " not found");
        }
    }

    private static void requireValidWindow(Instant start, Instant end) {
        if (!start.isBefore(end)) {
            throw new InvalidRequestException("Booking start must be before its end");
        }
        if (Duration.between(start, end).compareTo(MAX_BOOKING_DURATION) > 0) {
            throw new InvalidRequestException("A booking cannot be longer than " + MAX_BOOKING_DURATION.toHours() + " hours");
        }
    }
}
