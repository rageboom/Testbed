package com.roombook.api.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.roombook.api.application.ReservationView;
import com.roombook.api.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        long id,
        long roomId,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        String title,
        int attendeeCount,
        String reservedBy,
        String status,
        boolean mine,
        boolean cancellable) {

    static ReservationResponse from(ReservationView view) {
        Reservation reservation = view.reservation();
        return new ReservationResponse(reservation.getId(), reservation.getRoomId(), reservation.getDate(),
                reservation.getStartTime(), reservation.getEndTime(), reservation.getTitle(),
                reservation.getAttendeeCount(), reservation.getReservedBy(), reservation.getStatus().name(),
                view.mine(), view.cancellable());
    }
}
