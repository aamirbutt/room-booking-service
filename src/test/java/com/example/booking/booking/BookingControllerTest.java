package com.example.booking.booking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final long BOREALIS = 2;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void cancelsBooking() throws Exception {
        var id = bookingId(book(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z"));

        mockMvc.perform(delete("/bookings/{id}", id).header(BookingController.USER_HEADER, ALICE))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/bookings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelledSlotCanBeBookedAgain() throws Exception {
        var id = bookingId(book(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z"));
        mockMvc.perform(delete("/bookings/{id}", id).header(BookingController.USER_HEADER, ALICE))
                .andExpect(status().isNoContent());

        book(BOB, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(BOB));
    }

    @Test
    void cancellingUnknownBookingIsNotFound() throws Exception {
        mockMvc.perform(delete("/bookings/{id}", 999).header(BookingController.USER_HEADER, ALICE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Booking 999 not found"));
    }

    @Test
    void createsWeeklyBookings() throws Exception {
        bookWeekly(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z", 3)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].start").value("2030-01-07T10:00:00Z"))
                .andExpect(jsonPath("$[1].start").value("2030-01-14T10:00:00Z"))
                .andExpect(jsonPath("$[2].start").value("2030-01-21T10:00:00Z"))
                .andExpect(jsonPath("$[2].end").value("2030-01-21T11:00:00Z"));
    }

    @Test
    void weeklyBookingFailsWhenAnyWeekIsTaken() throws Exception {
        book(BOB, ATLAS, "2030-01-14T10:00:00Z", "2030-01-14T11:00:00Z").andExpect(status().isCreated());

        bookWeekly(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z", 3)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Room Atlas is already booked on 2030-01-14T10:00:00Z"));

        mockMvc.perform(get("/rooms/{roomId}/bookings", ATLAS)
                        .param("from", "2030-01-01T00:00:00Z")
                        .param("to", "2030-02-01T00:00:00Z"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(BOB));
    }

    @Test
    void searchesByRoomName() throws Exception {
        book(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z").andExpect(status().isCreated());
        book(BOB, BOREALIS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z").andExpect(status().isCreated());

        mockMvc.perform(get("/bookings/search")
                        .param("room", "bore")
                        .param("from", "2030-01-07T00:00:00Z")
                        .param("to", "2030-01-08T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].roomName").value("Borealis"))
                .andExpect(jsonPath("$[0].userId").value(BOB));
    }

    @Test
    void searchWithoutRoomReturnsAllRooms() throws Exception {
        book(ALICE, ATLAS, "2030-01-07T10:00:00Z", "2030-01-07T11:00:00Z").andExpect(status().isCreated());
        book(BOB, BOREALIS, "2030-01-07T12:00:00Z", "2030-01-07T13:00:00Z").andExpect(status().isCreated());

        mockMvc.perform(get("/bookings/search")
                        .param("from", "2030-01-07T00:00:00Z")
                        .param("to", "2030-01-08T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomName").value("Atlas"))
                .andExpect(jsonPath("$[1].roomName").value("Borealis"));
    }

    private long bookingId(ResultActions created) throws Exception {
        var body = created.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private ResultActions bookWeekly(long userId, long roomId, String start, String end, int weeks) throws Exception {
        return mockMvc.perform(post("/bookings/recurring")
                .header(BookingController.USER_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"roomId": %d, "start": "%s", "end": "%s", "weeks": %d}
                        """.formatted(roomId, start, end, weeks)));
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
