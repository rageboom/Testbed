import { useCallback, useEffect, useState } from 'react';
import { ApiError, roombookApi, type Reservation, type Room } from '../api/client';
import { rejectionMessage } from '../rejection-messages';
import { ReservationForm } from './ReservationForm';

type LoadState = 'loading' | 'loaded' | 'empty' | 'failed';

/** 오늘 날짜(YYYY-MM-DD). 서버와 같은 Asia/Seoul 기준이다. */
function todayInSeoul(): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Seoul' }).format(new Date());
}

export function ReservationBoard() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [roomId, setRoomId] = useState<number | null>(null);
  const [date, setDate] = useState(todayInSeoul);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [state, setState] = useState<LoadState>('loading');
  const [reloadKey, setReloadKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [formOpen, setFormOpen] = useState(false);

  useEffect(() => {
    let stale = false;
    roombookApi
      .listRooms()
      .then((loaded) => {
        if (stale) return;
        setRooms(loaded);
        // 기본값은 첫 번째 회의실이다.
        setRoomId((current) => current ?? loaded[0]?.id ?? null);
      })
      .catch(() => {
        if (!stale) setState('failed');
      });
    return () => {
      stale = true;
    };
  }, []);

  useEffect(() => {
    if (roomId === null || date === '') return;
    let stale = false;
    setState('loading');
    roombookApi
      .listReservations(roomId, date)
      .then((loaded) => {
        if (stale) return;
        setReservations(loaded);
        setState(loaded.length === 0 ? 'empty' : 'loaded');
      })
      .catch(() => {
        if (!stale) setState('failed');
      });
    return () => {
      stale = true;
    };
  }, [roomId, date, reloadKey]);

  const reload = useCallback(() => setReloadKey((key) => key + 1), []);

  async function handleCancel(reservationId: number) {
    setError(null);
    try {
      await roombookApi.cancelReservation(reservationId);
      reload();
    } catch (e) {
      setError(e instanceof ApiError ? rejectionMessage(e.code, e.message) : '취소 요청에 실패했습니다.');
    }
  }

  function handleCreated() {
    setFormOpen(false);
    reload();
  }

  return (
    <section className="reservation-board">
      <div className="filters">
        <label>
          회의실
          <select
            value={roomId ?? ''}
            onChange={(e) => {
              setError(null);
              setRoomId(Number(e.target.value));
            }}
          >
            {rooms.map((room) => (
              <option key={room.id} value={room.id}>
                {room.name} ({room.floor}층 · {room.capacity}명)
              </option>
            ))}
          </select>
        </label>
        <label>
          날짜
          <input
            type="date"
            value={date}
            onChange={(e) => {
              setError(null);
              setDate(e.target.value);
            }}
          />
        </label>
        <button type="button" disabled={roomId === null || date === ''} onClick={() => setFormOpen(true)}>
          예약하기
        </button>
      </div>

      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}

      {formOpen && roomId !== null && date !== '' && (
        <ReservationForm roomId={roomId} date={date} onCreated={handleCreated} onClose={() => setFormOpen(false)} />
      )}

      {state === 'loading' && <p className="hint">불러오는 중…</p>}
      {state === 'failed' && <p className="hint">예약 현황을 불러오지 못했습니다.</p>}
      {state === 'empty' && <p className="hint">예약이 없습니다</p>}
      {state === 'loaded' && (
        <ul className="reservations">
          {reservations.map((reservation) => (
            <li key={reservation.id}>
              <span className="time">
                {reservation.startTime} ~ {reservation.endTime}
              </span>
              <span className="title">{reservation.title}</span>
              <span className="reserved-by">{reservation.reservedBy}</span>
              {reservation.mine && (
                <button
                  type="button"
                  disabled={!reservation.cancellable}
                  onClick={() => handleCancel(reservation.id)}
                >
                  취소
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
