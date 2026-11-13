package com.roombook.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.roombook.api.support.DomainApi;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BHV-RESERVATION-004 / UNIT-RESERVATION-005 TimeRange.overlaps")
class TimeRangeTest {

    private static boolean overlaps(String start1, String end1, String start2, String end2) {
        return DomainApi.overlaps(LocalTime.parse(start1), LocalTime.parse(end1), LocalTime.parse(start2),
                LocalTime.parse(end2));
    }

    // @ac AC-RESERVATION-037
    // @unit UNIT-RESERVATION-005
    // @bhv BHV-RESERVATION-004
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-111 — 시간이 겹치는 두 구간은 겹침이다 (BR-RESERVATION-OVERLAP-001)")
    void detectsOverlap() {
        assertThat(overlaps("10:00", "11:00", "10:30", "11:30")).isTrue();
        assertThat(overlaps("10:30", "11:30", "10:00", "11:00")).isTrue();
        assertThat(overlaps("10:00", "12:00", "10:30", "11:00")).as("한 구간이 다른 구간을 품는다").isTrue();
        assertThat(overlaps("10:00", "11:00", "10:00", "11:00")).as("같은 구간").isTrue();
    }

    // @ac AC-RESERVATION-038
    // @unit UNIT-RESERVATION-005
    // @bhv BHV-RESERVATION-004
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-112 — 맞닿기만 하거나 떨어진 구간은 겹침이 아니다 (BR-RESERVATION-OVERLAP-001)")
    void adjacentOrApartIsNotOverlap() {
        assertThat(overlaps("10:00", "11:00", "11:00", "12:00")).isFalse();
        assertThat(overlaps("11:00", "12:00", "10:00", "11:00")).isFalse();
        assertThat(overlaps("10:00", "11:00", "13:00", "14:00")).isFalse();
    }
}
