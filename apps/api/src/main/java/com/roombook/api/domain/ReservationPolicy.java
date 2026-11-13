package com.roombook.api.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 예약 생성의 입력 규칙.
 * 여러 규칙에 걸리면 정해진 순서의 첫 위반 하나만 알린다:
 * TITLE → SLOT → DURATION → HOURS → WINDOW → ATTENDEES (ADR-RESERVATION-003).
 */
public final class ReservationPolicy {

    public static final int SLOT_MINUTES = 30;
    public static final int MIN_MINUTES = 30;
    public static final int MAX_MINUTES = 240;
    public static final LocalTime OPENING = LocalTime.of(9, 0);
    public static final LocalTime CLOSING = LocalTime.of(20, 0);
    public static final int BOOKING_WINDOW_DAYS = 30;
    public static final int TITLE_MAX_LENGTH = 50;

    private ReservationPolicy() {
        // 정적 메서드만 있다
    }

    public static void validate(LocalDate date, LocalTime startTime, LocalTime endTime, String title,
            int attendeeCount, int roomCapacity, LocalDateTime now) {
        if (title == null || title.isBlank() || title.length() > TITLE_MAX_LENGTH) {
            throw new RejectionException(RejectionCode.INVALID_TITLE);
        }
        if (!onSlot(startTime) || !onSlot(endTime)) {
            throw new RejectionException(RejectionCode.INVALID_SLOT);
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new RejectionException(RejectionCode.INVALID_DURATION);
        }
        if (startTime.isBefore(OPENING) || endTime.isAfter(CLOSING)) {
            throw new RejectionException(RejectionCode.OUT_OF_HOURS);
        }
        if (LocalDateTime.of(date, startTime).isBefore(now)
                || date.isAfter(now.toLocalDate().plusDays(BOOKING_WINDOW_DAYS))) {
            throw new RejectionException(RejectionCode.OUT_OF_WINDOW);
        }
        if (attendeeCount < 1 || attendeeCount > roomCapacity) {
            throw new RejectionException(RejectionCode.INVALID_ATTENDEES);
        }
    }

    private static boolean onSlot(LocalTime time) {
        return time.getMinute() % SLOT_MINUTES == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }
}
