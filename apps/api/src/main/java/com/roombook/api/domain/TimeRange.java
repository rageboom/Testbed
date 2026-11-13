package com.roombook.api.domain;

import java.time.LocalTime;

/** 하루 안의 시간 구간 [start, end). */
public record TimeRange(LocalTime start, LocalTime end) {

    /** 두 구간이 겹치면 true. 끝과 시작이 맞닿기만 한 것은 겹침이 아니다(BR-RESERVATION-OVERLAP-001). */
    public boolean overlaps(TimeRange other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
