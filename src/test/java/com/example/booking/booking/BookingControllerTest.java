package com.example.booking.booking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingControllerTest {

    private static final long ALICE = 1;
    private static final long BOB = 2;
    private static final long ATLAS = 1;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsBooking() throws Exception {
        book(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomName").value("Atlas"))
                .andExpect(jsonPath("$.userId").value(ALICE))
                .andExpect(jsonPath("$.start").value("2030-01-07T10:00:00Z"))
                .andExpect(jsonPath("$.end").value("2030-01-07T11:00:00Z"));
    }

    @Test
    void rejectsOverlappingBooking() throws Exception {
        book(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z").andExpect(status().isCreated());

        book(BOB, ATLAS, "2030-01-07T09:30:00Z", "2030-01-07T10:30:00Z")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Room Atlas is already booked in that window"));
    }

    @Test
    void allowsBackToBackBookings() throws Exception {
        book(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z").andExpect(status().isCreated());

        book(BOB, ATLAS, "2030-01-07T11:00:00Z", "2030-01-07T12:00:00Z").andExpect(status().isCreated());
    }

    @Test
    void rejectsBookingThatEndsBeforeItStarts() throws Exception {
        book(ALICE, ATLAS, "2030-01-07T11:00:00Z", "2030-01-07T10:00:00Z")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Booking start must be before its end"));
    }

    @Test
    void rejectsBookingLongerThanTwelveHours() throws Exception {
        book(ALICE, ATLAS, "2030-01-07T06:00:00Z", "2030-01-07T19:00:00Z")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A booking cannot be longer than 12 hours"));
    }

    @Test
    void rejectsUnknownUser() throws Exception {
        book(999, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User 999 not found"));
    }

    @Test
    void listsBookingsForRoomInWindow() throws Exception {
        book(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z").andExpect(status().isCreated());
        book(BOB, ATLAS, "2030-01-08T10:00:00Z", "2030-01-08T11:00:00Z").andExpect(status().isCreated());

        mockMvc.perform(get("/rooms/{roomId}/bookings", ATLAS)
                        .param("from", "2030-01-07T00:00:00Z")
                        .param("to", "2030-01-08T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(ALICE));
    }

    private ResultActions book(long userId, long roomId, String start, String end) throws Exception {
        return mockMvc.perform(post("/bookings")
                .header(BookingController.USER_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"roomId": %d, "start": "%s", "end": "%s"}
                        """.formatted(roomId, start, end)));
    }
}
