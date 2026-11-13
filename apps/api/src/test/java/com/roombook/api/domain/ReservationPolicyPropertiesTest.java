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

/** BHV-RESERVATION-003 / UNIT-RESERVATION-004 — 시간 규칙의 속성. */
class ReservationPolicyPropertiesTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 10, 0);
    private static final LocalDate TOMORROW = LocalDate.of(2026, 11, 17);

    // @tc TC-RESERVATION-109
    // @ac AC-RESERVATION-015 AC-RESERVATION-016 AC-RESERVATION-017 AC-RESERVATION-019 AC-RESERVATION-020
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type property
    @Property(tries = 500)
    @Label("TC-RESERVATION-109 — 시간 규칙은 30분 단위 · 길이 · 시간대를 모두 지킬 때에만 통과한다")
    void TC_RESERVATION_109_time_rules_pass_exactly_when_slot_duration_hours_hold(
            @ForAll @IntRange(min = 0, max = 1439) int startMinute,
            @ForAll @IntRange(min = 0, max = 1439) int endMinute) {
        boolean slot = startMinute % 30 == 0 && endMinute % 30 == 0;
        int length = endMinute - startMinute;
        boolean duration = length >= 30 && length <= 240;
        boolean hours = startMinute >= 9 * 60 && endMinute <= 20 * 60;

        String code = DomainApi.validate(TOMORROW, LocalTime.ofSecondOfDay(startMinute * 60L),
                LocalTime.ofSecondOfDay(endMinute * 60L), "회의", 2, 6, NOW);

        if (slot && duration && hours) {
            assertThat(code).isNull();
        } else if (!slot) {
            assertThat(code).isEqualTo("INVALID_SLOT");
        } else if (!duration) {
            assertThat(code).isEqualTo("INVALID_DURATION");
        } else {
            assertThat(code).isEqualTo("OUT_OF_HOURS");
        }
    }
}
