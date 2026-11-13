package com.roombook.api.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.roombook.api.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UC-RESERVATION-001 / BHV-RESERVATION-001 회의실 목록 조회")
class RoomListApiTest extends ApiTestBase {

    // @ac AC-RESERVATION-001
    // @type integration
    @Test
    @DisplayName("TC-RESERVATION-001 — 회의실 목록을 이름·층·수용 인원과 함께 돌려준다 (AC-RESERVATION-001)")
    void listsRoomsWithNameFloorCapacity() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("오로라"))
                .andExpect(jsonPath("$[0].floor").value(3))
                .andExpect(jsonPath("$[0].capacity").value(6))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("보리"))
                .andExpect(jsonPath("$[1].floor").value(5))
                .andExpect(jsonPath("$[1].capacity").value(12));
    }
}
