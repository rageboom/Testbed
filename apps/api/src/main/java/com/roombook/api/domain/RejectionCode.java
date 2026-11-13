package com.roombook.api.domain;

/** 거절 응답이 싣는 코드. 걸린 규칙과 HTTP 상태, 사람이 읽는 설명을 한곳에 둔다(ADR-RESERVATION-003). */
public enum RejectionCode {
    OVERLAP(409, "같은 회의실의 다른 예약과 시간이 겹칩니다."),
    INVALID_SLOT(422, "시작과 끝은 정각 또는 30분이어야 합니다."),
    INVALID_DURATION(422, "예약은 30분 이상 4시간 이하여야 합니다."),
    OUT_OF_HOURS(422, "예약은 09:00 부터 20:00 사이에만 잡을 수 있습니다."),
    OUT_OF_WINDOW(422, "지난 시각이나 오늘부터 30일 뒤를 넘는 날짜로는 예약할 수 없습니다."),
    INVALID_ATTENDEES(422, "참석 인원은 1명 이상이고 회의실 수용 인원 이하여야 합니다."),
    INVALID_TITLE(422, "제목은 필수이고 50자 이내여야 합니다."),
    NOT_OWNER(403, "예약한 본인만 취소할 수 있습니다."),
    CANCEL_TOO_LATE(422, "시작 30분 전까지만 취소할 수 있습니다."),
    ALREADY_CANCELLED(422, "이미 취소된 예약입니다."),
    ROOM_NOT_FOUND(404, "회의실을 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(404, "예약을 찾을 수 없습니다."),
    INVALID_REQUEST(400, "요청 형식이 올바르지 않습니다.");

    private final int httpStatus;
    private final String message;

    RejectionCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String message() {
        return message;
    }
}
