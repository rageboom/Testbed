export interface Room {
  id: number;
  name: string;
  floor: number;
  capacity: number;
}

export interface Reservation {
  id: number;
  roomId: number;
  date: string;
  startTime: string;
  endTime: string;
  title: string;
  attendeeCount: number;
  reservedBy: string;
  status: 'RESERVED' | 'CANCELLED';
  mine: boolean;
  cancellable: boolean;
}

export interface CreateReservationInput {
  date: string;
  startTime: string;
  endTime: string;
  title: string;
  attendeeCount: number;
}

/** 서버가 거절한 요청. code 는 걸린 규칙을 식별한다. */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

/**
 * 운영에서는 사내 게이트웨이가 X-Employee-Id 를 넣어 준다.
 * 개발 서버에는 게이트웨이가 없으므로 개발 모드에서만 이 헤더를 직접 싣는다(VITE_DEV_EMPLOYEE_ID, 기본 E100).
 */
function devHeaders(): Record<string, string> {
  if (!import.meta.env.DEV) return {};
  const employeeId = import.meta.env.VITE_DEV_EMPLOYEE_ID as string | undefined;
  return { 'X-Employee-Id': employeeId ?? 'E100' };
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, { ...init, headers: { ...devHeaders(), ...init?.headers } });
  if (!response.ok) {
    let code = '';
    let message = `요청에 실패했습니다 (${response.status})`;
    try {
      const body = (await response.json()) as { code?: unknown; message?: unknown };
      if (typeof body.code === 'string') code = body.code;
      if (typeof body.message === 'string' && body.message) message = body.message;
    } catch {
      // 본문이 JSON 이 아니면 상태 코드로 만든 설명을 쓴다.
    }
    throw new ApiError(response.status, code, message);
  }
  return (await response.json()) as T;
}

/**
 * 백엔드 REST API 호출. 직원 식별 헤더(X-Employee-Id)는 운영에서 사내 게이트웨이가 넣어 준다.
 */
export const roombookApi = {
  listRooms(): Promise<Room[]> {
    return request<Room[]>('/api/rooms');
  },

  listReservations(roomId: number, date: string): Promise<Reservation[]> {
    return request<Reservation[]>(`/api/rooms/${roomId}/reservations?date=${encodeURIComponent(date)}`);
  },

  createReservation(roomId: number, input: CreateReservationInput): Promise<Reservation> {
    return request<Reservation>(`/api/rooms/${roomId}/reservations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    });
  },

  cancelReservation(reservationId: number): Promise<Reservation> {
    return request<Reservation>(`/api/reservations/${reservationId}/cancel`, { method: 'POST' });
  },
};
