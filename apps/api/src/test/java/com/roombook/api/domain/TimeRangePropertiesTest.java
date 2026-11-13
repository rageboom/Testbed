package com.roombook.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.roombook.api.support.DomainApi;
import java.time.LocalTime;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/** BHV-RESERVATION-004 / UNIT-RESERVATION-005 — 불변식 INV-RESERVATION-001 을 받치는 겹침 판정의 속성. */
class TimeRangePropertiesTest {

    private static LocalTime slot(int index) {
        return LocalTime.of(9, 0).plusMinutes(30L * index);
    }

    // @tc TC-RESERVATION-113
    // @ac AC-RESERVATION-037 AC-RESERVATION-038
    // @unit UNIT-RESERVATION-005
    // @bhv BHV-RESERVATION-004
    // @type property
    @Property(tries = 500)
    @Label("TC-RESERVATION-113 — 겹침 판정은 대칭이고 구간 산술과 같다")
    void TC_RESERVATION_113_overlap_is_symmetric_and_matches_interval_arithmetic(
            @ForAll @IntRange(min = 0, max = 21) int start1, @ForAll @IntRange(min = 1, max = 22) int end1,
            @ForAll @IntRange(min = 0, max = 21) int start2, @ForAll @IntRange(min = 1, max = 22) int end2) {
        Assume.that(start1 < end1 && start2 < end2);

        boolean expected = start1 < end2 && start2 < end1;

        assertThat(DomainApi.overlaps(slot(start1), slot(end1), slot(start2), slot(end2))).isEqualTo(expected);
        assertThat(DomainApi.overlaps(slot(start2), slot(end2), slot(start1), slot(end1))).isEqualTo(expected);
    }

    // @tc TC-RESERVATION-114
    // @ac AC-RESERVATION-038
    // @unit UNIT-RESERVATION-005
    // @bhv BHV-RESERVATION-004
    // @type property
    @Property(tries = 200)
    @Label("TC-RESERVATION-114 — 맞닿은 두 구간은 겹치지 않는다")
    void TC_RESERVATION_114_adjacent_ranges_never_overlap(
            @ForAll @IntRange(min = 0, max = 20) int start, @ForAll @IntRange(min = 1, max = 21) int middle,
            @ForAll @IntRange(min = 2, max = 22) int end) {
        Assume.that(start < middle && middle < end);

        assertThat(DomainApi.overlaps(slot(start), slot(middle), slot(middle), slot(end))).isFalse();
        assertThat(DomainApi.overlaps(slot(middle), slot(end), slot(start), slot(middle))).isFalse();
    }
}
