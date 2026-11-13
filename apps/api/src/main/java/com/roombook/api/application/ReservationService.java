package com.roombook.api.application;

import com.roombook.api.domain.RejectionCode;
import com.roombook.api.domain.RejectionException;
import com.roombook.api.domain.Reservation;
import com.roombook.api.domain.ReservationPolicy;
import com.roombook.api.domain.ReservationStatus;
import com.roombook.api.domain.Room;
import com.roombook.api.domain.TimeRange;
import com.roombook.api.persistence.ReservationRepository;
import com.roombook.api.persistence.RoomRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final RoomRepository rooms;
    private final ReservationRepository reservations;
    private final Clock clock;

    public ReservationService(RoomRepository rooms, ReservationRepository reservations, Clock clock) {
        this.rooms = rooms;
        this.reservations = reservations;
        this.clock = clock;
    }

    /** 회의실 하나와 날짜 하나의 예약 상태인 예약을 시작 시각순으로 돌려준다. */
    @Transactional(readOnly = true)
    public List<ReservationView> listReservations(long roomId, LocalDate date, String employeeId) {
        if (!rooms.existsById(roomId)) {
            throw new RejectionException(RejectionCode.ROOM_NOT_FOUND);
        }
        LocalDateTime now = now();
        return reservations.findByRoomIdAndDateAndStatusOrderByStartTimeAsc(roomId, date, ReservationStatus.RESERVED)
                .stream()
                .map(reservation -> view(reservation, employeeId, now))
                .toList();
    }

    /**
     * 예약을 만든다. 회의실 행을 잠근 뒤 입력 규칙과 겹침을 차례로 확인하므로
     * 겹치는 요청이 동시에 들어와도 하나만 성공한다.
     */
    @Transactional
    public ReservationView create(long roomId, ReservationCommand command, String employeeId) {
        Room room = rooms.findByIdForUpdate(roomId)
                .orElseThrow(() -> new RejectionException(RejectionCode.ROOM_NOT_FOUND));
        LocalDateTime now = now();
        ReservationPolicy.validate(command.date(), command.startTime(), command.endTime(), command.title(),
                command.attendeeCount(), room.getCapacity(), now);

        TimeRange requested = new TimeRange(command.startTime(), command.endTime());
        boolean overlaps = reservations
                .findByRoomIdAndDateAndStatusOrderByStartTimeAsc(roomId, command.date(), ReservationStatus.RESERVED)
                .stream()
                .anyMatch(existing -> existing.timeRange().overlaps(requested));
        if (overlaps) {
            throw new RejectionException(RejectionCode.OVERLAP);
        }

        Reservation saved = reservations.save(Reservation.reserve(roomId, command.date(), command.startTime(),
                command.endTime(), command.title(), command.attendeeCount(), employeeId, now));
        return view(saved, employeeId, now);
    }

    /** 예약한 본인이 시작 30분 전까지 예약을 취소 상태로 바꾼다. */
    @Transactional
    public ReservationView cancel(long reservationId, String employeeId) {
        Reservation reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new RejectionException(RejectionCode.RESERVATION_NOT_FOUND));
        LocalDateTime now = now();
        reservation.cancel(employeeId, now);
        return view(reservation, employeeId, now);
    }

    private ReservationView view(Reservation reservation, String employeeId, LocalDateTime now) {
        return new ReservationView(reservation, reservation.isReservedBy(employeeId),
                reservation.isCancellableBy(employeeId, now));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
