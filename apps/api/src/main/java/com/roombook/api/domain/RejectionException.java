package com.roombook.api.domain;

/** 업무 규칙 위반. 어느 규칙에 걸렸는지 코드 하나를 싣는다. */
public class RejectionException extends RuntimeException {

    private final RejectionCode code;

    public RejectionException(RejectionCode code) {
        super(code.message());
        this.code = code;
    }

    public RejectionCode getCode() {
        return code;
    }
}
