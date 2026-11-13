package com.roombook.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** 예약. 취소해도 지우지 않고 취소 상태로 남긴다(BR-RESERVATION-RETAIN-001). */
@Entity
@Table(name = "RESERVATION")
public class Reservation {

    public static final int CANCEL_DEADLINE_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ROOM_ID", nullable = false)
    private long roomId;

    @Column(name = "RESERVATION_DATE", nullable = false)
    private LocalDate date;

    @Column(name = "START_TIME", nullable = false)
    private LocalTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalTime endTime;

    @Column(name = "TITLE", nullable = false, length = 50)
    private String title;

    @Column(name = "ATTENDEE_COUNT", nullable = false)
    private int attendeeCount;

    @Column(name = "RESERVED_BY", nullable = false, length = 64)
    private String reservedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 16)
    private ReservationStatus status;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected Reservation() {
        // JPA 가 쓰는 기본 생성자
    }

    /** 입력 규칙과 겹침 판정을 통과한 예약을 예약 상태로 만든다. */
    public static Reservation reserve(long roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
            String title, int attendeeCount, String reservedBy, LocalDateTime now) {
        Reservation reservation = new Reservation();
        reservation.roomId = roomId;
        reservation.date = date;
        reservation.startTime = startTime;
        reservation.endTime = endTime;
        reservation.title = title;
        reservation.attendeeCount = attendeeCount;
        reservation.reservedBy = reservedBy;
        reservation.status = ReservationStatus.RESERVED;
        reservation.createdAt = now;
        return reservation;
    }

    /**
     * 예약한 본인이 시작 30분 전까지 취소한다.
     * 본인 확인 → 이미 취소됐는지 → 기한 순서로 본다.
     */
    public void cancel(String employeeId, LocalDateTime now) {
        if (!reservedBy.equals(employeeId)) {
            throw new RejectionException(RejectionCode.NOT_OWNER);
        }
        if (status == ReservationStatus.CANCELLED) {
            throw new RejectionException(RejectionCode.ALREADY_CANCELLED);
        }
        if (now.isAfter(cancelDeadline())) {
            throw new RejectionException(RejectionCode.CANCEL_TOO_LATE);
        }
        status = ReservationStatus.CANCELLED;
        cancelledAt = now;
    }

    /** 이 직원이 지금 취소할 수 있는가. */
    public boolean isCancellableBy(String employeeId, LocalDateTime now) {
        return isReservedBy(employeeId) && status == ReservationStatus.RESERVED && !now.isAfter(cancelDeadline());
    }

    public boolean isReservedBy(String employeeId) {
        return reservedBy.equals(employeeId);
    }

    public TimeRange timeRange() {
        return new TimeRange(startTime, endTime);
    }

    private LocalDateTime cancelDeadline() {
        return LocalDateTime.of(date, startTime).minusMinutes(CANCEL_DEADLINE_MINUTES);
    }

    public Long getId() {
        return id;
    }

    public long getRoomId() {
        return roomId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getTitle() {
        return title;
    }

    public int getAttendeeCount() {
        return attendeeCount;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
