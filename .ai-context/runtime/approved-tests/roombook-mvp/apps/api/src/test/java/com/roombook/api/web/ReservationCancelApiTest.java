package com.roombook.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.roombook.api.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UC-RESERVATION-004 / BHV-RESERVATION-005 예약 취소")
class ReservationCancelApiTest extends ApiTestBase {

    // @ac AC-RESERVATION-042
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-042 — 본인이 기한 안에 취소하면 취소 상태가 된다 (BR-RESERVATION-CANCEL-001 / AC-RESERVATION-042)")
    void cancelsOwnReservation() throws Exception {
        long id = insertReserved(1, "2026-11-16", "11:00", "12:00", "E100");

        cancel(id, "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        assertThat(statusOf(id)).as("행은 지워지지 않고 취소 상태로 남는다").isEqualTo("CANCELLED");
    }

    // @ac AC-RESERVATION-043
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-043 — 정확히 시작 30분 전의 취소는 받는다 (BR-RESERVATION-CANCEL-002 / AC-RESERVATION-043)")
    void cancelsExactlyThirtyMinutesBefore() throws Exception {
        long id = insertReserved(1, "2026-11-16", "10:30", "11:30", "E100");

        cancel(id, "E100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // @ac AC-RESERVATION-044
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-044 — 예약한 본인이 아니면 NOT_OWNER 로 거절한다 (BR-RESERVATION-CANCEL-001 / AC-RESERVATION-044)")
    void rejectsCancelByOther() throws Exception {
        long id = insertReserved(1, "2026-11-16", "11:00", "12:00", "E200");

        cancel(id, "E100")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_OWNER"));
        assertThat(statusOf(id)).isEqualTo("RESERVED");
    }

    // @ac AC-RESERVATION-045
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-045 — 시작 30분 전이 지나면 CANCEL_TOO_LATE 로 거절한다 (BR-RESERVATION-CANCEL-002 / AC-RESERVATION-045)")
    void rejectsLateCancel() throws Exception {
        long id = insertReserved(1, "2026-11-16", "10:00", "11:00", "E100");

        cancel(id, "E100")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CANCEL_TOO_LATE"));
        assertThat(statusOf(id)).isEqualTo("RESERVED");
    }

    // @ac AC-RESERVATION-046
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-046 — 이미 취소된 예약의 취소는 ALREADY_CANCELLED 로 거절한다 (BR-RESERVATION-RETAIN-001 / AC-RESERVATION-046)")
    void rejectsCancelOfCancelled() throws Exception {
        long id = insertReservation(1, "2026-11-16", "11:00", "12:00", "회의", "E100", "CANCELLED");

        cancel(id, "E100")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ALREADY_CANCELLED"));
    }

    // @ac AC-RESERVATION-047
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-047 — 없는 예약의 취소는 404 로 거절한다 (AC-RESERVATION-047)")
    void rejectsUnknownReservation() throws Exception {
        cancel(999, "E100")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }
}
