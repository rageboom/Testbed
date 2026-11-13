package com.roombook.api.application;

import com.roombook.api.domain.Reservation;

/** 예약과, 요청한 직원 기준의 본인 여부 · 지금 취소할 수 있는지. */
public record ReservationView(Reservation reservation, boolean mine, boolean cancellable) {
}
