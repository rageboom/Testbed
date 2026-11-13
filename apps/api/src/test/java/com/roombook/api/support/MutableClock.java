package com.roombook.api.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** 테스트가 현재 시각을 고정하는 시계. 시간대는 Asia/Seoul 이다(ADR-RESERVATION-002). */
public class MutableClock extends Clock {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private volatile Instant instant = LocalDateTime.of(2026, 11, 16, 10, 0).atZone(ZONE).toInstant();

    public void set(LocalDateTime now) {
        this.instant = now.atZone(ZONE).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return ZONE;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
