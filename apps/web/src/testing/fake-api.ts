import { vi } from 'vitest';

export interface RoomDto {
  id: number;
  name: string;
  floor: number;
  capacity: number;
}

export interface ReservationDto {
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

export const ROOMS: RoomDto[] = [
  { id: 1, name: '오로라', floor: 3, capacity: 6 },
  { id: 2, name: '보리', floor: 5, capacity: 12 },
];

export function reservation(over: Partial<ReservationDto> = {}): ReservationDto {
  return {
    id: 1,
    roomId: 1,
    date: '2026-11-16',
    startTime: '10:00',
    endTime: '11:00',
    title: '설계 리뷰',
    attendeeCount: 4,
    reservedBy: 'E100',
    status: 'RESERVED',
    mine: false,
    cancellable: false,
    ...over,
  };
}

export interface Call {
  method: string;
  path: string;
  body: unknown;
}

type Reply = { status: number; body?: unknown };

/**
 * fetch 대역. 경로와 메서드로 응답을 고르고 부른 내역을 남긴다.
 * 현황 조회의 응답은 list 에 넣은 함수가 호출 때마다 정한다(다시 불러오기를 확인하려고).
 */
export function fakeApi(options: {
  rooms?: RoomDto[];
  list?: (roomId: number, date: string) => ReservationDto[];
  create?: () => Reply;
  cancel?: () => Reply;
} = {}) {
  const calls: Call[] = [];
  const json = (reply: Reply) =>
    new Response(reply.body === undefined ? null : JSON.stringify(reply.body), {
      status: reply.status,
      headers: { 'Content-Type': 'application/json' },
    });

  const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = new URL(typeof input === 'string' ? input : input.toString(), 'http://localhost');
    const method = (init?.method ?? 'GET').toUpperCase();
    const body = typeof init?.body === 'string' ? JSON.parse(init.body) : undefined;
    calls.push({ method, path: url.pathname + url.search, body });

    if (method === 'GET' && url.pathname === '/api/rooms') {
      return json({ status: 200, body: options.rooms ?? ROOMS });
    }
    const listMatch = /^\/api\/rooms\/(\d+)\/reservations$/.exec(url.pathname);
    if (listMatch && method === 'GET') {
      const date = url.searchParams.get('date') ?? '';
      return json({ status: 200, body: options.list ? options.list(Number(listMatch[1]), date) : [] });
    }
    if (listMatch && method === 'POST') {
      return json(options.create ? options.create() : { status: 201, body: reservation({ id: 99, mine: true, cancellable: true }) });
    }
    if (/^\/api\/reservations\/\d+\/cancel$/.test(url.pathname) && method === 'POST') {
      return json(options.cancel ? options.cancel() : { status: 200, body: reservation({ status: 'CANCELLED' }) });
    }
    return json({ status: 404, body: { code: 'NOT_FOUND', message: `대역에 없는 경로: ${method} ${url.pathname}` } });
  });
  vi.stubGlobal('fetch', fetchMock);

  return {
    calls,
    listCalls: () => calls.filter((c) => c.method === 'GET' && /\/reservations\?/.test(c.path)),
    posts: () => calls.filter((c) => c.method === 'POST'),
  };
}

/** 현재 시각을 2026-11-16 10:00 (Asia/Seoul) 로 고정한다. 타이머는 그대로 둔다. */
export function freezeToday() {
  vi.useFakeTimers({ toFake: ['Date'] });
  vi.setSystemTime(new Date('2026-11-16T10:00:00+09:00'));
}
