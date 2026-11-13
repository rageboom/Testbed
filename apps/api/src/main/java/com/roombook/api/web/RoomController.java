package com.roombook.api.web;

import com.roombook.api.persistence.RoomRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomController {

    private final RoomRepository rooms;

    public RoomController(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @GetMapping("/api/rooms")
    public List<RoomResponse> listRooms() {
        return rooms.findAllByOrderByIdAsc().stream().map(RoomResponse::from).toList();
    }
}
