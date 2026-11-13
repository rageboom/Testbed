package com.roombook.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.roombook.api.support.ApiTestBase;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("UC-RESERVATION-003 / BHV-RESERVATION-004 겹치는 예약 거절")
class ReservationOverlapApiTest extends ApiTestBase {

    // @ac AC-RESERVATION-037
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-037 — 기존 예약과 시간이 겹치면 OVERLAP 으로 거절한다 (BR-RESERVATION-OVERLAP-001 / AC-RESERVATION-037)")
    void rejectsOverlappingReservation() throws Exception {
        insertReserved(1, "2026-11-17", "10:00", "11:00", "E200");

        create(1, "E100", body("2026-11-17", "10:30", "11:30", "회의", 2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OVERLAP"));
        assertThat(countReserved(1, "2026-11-17")).isEqualTo(1);
    }

    // @ac AC-RESERVATION-038
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-038 — 기존 예약의 끝과 맞닿은 예약은 받는다 (BR-RESERVATION-OVERLAP-001 / AC-RESERVATION-038)")
    void acceptsAdjacentReservation() throws Exception {
        insertReserved(1, "2026-11-17", "10:00", "11:00", "E200");

        create(1, "E100", body("2026-11-17", "11:00", "12:00", "회의", 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"));
        assertThat(countReserved(1, "2026-11-17")).isEqualTo(2);
    }

    // @ac AC-RESERVATION-039
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-039 — 취소된 예약과 같은 시간대의 예약은 받는다 (BR-RESERVATION-RETAIN-001 / AC-RESERVATION-039)")
    void acceptsSlotOfCancelledReservation() throws Exception {
        insertReservation(1, "2026-11-17", "10:00", "11:00", "취소된 회의", "E200", "CANCELLED");

        create(1, "E100", body("2026-11-17", "10:00", "11:00", "회의", 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    // @ac AC-RESERVATION-040
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-040 — 다른 회의실의 같은 시간대 예약은 받는다 (BR-RESERVATION-OVERLAP-001 / AC-RESERVATION-040)")
    void acceptsSameSlotInAnotherRoom() throws Exception {
        insertReserved(2, "2026-11-17", "10:00", "11:00", "E100");

        create(1, "E100", body("2026-11-17", "10:00", "11:00", "회의", 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    // @ac AC-RESERVATION-041
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-041 — 겹치는 예약이 동시에 들어오면 하나만 성공한다 (BR-RESERVATION-OVERLAP-001 / AC-RESERVATION-041)")
    void onlyOneOfConcurrentOverlappingRequestsSucceeds() throws Exception {
        int requests = 10;
        ExecutorService pool = Executors.newFixedThreadPool(requests);
        CountDownLatch ready = new CountDownLatch(requests);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<MockHttpServletResponse>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < requests; i++) {
                String employeeId = "E" + (300 + i);
                Callable<MockHttpServletResponse> call = () -> {
                    ready.countDown();
                    go.await();
                    return create(1, employeeId, body("2026-11-17", "10:00", "11:00", "회의", 2))
                            .andReturn().getResponse();
                };
                futures.add(pool.submit(call));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            go.countDown();

            int created = 0;
            int conflict = 0;
            for (Future<MockHttpServletResponse> future : futures) {
                MockHttpServletResponse response = future.get(30, TimeUnit.SECONDS);
                if (response.getStatus() == 201) {
                    created++;
                } else if (response.getStatus() == 409) {
                    assertThat(response.getContentAsString()).contains("\"code\":\"OVERLAP\"");
                    conflict++;
                }
            }
            assertThat(created).as("201 응답 수").isEqualTo(1);
            assertThat(conflict).as("409 OVERLAP 응답 수").isEqualTo(requests - 1);
            assertThat(countReserved(1, "2026-11-17")).as("예약 상태인 예약 수").isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }
}
