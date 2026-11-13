package com.roombook.api.persistence;

import com.roombook.api.domain.Room;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByOrderByIdAsc();

    /**
     * 회의실 행을 잠그고 읽는다. 같은 회의실의 예약 생성이 줄을 서게 해
     * 겹침 판정과 저장 사이에 다른 예약이 끼어들지 못하게 한다(ADR-RESERVATION-001).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") long id);
}
