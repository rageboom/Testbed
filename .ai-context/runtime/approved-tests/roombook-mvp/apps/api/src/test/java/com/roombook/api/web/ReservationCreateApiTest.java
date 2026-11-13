package com.roombook.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.roombook.api.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("UC-RESERVATION-003 / BHV-RESERVATION-003 예약 생성과 입력 규칙 확인")
class ReservationCreateApiTest extends ApiTestBase {

    private void expectRejected(ResultActions result, int httpStatus, String code) throws Exception {
        result.andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty());
        assertThat(countAllReservations()).as("거절한 예약은 저장되지 않는다").isZero();
    }

    private void expectCreated(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"));
        assertThat(countAllReservations()).isEqualTo(1);
    }

    // @ac AC-RESERVATION-014
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-014 — 규칙을 지킨 예약은 예약 상태로 만들어진다 (AC-RESERVATION-014)")
    void createsReservation() throws Exception {
        create(1, "E100", body("2026-11-17", "10:00", "11:00", "설계 리뷰", 4))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.date").value("2026-11-17"))
                .andExpect(jsonPath("$.startTime").value("10:00"))
                .andExpect(jsonPath("$.endTime").value("11:00"))
                .andExpect(jsonPath("$.title").value("설계 리뷰"))
                .andExpect(jsonPath("$.attendeeCount").value(4))
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.reservedBy").value("E100"));

        list(1, "2026-11-17", "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("설계 리뷰"));
    }

    // @ac AC-RESERVATION-015
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-015 — 30분 단위가 아닌 시각은 INVALID_SLOT 으로 거절한다 (BR-RESERVATION-SLOT-001 / AC-RESERVATION-015)")
    void rejectsOffSlotTime() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "10:15", "11:00", "회의", 2)), 422, "INVALID_SLOT");
    }

    // @ac AC-RESERVATION-016
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-016 — 끝 시각이 시작 시각과 같으면 INVALID_DURATION 으로 거절한다 (BR-RESERVATION-DURATION-001 / AC-RESERVATION-016)")
    void rejectsZeroDuration() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "10:00", "10:00", "회의", 2)), 422, "INVALID_DURATION");
    }

    // @ac AC-RESERVATION-017
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-017 — 4시간을 넘는 예약은 INVALID_DURATION 으로 거절한다 (BR-RESERVATION-DURATION-001 / AC-RESERVATION-017)")
    void rejectsOverFourHours() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "10:00", "14:30", "회의", 2)), 422, "INVALID_DURATION");
    }

    // @ac AC-RESERVATION-018
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-018 — 정확히 4시간인 예약은 받는다 (BR-RESERVATION-DURATION-001 / AC-RESERVATION-018)")
    void acceptsExactlyFourHours() throws Exception {
        expectCreated(create(1, "E100", body("2026-11-17", "10:00", "14:00", "워크숍", 2)));
    }

    // @ac AC-RESERVATION-019
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-019 — 09:00 보다 이른 시작은 OUT_OF_HOURS 로 거절한다 (BR-RESERVATION-HOURS-001 / AC-RESERVATION-019)")
    void rejectsStartBeforeNine() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "08:30", "09:30", "회의", 2)), 422, "OUT_OF_HOURS");
    }

    // @ac AC-RESERVATION-020
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-020 — 20:00 보다 늦은 끝은 OUT_OF_HOURS 로 거절한다 (BR-RESERVATION-HOURS-001 / AC-RESERVATION-020)")
    void rejectsEndAfterTwenty() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "19:30", "20:30", "회의", 2)), 422, "OUT_OF_HOURS");
    }

    // @ac AC-RESERVATION-021
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-021 — 끝 시각이 정확히 20:00 인 예약은 받는다 (BR-RESERVATION-HOURS-001 / AC-RESERVATION-021)")
    void acceptsEndAtTwenty() throws Exception {
        expectCreated(create(1, "E100", body("2026-11-17", "19:00", "20:00", "회의", 2)));
    }

    // @ac AC-RESERVATION-022
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-022 — 지난 시각의 예약은 OUT_OF_WINDOW 로 거절한다 (BR-RESERVATION-WINDOW-001 / AC-RESERVATION-022)")
    void rejectsPastStart() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-16", "09:30", "10:30", "회의", 2)), 422, "OUT_OF_WINDOW");
    }

    // @ac AC-RESERVATION-023
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-023 — 오늘 + 31일의 예약은 OUT_OF_WINDOW 로 거절한다 (BR-RESERVATION-WINDOW-001 / AC-RESERVATION-023)")
    void rejectsBeyondThirtyDays() throws Exception {
        expectRejected(create(1, "E100", body("2026-12-17", "10:00", "11:00", "회의", 2)), 422, "OUT_OF_WINDOW");
    }

    // @ac AC-RESERVATION-024
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-024 — 오늘 + 30일의 예약은 받는다 (BR-RESERVATION-WINDOW-001 / AC-RESERVATION-024)")
    void acceptsThirtiethDay() throws Exception {
        expectCreated(create(1, "E100", body("2026-12-16", "10:00", "11:00", "회의", 2)));
    }

    // @ac AC-RESERVATION-025
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-025 — 참석 인원이 0명이면 INVALID_ATTENDEES 로 거절한다 (BR-RESERVATION-ATTENDEES-001 / AC-RESERVATION-025)")
    void rejectsZeroAttendees() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "10:00", "11:00", "회의", 0)), 422, "INVALID_ATTENDEES");
    }

    // @ac AC-RESERVATION-026
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-026 — 참석 인원이 수용 인원을 넘으면 INVALID_ATTENDEES 로 거절한다 (BR-RESERVATION-ATTENDEES-001 / AC-RESERVATION-026)")
    void rejectsAttendeesOverCapacity() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "10:00", "11:00", "회의", 7)), 422, "INVALID_ATTENDEES");
    }

    // @ac AC-RESERVATION-027
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-027 — 참석 인원이 수용 인원과 같은 예약은 받는다 (BR-RESERVATION-ATTENDEES-001 / AC-RESERVATION-027)")
    void acceptsAttendeesAtCapacity() throws Exception {
        expectCreated(create(1, "E100", body("2026-11-17", "10:00", "11:00", "회의", 6)));
    }

    // @ac AC-RESERVATION-028
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-028 — 제목이 비어 있으면 INVALID_TITLE 로 거절한다 (BR-RESERVATION-TITLE-001 / AC-RESERVATION-028)")
    void rejectsBlankTitle() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "10:00", "11:00", "  ", 2)), 422, "INVALID_TITLE");
    }

    // @ac AC-RESERVATION-029
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-029 — 제목이 51자면 INVALID_TITLE 로 거절한다 (BR-RESERVATION-TITLE-001 / AC-RESERVATION-029)")
    void rejectsTitleOverFiftyChars() throws Exception {
        expectRejected(create(1, "E100", body("2026-11-17", "10:00", "11:00", "가".repeat(51), 2)), 422,
                "INVALID_TITLE");
    }

    // @ac AC-RESERVATION-030
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-030 — 없는 회의실의 예약은 404 로 거절한다 (AC-RESERVATION-030)")
    void rejectsUnknownRoom() throws Exception {
        expectRejected(create(999, "E100", body("2026-11-17", "10:00", "11:00", "회의", 2)), 404, "ROOM_NOT_FOUND");
    }

    // @ac AC-RESERVATION-031
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-031 — X-Employee-Id 헤더가 없는 예약 요청은 400 으로 거절한다 (AC-RESERVATION-031)")
    void rejectsMissingEmployeeHeader() throws Exception {
        expectRejected(
                mockMvc.perform(post("/api/rooms/{roomId}/reservations", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("2026-11-17", "10:00", "11:00", "회의", 2))),
                400, "INVALID_REQUEST");
    }
}
