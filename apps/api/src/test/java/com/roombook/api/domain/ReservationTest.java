package com.roombook.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.roombook.api.support.DomainApi;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BHV-RESERVATION-005 · BHV-RESERVATION-002 / Reservation")
class ReservationTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 16);
    private static final LocalDateTime BOOKED_AT = LocalDateTime.of(2026, 11, 15, 9, 0);

    /** E100 이 2026-11-16 11:00~12:00 에 잡은 예약. */
    private static Object reservation() {
        return DomainApi.reserve("E100", DAY, LocalTime.of(11, 0), LocalTime.of(12, 0), BOOKED_AT);
    }

    private static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(DAY, LocalTime.of(hour, minute));
    }

    // @ac AC-RESERVATION-042
    // @unit UNIT-RESERVATION-006
    // @bhv BHV-RESERVATION-005
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-121 — 새 예약은 예약 상태이고 본인이 기한 안에 취소하면 취소 상태가 된다 (BR-RESERVATION-RETAIN-001)")
    void cancelsWithinDeadline() {
        Object reservation = reservation();
        assertThat(DomainApi.status(reservation)).isEqualTo("RESERVED");

        assertThat(DomainApi.cancel(reservation, "E100", at(10, 0))).isNull();
        assertThat(DomainApi.status(reservation)).isEqualTo("CANCELLED");
    }

    // @ac AC-RESERVATION-044
    // @unit UNIT-RESERVATION-006
    // @bhv BHV-RESERVATION-005
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-122 — 예약한 본인이 아니면 NOT_OWNER 이고 상태는 그대로다 (BR-RESERVATION-CANCEL-001)")
    void rejectsCancelByOther() {
        Object reservation = reservation();

        assertThat(DomainApi.cancel(reservation, "E200", at(10, 0))).isEqualTo("NOT_OWNER");
        assertThat(DomainApi.status(reservation)).isEqualTo("RESERVED");
    }

    // @ac AC-RESERVATION-043 AC-RESERVATION-045
    // @unit UNIT-RESERVATION-006
    // @bhv BHV-RESERVATION-005
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-123 — 시작 30분 전까지는 취소되고 그 뒤에는 CANCEL_TOO_LATE 다 (BR-RESERVATION-CANCEL-002)")
    void enforcesCancelDeadline() {
        Object late = reservation();
        assertThat(DomainApi.cancel(late, "E100", at(10, 31))).isEqualTo("CANCEL_TOO_LATE");
        assertThat(DomainApi.status(late)).isEqualTo("RESERVED");

        Object started = reservation();
        assertThat(DomainApi.cancel(started, "E100", at(11, 30))).isEqualTo("CANCEL_TOO_LATE");

        Object boundary = reservation();
        assertThat(DomainApi.cancel(boundary, "E100", at(10, 30))).as("정확히 30분 전").isNull();
        assertThat(DomainApi.status(boundary)).isEqualTo("CANCELLED");
    }

    // @ac AC-RESERVATION-046
    // @unit UNIT-RESERVATION-006
    // @bhv BHV-RESERVATION-005
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-124 — 이미 취소된 예약의 취소는 ALREADY_CANCELLED 다 (BR-RESERVATION-RETAIN-001)")
    void rejectsSecondCancel() {
        Object reservation = reservation();
        DomainApi.cancel(reservation, "E100", at(9, 0));

        assertThat(DomainApi.cancel(reservation, "E100", at(9, 5))).isEqualTo("ALREADY_CANCELLED");
        assertThat(DomainApi.status(reservation)).isEqualTo("CANCELLED");
    }

    // @ac AC-RESERVATION-044
    // @unit UNIT-RESERVATION-006
    // @bhv BHV-RESERVATION-005
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-125 — 본인 확인이 기한 확인보다 먼저다")
    void checksOwnerBeforeDeadline() {
        Object reservation = reservation();

        assertThat(DomainApi.cancel(reservation, "E200", at(10, 45))).isEqualTo("NOT_OWNER");
    }

    // @ac AC-RESERVATION-005 AC-RESERVATION-006 AC-RESERVATION-007
    // @unit UNIT-RESERVATION-007
    // @bhv BHV-RESERVATION-002
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-131 — 본인 예약이고 예약 상태이며 시작 30분 전 이내가 아닐 때만 취소할 수 있다고 답한다 (BR-RESERVATION-CANCEL-002)")
    void answersCancellable() {
        Object reservation = reservation();

        assertThat(DomainApi.isCancellableBy(reservation, "E100", at(10, 0))).isTrue();
        assertThat(DomainApi.isCancellableBy(reservation, "E100", at(10, 30))).as("정확히 30분 전").isTrue();
        assertThat(DomainApi.isCancellableBy(reservation, "E100", at(10, 31))).isFalse();
        assertThat(DomainApi.isCancellableBy(reservation, "E200", at(10, 0))).as("남의 예약").isFalse();

        DomainApi.cancel(reservation, "E100", at(10, 0));
        assertThat(DomainApi.isCancellableBy(reservation, "E100", at(10, 5))).as("이미 취소된 예약").isFalse();
    }
}
