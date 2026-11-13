package com.roombook.api.web;

import com.roombook.api.domain.Room;

public record RoomResponse(long id, String name, int floor, int capacity) {

    static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getFloor(), room.getCapacity());
    }
}
