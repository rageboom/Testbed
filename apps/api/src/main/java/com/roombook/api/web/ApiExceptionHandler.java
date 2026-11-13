package com.roombook.api.web;

import com.roombook.api.domain.RejectionCode;
import com.roombook.api.domain.RejectionException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** 규칙 위반과 요청 형식 오류를 {code, message} 응답으로 바꾸는 한 곳(ADR-RESERVATION-003). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RejectionException.class)
    public ResponseEntity<RejectionResponse> handleRejection(RejectionException e) {
        return respond(e.getCode());
    }

    @ExceptionHandler({
        MissingRequestHeaderException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<RejectionResponse> handleInvalidRequest(Exception e) {
        return respond(RejectionCode.INVALID_REQUEST);
    }

    private static ResponseEntity<RejectionResponse> respond(RejectionCode code) {
        return ResponseEntity.status(code.httpStatus()).body(RejectionResponse.of(code));
    }
}
