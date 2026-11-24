package com.example.booking.room;

import java.util.List;

import com.example.booking.api.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping
    public List<RoomResponse> list() {
        return roomRepository.findAll().stream().map(RoomResponse::from).toList();
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable long id) {
        return roomRepository.findById(id)
                .map(RoomResponse::from)
                .orElseThrow(() -> new NotFoundException("Room " + id + " not found"));
    }
}
