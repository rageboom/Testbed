package com.roombook.api.web;

import com.roombook.api.application.ReservationCommand;
import com.roombook.api.domain.RejectionCode;
import com.roombook.api.domain.RejectionException;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateReservationRequest(LocalDate date, LocalTime startTime, LocalTime endTime, String title,
        Integer attendeeCount) {

    /** 빠진 칸이 있으면 요청 형식 오류다. 제목이 비었는지는 업무 규칙(INVALID_TITLE)이 본다. */
    ReservationCommand toCommand() {
        if (date == null || startTime == null || endTime == null || attendeeCount == null) {
            throw new RejectionException(RejectionCode.INVALID_REQUEST);
        }
        return new ReservationCommand(date, startTime, endTime, title, attendeeCount);
    }
}
