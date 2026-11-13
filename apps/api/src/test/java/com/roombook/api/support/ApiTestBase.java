package com.roombook.api.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * API 통합 테스트의 바탕. HTTP 요청부터 H2 까지 실제로 지난다.
 * 애플리케이션({@code com.roombook.api.RoombookApplication})을 테스트 시계와 함께 한 번 띄워 모든 테스트가 같이 쓴다.
 * 테스트를 구현보다 먼저 쓰므로(RED) 애플리케이션 클래스를 이름으로 찾고, 없으면 테스트마다 실패한다.
 * 테스트마다 표를 비우고 회의실 1(오로라 · 3층 · 6명)과 회의실 2(보리 · 5층 · 12명)를 넣으며,
 * 현재 시각을 2026-11-16 10:00 (Asia/Seoul) 로 고정한다.
 */
public abstract class ApiTestBase {

    private static final String APPLICATION_CLASS = "com.roombook.api.RoombookApplication";

    private static ConfigurableApplicationContext context;

    protected static final String EMPLOYEE_HEADER = "X-Employee-Id";
    protected static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 10, 0);

    protected MockMvc mockMvc;

    protected JdbcTemplate jdbc;

    protected MutableClock clock;

    private static synchronized ConfigurableApplicationContext application() throws ClassNotFoundException {
        if (context == null) {
            context = new SpringApplicationBuilder(Class.forName(APPLICATION_CLASS), TestClockConfig.class)
                    .properties("server.port=0", "spring.datasource.url=jdbc:h2:mem:roombook-test;DB_CLOSE_DELAY=-1")
                    .run();
        }
        return context;
    }

    @BeforeEach
    void resetData() throws Exception {
        ConfigurableApplicationContext app = application();
        mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) app).build();
        jdbc = app.getBean(JdbcTemplate.class);
        clock = app.getBean(MutableClock.class);
        jdbc.update("DELETE FROM RESERVATION");
        jdbc.update("DELETE FROM ROOM");
        insertRoom(1, "오로라", 3, 6);
        insertRoom(2, "보리", 5, 12);
        clock.set(NOW);
    }

    protected void insertRoom(long id, String name, int floor, int capacity) {
        jdbc.update("INSERT INTO ROOM (ID, NAME, FLOOR, CAPACITY) VALUES (?, ?, ?, ?)", id, name, floor, capacity);
    }

    protected long insertReservation(long roomId, String date, String start, String end, String title,
            String reservedBy, String status) {
        jdbc.update(
                "INSERT INTO RESERVATION (ROOM_ID, RESERVATION_DATE, START_TIME, END_TIME, TITLE, ATTENDEE_COUNT,"
                        + " RESERVED_BY, STATUS, CANCELLED_AT, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                roomId, Date.valueOf(LocalDate.parse(date)), Time.valueOf(LocalTime.parse(start)),
                Time.valueOf(LocalTime.parse(end)), title, 2, reservedBy, status,
                "CANCELLED".equals(status) ? Timestamp.valueOf(NOW.minusDays(1)) : null,
                Timestamp.valueOf(NOW.minusDays(2)));
        return jdbc.queryForObject("SELECT MAX(ID) FROM RESERVATION", Long.class);
    }

    protected long insertReserved(long roomId, String date, String start, String end, String reservedBy) {
        return insertReservation(roomId, date, start, end, "회의", reservedBy, "RESERVED");
    }

    protected String statusOf(long reservationId) {
        return jdbc.queryForObject("SELECT STATUS FROM RESERVATION WHERE ID = ?", String.class, reservationId);
    }

    protected int countReserved(long roomId, String date) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM RESERVATION WHERE ROOM_ID = ? AND RESERVATION_DATE = ? AND STATUS = 'RESERVED'",
                Integer.class, roomId, Date.valueOf(LocalDate.parse(date)));
    }

    protected int countAllReservations() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM RESERVATION", Integer.class);
    }

    protected static String body(String date, String start, String end, String title, int attendeeCount) {
        return "{\"date\":\"" + date + "\",\"startTime\":\"" + start + "\",\"endTime\":\"" + end + "\",\"title\":\""
                + title + "\",\"attendeeCount\":" + attendeeCount + "}";
    }

    protected ResultActions create(long roomId, String employeeId, String json) throws Exception {
        return mockMvc.perform(post("/api/rooms/{roomId}/reservations", roomId)
                .header(EMPLOYEE_HEADER, employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    protected ResultActions list(long roomId, String date, String employeeId) throws Exception {
        return mockMvc.perform(get("/api/rooms/{roomId}/reservations", roomId)
                .param("date", date)
                .header(EMPLOYEE_HEADER, employeeId));
    }

    protected ResultActions cancel(long reservationId, String employeeId) throws Exception {
        return mockMvc.perform(post("/api/reservations/{id}/cancel", reservationId)
                .header(EMPLOYEE_HEADER, employeeId));
    }
}
