package com.roombook.api.web;

import com.roombook.api.domain.RejectionCode;

public record RejectionResponse(String code, String message) {

    static RejectionResponse of(RejectionCode code) {
        return new RejectionResponse(code.name(), code.message());
    }
}
