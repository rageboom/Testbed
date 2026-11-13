package com.roombook.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.roombook.api.support.DomainApi;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BHV-RESERVATION-003 / UNIT-RESERVATION-004 ReservationPolicy.validate")
class ReservationPolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 10, 0);
    private static final LocalDate TOMORROW = LocalDate.of(2026, 11, 17);
    private static final int CAPACITY = 6;

    private static String validate(LocalDate date, String start, String end, String title, int attendees) {
        return DomainApi.validate(date, LocalTime.parse(start), LocalTime.parse(end), title, attendees, CAPACITY, NOW);
    }

    // @ac AC-RESERVATION-014
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-101 — 규칙을 모두 지킨 예약은 통과한다")
    void passesValidReservation() {
        assertThat(validate(TOMORROW, "10:00", "11:00", "설계 리뷰", 4)).isNull();
        assertThat(validate(TOMORROW, "09:00", "09:30", "가".repeat(50), 1)).isNull();
        assertThat(validate(TOMORROW, "16:00", "20:00", "워크숍", CAPACITY)).isNull();
    }

    // @ac AC-RESERVATION-015
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-102 — 시작이나 끝이 30분 단위가 아니면 INVALID_SLOT 이다 (BR-RESERVATION-SLOT-001)")
    void rejectsOffSlot() {
        assertThat(validate(TOMORROW, "10:15", "11:00", "회의", 2)).isEqualTo("INVALID_SLOT");
        assertThat(validate(TOMORROW, "10:00", "10:45", "회의", 2)).isEqualTo("INVALID_SLOT");
        assertThat(DomainApi.validate(TOMORROW, LocalTime.of(10, 0, 30), LocalTime.of(11, 0), "회의", 2, CAPACITY, NOW))
                .isEqualTo("INVALID_SLOT");
    }

    // @ac AC-RESERVATION-016 AC-RESERVATION-017 AC-RESERVATION-018
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-103 — 길이가 30분 미만이거나 4시간을 넘으면 INVALID_DURATION 이다 (BR-RESERVATION-DURATION-001)")
    void rejectsBadDuration() {
        assertThat(validate(TOMORROW, "10:00", "10:00", "회의", 2)).isEqualTo("INVALID_DURATION");
        assertThat(validate(TOMORROW, "11:00", "10:00", "회의", 2)).isEqualTo("INVALID_DURATION");
        assertThat(validate(TOMORROW, "10:00", "14:30", "회의", 2)).isEqualTo("INVALID_DURATION");
        assertThat(validate(TOMORROW, "10:00", "14:00", "회의", 2)).isNull();
        assertThat(validate(TOMORROW, "10:00", "10:30", "회의", 2)).isNull();
    }

    // @ac AC-RESERVATION-019 AC-RESERVATION-020 AC-RESERVATION-021
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-104 — 09:00 이전 시작이나 20:00 이후 끝은 OUT_OF_HOURS 다 (BR-RESERVATION-HOURS-001)")
    void rejectsOutOfHours() {
        assertThat(validate(TOMORROW, "08:30", "09:30", "회의", 2)).isEqualTo("OUT_OF_HOURS");
        assertThat(validate(TOMORROW, "19:30", "20:30", "회의", 2)).isEqualTo("OUT_OF_HOURS");
        assertThat(validate(TOMORROW, "09:00", "10:00", "회의", 2)).isNull();
        assertThat(validate(TOMORROW, "19:00", "20:00", "회의", 2)).isNull();
    }

    // @ac AC-RESERVATION-022 AC-RESERVATION-023 AC-RESERVATION-024
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-105 — 지난 시각이나 오늘 + 30일을 넘는 날짜는 OUT_OF_WINDOW 다 (BR-RESERVATION-WINDOW-001)")
    void rejectsOutOfWindow() {
        LocalDate today = NOW.toLocalDate();
        assertThat(validate(today, "09:30", "10:30", "회의", 2)).isEqualTo("OUT_OF_WINDOW");
        assertThat(validate(today.minusDays(1), "10:00", "11:00", "회의", 2)).isEqualTo("OUT_OF_WINDOW");
        assertThat(validate(today.plusDays(31), "10:00", "11:00", "회의", 2)).isEqualTo("OUT_OF_WINDOW");
        assertThat(validate(today, "10:00", "11:00", "회의", 2)).as("지금 시작하는 예약은 지난 시각이 아니다").isNull();
        assertThat(validate(today.plusDays(30), "10:00", "11:00", "회의", 2)).isNull();
    }

    // @ac AC-RESERVATION-025 AC-RESERVATION-026 AC-RESERVATION-027
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-106 — 참석 인원이 1명 미만이거나 수용 인원을 넘으면 INVALID_ATTENDEES 다 (BR-RESERVATION-ATTENDEES-001)")
    void rejectsBadAttendees() {
        assertThat(validate(TOMORROW, "10:00", "11:00", "회의", 0)).isEqualTo("INVALID_ATTENDEES");
        assertThat(validate(TOMORROW, "10:00", "11:00", "회의", -1)).isEqualTo("INVALID_ATTENDEES");
        assertThat(validate(TOMORROW, "10:00", "11:00", "회의", CAPACITY + 1)).isEqualTo("INVALID_ATTENDEES");
        assertThat(validate(TOMORROW, "10:00", "11:00", "회의", CAPACITY)).isNull();
    }

    // @ac AC-RESERVATION-028 AC-RESERVATION-029
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-107 — 제목이 비었거나 50자를 넘으면 INVALID_TITLE 이다 (BR-RESERVATION-TITLE-001)")
    void rejectsBadTitle() {
        assertThat(validate(TOMORROW, "10:00", "11:00", null, 2)).isEqualTo("INVALID_TITLE");
        assertThat(validate(TOMORROW, "10:00", "11:00", "", 2)).isEqualTo("INVALID_TITLE");
        assertThat(validate(TOMORROW, "10:00", "11:00", "   ", 2)).isEqualTo("INVALID_TITLE");
        assertThat(validate(TOMORROW, "10:00", "11:00", "가".repeat(51), 2)).isEqualTo("INVALID_TITLE");
    }

    // @ac AC-RESERVATION-015 AC-RESERVATION-016 AC-RESERVATION-019 AC-RESERVATION-022 AC-RESERVATION-025 AC-RESERVATION-028
    // @unit UNIT-RESERVATION-004
    // @bhv BHV-RESERVATION-003
    // @type unit
    @Test
    @DisplayName("TC-RESERVATION-108 — 여러 규칙에 걸리면 정해진 순서의 첫 위반을 알린다")
    void reportsFirstViolationInFixedOrder() {
        // 순서: TITLE → SLOT → DURATION → HOURS → WINDOW → ATTENDEES (ADR-RESERVATION-003)
        LocalDate tooFar = NOW.toLocalDate().plusDays(40);
        assertThat(validate(tooFar, "08:15", "08:15", "", 0)).isEqualTo("INVALID_TITLE");
        assertThat(validate(tooFar, "08:15", "08:15", "회의", 0)).isEqualTo("INVALID_SLOT");
        assertThat(validate(tooFar, "08:00", "08:00", "회의", 0)).isEqualTo("INVALID_DURATION");
        assertThat(validate(tooFar, "08:00", "09:00", "회의", 0)).isEqualTo("OUT_OF_HOURS");
        assertThat(validate(tooFar, "09:00", "10:00", "회의", 0)).isEqualTo("OUT_OF_WINDOW");
        assertThat(validate(TOMORROW, "09:00", "10:00", "회의", 0)).isEqualTo("INVALID_ATTENDEES");
    }
}
