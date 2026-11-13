package com.roombook.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.roombook.api.support.DomainApi;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/** BHV-RESERVATION-005 / UNIT-RESERVATION-006 — 불변식 INV-RESERVATION-002 의 속성. */
class ReservationPropertiesTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 16);

    // @tc TC-RESERVATION-126
    // @ac AC-RESERVATION-046
    // @unit UNIT-RESERVATION-006
    // @bhv BHV-RESERVATION-005
    // @type property
    @Property(tries = 300)
    @Label("TC-RESERVATION-126 — 취소된 예약은 다시 예약 상태로 돌아가지 않는다")
    void TC_RESERVATION_126_cancelled_reservation_never_returns_to_reserved(
            @ForAll @IntRange(min = 0, max = 3) int who, @ForAll @IntRange(min = -2880, max = 2880) int minutesFromStart) {
        Object reservation = DomainApi.reserve("E100", DAY, LocalTime.of(11, 0), LocalTime.of(12, 0),
                LocalDateTime.of(2026, 11, 10, 9, 0));
        assertThat(DomainApi.cancel(reservation, "E100", LocalDateTime.of(2026, 11, 12, 9, 0))).isNull();

        String employeeId = "E10" + who;
        LocalDateTime when = LocalDateTime.of(DAY, LocalTime.of(11, 0)).plusMinutes(minutesFromStart);
        String code = DomainApi.cancel(reservation, employeeId, when);

        assertThat(code).as("취소된 예약을 다시 취소할 수는 없다").isNotNull();
        assertThat(DomainApi.status(reservation)).isEqualTo("CANCELLED");
        assertThat(DomainApi.isCancellableBy(reservation, employeeId, when)).isFalse();
    }
}
