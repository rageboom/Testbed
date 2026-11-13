package com.roombook.api.web;

import com.roombook.api.application.ReservationService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 직원은 사내 게이트웨이가 넣어 주는 X-Employee-Id 헤더로 식별한다. */
@RestController
public class ReservationController {

    static final String EMPLOYEE_HEADER = "X-Employee-Id";

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping("/api/rooms/{roomId}/reservations")
    public List<ReservationResponse> listReservations(@PathVariable long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(EMPLOYEE_HEADER) String employeeId) {
        return service.listReservations(roomId, date, employeeId).stream().map(ReservationResponse::from).toList();
    }

    @PostMapping("/api/rooms/{roomId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@PathVariable long roomId,
            @RequestHeader(EMPLOYEE_HEADER) String employeeId, @RequestBody CreateReservationRequest request) {
        return ReservationResponse.from(service.create(roomId, request.toCommand(), employeeId));
    }

    @PostMapping("/api/reservations/{reservationId}/cancel")
    public ReservationResponse cancelReservation(@PathVariable long reservationId,
            @RequestHeader(EMPLOYEE_HEADER) String employeeId) {
        return ReservationResponse.from(service.cancel(reservationId, employeeId));
    }
}
