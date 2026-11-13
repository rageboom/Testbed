import { useState, type FormEvent } from 'react';
import { ApiError, roombookApi } from '../api/client';
import { rejectionMessage } from '../rejection-messages';
import { timeOptions } from '../time-options';

interface ReservationFormProps {
  /** 현황 화면에서 고른 회의실 */
  roomId: number;
  /** 현황 화면에서 고른 날짜 (YYYY-MM-DD) */
  date: string;
  onCreated: () => void;
  onClose: () => void;
}

const TIMES = timeOptions();
const START_TIMES = TIMES.slice(0, -1);
const END_TIMES = TIMES.slice(1);

export function ReservationForm({ roomId, date, onCreated, onClose }: ReservationFormProps) {
  const [startTime, setStartTime] = useState(START_TIMES[0]);
  const [endTime, setEndTime] = useState(END_TIMES[0]);
  const [title, setTitle] = useState('');
  const [attendeeCount, setAttendeeCount] = useState('1');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await roombookApi.createReservation(roomId, {
        date,
        startTime,
        endTime,
        title,
        attendeeCount: Number(attendeeCount),
      });
      onCreated();
    } catch (e) {
      setError(e instanceof ApiError ? rejectionMessage(e.code, e.message) : '예약 요청에 실패했습니다.');
      setSubmitting(false);
    }
  }

  return (
    <form aria-label="예약 폼" className="reservation-form" onSubmit={handleSubmit}>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      <label>
        시작 시각
        <select value={startTime} onChange={(e) => setStartTime(e.target.value)}>
          {START_TIMES.map((time) => (
            <option key={time} value={time}>
              {time}
            </option>
          ))}
        </select>
      </label>
      <label>
        끝 시각
        <select value={endTime} onChange={(e) => setEndTime(e.target.value)}>
          {END_TIMES.map((time) => (
            <option key={time} value={time}>
              {time}
            </option>
          ))}
        </select>
      </label>
      <label>
        제목
        <input type="text" value={title} required maxLength={50} onChange={(e) => setTitle(e.target.value)} />
      </label>
      <label>
        참석 인원
        <input
          type="number"
          value={attendeeCount}
          required
          min={1}
          onChange={(e) => setAttendeeCount(e.target.value)}
        />
      </label>
      <div className="actions">
        <button type="submit" disabled={submitting}>
          예약
        </button>
        <button type="button" onClick={onClose}>
          닫기
        </button>
      </div>
    </form>
  );
}
