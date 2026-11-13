package com.roombook.api.persistence;

import com.roombook.api.domain.Reservation;
import com.roombook.api.domain.ReservationStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByRoomIdAndDateAndStatusOrderByStartTimeAsc(long roomId, LocalDate date,
            ReservationStatus status);
}
