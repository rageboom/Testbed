package com.roombook.api.application;

import java.time.LocalDate;
import java.time.LocalTime;

/** 예약 생성 입력. */
public record ReservationCommand(LocalDate date, LocalTime startTime, LocalTime endTime, String title,
        int attendeeCount) {
}
