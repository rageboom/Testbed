package com.roombook.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.roombook.api.support.ApiTestBase;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UC-RESERVATION-002 / BHV-RESERVATION-002 날짜별 예약 현황 조회")
class ReservationListApiTest extends ApiTestBase {

    // @ac AC-RESERVATION-002
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-002 — 그날의 예약을 시작 시각순으로 돌려준다 (AC-RESERVATION-002)")
    void listsReservationsOrderedByStartTime() throws Exception {
        insertReservation(1, "2026-11-17", "14:00", "15:00", "주간 회의", "E200", "RESERVED");
        insertReservation(1, "2026-11-17", "10:00", "11:00", "설계 리뷰", "E100", "RESERVED");

        list(1, "2026-11-17", "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("설계 리뷰"))
                .andExpect(jsonPath("$[0].startTime").value("10:00"))
                .andExpect(jsonPath("$[0].endTime").value("11:00"))
                .andExpect(jsonPath("$[0].reservedBy").value("E100"))
                .andExpect(jsonPath("$[1].title").value("주간 회의"))
                .andExpect(jsonPath("$[1].startTime").value("14:00"))
                .andExpect(jsonPath("$[1].endTime").value("15:00"))
                .andExpect(jsonPath("$[1].reservedBy").value("E200"));
    }

    // @ac AC-RESERVATION-003
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-003 — 예약이 없는 날은 빈 배열을 돌려준다 (AC-RESERVATION-003)")
    void returnsEmptyArrayWhenNoReservations() throws Exception {
        list(1, "2026-11-18", "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // @ac AC-RESERVATION-004
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-004 — 취소된 예약은 현황 목록에 싣지 않는다 (BR-RESERVATION-RETAIN-001 / AC-RESERVATION-004)")
    void hidesCancelledReservations() throws Exception {
        insertReservation(1, "2026-11-17", "10:00", "11:00", "취소된 회의", "E100", "CANCELLED");
        insertReservation(1, "2026-11-17", "13:00", "14:00", "남은 회의", "E100", "RESERVED");

        list(1, "2026-11-17", "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("남은 회의"));
    }

    // @ac AC-RESERVATION-005
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-005 — 본인 예약이고 취소 기한 안이면 mine 과 cancellable 이 참이다 (AC-RESERVATION-005)")
    void flagsOwnCancellableReservation() throws Exception {
        insertReserved(1, "2026-11-16", "11:00", "12:00", "E100");

        list(1, "2026-11-16", "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mine").value(true))
                .andExpect(jsonPath("$[0].cancellable").value(true));
    }

    // @ac AC-RESERVATION-006
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-006 — 남의 예약은 mine 과 cancellable 이 거짓이다 (AC-RESERVATION-006)")
    void flagsOthersReservation() throws Exception {
        insertReserved(1, "2026-11-16", "11:00", "12:00", "E200");

        list(1, "2026-11-16", "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mine").value(false))
                .andExpect(jsonPath("$[0].cancellable").value(false));
    }

    // @ac AC-RESERVATION-007
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-007 — 본인 예약이라도 시작 30분 전이 지나면 cancellable 이 거짓이다 (AC-RESERVATION-007)")
    void flagsOwnReservationPastCancelDeadline() throws Exception {
        insertReserved(1, "2026-11-16", "10:00", "11:00", "E100");

        list(1, "2026-11-16", "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mine").value(true))
                .andExpect(jsonPath("$[0].cancellable").value(false));
    }

    // @ac AC-RESERVATION-008
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-008 — 없는 회의실의 현황 조회는 404 로 거절한다 (AC-RESERVATION-008)")
    void rejectsUnknownRoom() throws Exception {
        list(999, "2026-11-17", "E100")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROOM_NOT_FOUND"));
    }

    // @ac AC-RESERVATION-009
    // @type load
    @Test
    @DisplayName("TC-RESERVATION-009 — 예약 현황 조회는 p95 500ms 안에 응답한다 (AC-RESERVATION-009)")
    void listRespondsWithin500msAtP95() throws Exception {
        // 회의실 30개 × 30일 × 하루 8건
        for (long roomId = 3; roomId <= 30; roomId++) {
            insertRoom(roomId, "회의실 " + roomId, 1, 8);
        }
        List<Object[]> rows = new ArrayList<>();
        LocalDate today = NOW.toLocalDate();
        for (long roomId = 1; roomId <= 30; roomId++) {
            for (int day = 0; day < 30; day++) {
                for (int slot = 0; slot < 8; slot++) {
                    rows.add(new Object[] {roomId, java.sql.Date.valueOf(today.plusDays(day)),
                            java.sql.Time.valueOf(java.time.LocalTime.of(11 + slot, 0)),
                            java.sql.Time.valueOf(java.time.LocalTime.of(12 + slot, 0)),
                            "회의", 2, "E" + (200 + slot), "RESERVED",
                            java.sql.Timestamp.valueOf(NOW.minusDays(1))});
                }
            }
        }
        jdbc.batchUpdate(
                "INSERT INTO RESERVATION (ROOM_ID, RESERVATION_DATE, START_TIME, END_TIME, TITLE, ATTENDEE_COUNT,"
                        + " RESERVED_BY, STATUS, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                rows);

        // 준비 호출(첫 호출의 초기화 비용은 '평소' 가 아니다)
        for (int i = 0; i < 10; i++) {
            list(1, today.toString(), "E100").andExpect(status().isOk());
        }
        List<Long> elapsedMillis = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long started = System.nanoTime();
            list(1, today.toString(), "E100")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(8)));
            elapsedMillis.add((System.nanoTime() - started) / 1_000_000);
        }
        Collections.sort(elapsedMillis);
        long p95 = elapsedMillis.get(94);
        assertThat(p95).as("예약 현황 조회 p95(ms)").isLessThanOrEqualTo(500);
    }
}
