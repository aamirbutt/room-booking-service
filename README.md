# room-booking-service

A small REST service for booking meeting rooms.

## Model

- **Room** — a bookable meeting room.
- **User** — an employee. Requests are authenticated by the API gateway, which forwards the
  caller's id in the `X-User-Id` header.
- **Booking** — a room reserved by a user for a time window. Bookings in the same room may not
  overlap; back-to-back bookings are fine. A booking lasts at most 12 hours.

Every employee can see every room's calendar. Only the person who made a booking may change it.

All times are UTC instants (ISO-8601, e.g. `2030-01-07T10:00:00Z`).

## API

| Method | Path | Description |
|---|---|---|
| `GET` | `/rooms` | List rooms |
| `GET` | `/rooms/{id}` | Get a room |
| `GET` | `/rooms/{roomId}/bookings?from=&to=` | Active bookings in a room that overlap a window |
| `POST` | `/bookings` | Book a room: `{"roomId", "start", "end"}` |
| `POST` | `/bookings/recurring` | Book the same slot weekly: `{"roomId", "start", "end", "weeks"}` |
| `GET` | `/bookings/{id}` | Get a booking |
| `DELETE` | `/bookings/{id}` | Cancel a booking |
| `GET` | `/bookings/search?room=&from=&to=` | Active bookings overlapping a window, optionally filtered by room name |

## Running

Requires Java 21.

```sh
mvn spring-boot:run
mvn verify
```

The service uses an in-memory H2 database seeded by Flyway (`src/main/resources/db/migration`).
