import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';
import { fakeApi, freezeToday, reservation } from '../testing/fake-api';
import { ReservationBoard } from './ReservationBoard';

beforeEach(() => {
  freezeToday();
});

describe('UC-RESERVATION-002 / BHV-RESERVATION-002 예약 현황 화면', () => {
  // @ac AC-RESERVATION-010
  // @type integration
  it('TC-RESERVATION-010 — 첫 화면은 첫 번째 회의실과 오늘의 예약을 시간순으로 보여 준다 (AC-RESERVATION-010)', async () => {
    const api = fakeApi({
      list: () => [
        reservation({ id: 1, startTime: '10:00', endTime: '11:00', title: '설계 리뷰', reservedBy: 'E100' }),
        reservation({ id: 2, startTime: '14:00', endTime: '15:00', title: '주간 회의', reservedBy: 'E200' }),
      ],
    });

    render(<ReservationBoard />);

    const items = await screen.findAllByRole('listitem');
    expect(items).toHaveLength(2);
    expect(items[0]).toHaveTextContent('10:00');
    expect(items[0]).toHaveTextContent('11:00');
    expect(items[0]).toHaveTextContent('E100');
    expect(items[0]).toHaveTextContent('설계 리뷰');
    expect(items[1]).toHaveTextContent('14:00');
    expect(items[1]).toHaveTextContent('15:00');
    expect(items[1]).toHaveTextContent('E200');
    expect(items[1]).toHaveTextContent('주간 회의');

    const roomSelect = screen.getByRole('combobox', { name: '회의실' });
    expect(within(roomSelect).getByRole('option', { name: /오로라/, selected: true })).toBeInTheDocument();
    expect(screen.getByLabelText('날짜')).toHaveValue('2026-11-16');
    expect(api.listCalls()[0].path).toBe('/api/rooms/1/reservations?date=2026-11-16');
  });

  // @ac AC-RESERVATION-011
  // @type integration
  it('TC-RESERVATION-011 — 회의실이나 날짜를 바꾸면 그 현황을 다시 불러온다 (AC-RESERVATION-011)', async () => {
    const user = userEvent.setup();
    const api = fakeApi({
      list: (roomId, date) => {
        if (roomId === 2 && date === '2026-11-16') return [reservation({ id: 5, roomId: 2, title: '보리 회의' })];
        if (roomId === 2 && date === '2026-11-18') return [reservation({ id: 6, roomId: 2, date, title: '수요일 회의' })];
        return [reservation({ id: 1, title: '오로라 회의' })];
      },
    });
    render(<ReservationBoard />);
    expect(await screen.findByText('오로라 회의')).toBeInTheDocument();

    await user.selectOptions(screen.getByRole('combobox', { name: '회의실' }), '2');

    expect(await screen.findByText('보리 회의')).toBeInTheDocument();
    expect(screen.queryByText('오로라 회의')).not.toBeInTheDocument();
    expect(api.listCalls().at(-1)?.path).toBe('/api/rooms/2/reservations?date=2026-11-16');

    const dateInput = screen.getByLabelText('날짜');
    await user.clear(dateInput);
    await user.type(dateInput, '2026-11-18');

    expect(await screen.findByText('수요일 회의')).toBeInTheDocument();
    expect(api.listCalls().at(-1)?.path).toBe('/api/rooms/2/reservations?date=2026-11-18');
  });

  // @ac AC-RESERVATION-012
  // @type integration
  it("TC-RESERVATION-012 — 예약이 없으면 '예약이 없습니다' 를 보여 준다 (AC-RESERVATION-012)", async () => {
    fakeApi({ list: () => [] });

    render(<ReservationBoard />);

    expect(await screen.findByText('예약이 없습니다')).toBeInTheDocument();
    expect(screen.queryAllByRole('listitem')).toHaveLength(0);
  });

  // @ac AC-RESERVATION-013
  // @type integration
  it('TC-RESERVATION-013 — 자기 예약에만 취소 버튼이 있고 취소할 수 없으면 비활성이다 (BR-RESERVATION-CANCEL-001 / AC-RESERVATION-013)', async () => {
    fakeApi({
      list: () => [
        reservation({ id: 1, startTime: '11:00', endTime: '12:00', title: '취소 가능', mine: true, cancellable: true }),
        reservation({ id: 2, startTime: '13:00', endTime: '14:00', title: '기한 지남', mine: true, cancellable: false }),
        reservation({ id: 3, startTime: '15:00', endTime: '16:00', title: '남의 예약', mine: false, cancellable: false }),
      ],
    });

    render(<ReservationBoard />);

    const items = await screen.findAllByRole('listitem');
    expect(within(items[0]).getByRole('button', { name: '취소' })).toBeEnabled();
    expect(within(items[1]).getByRole('button', { name: '취소' })).toBeDisabled();
    expect(within(items[2]).queryByRole('button', { name: '취소' })).not.toBeInTheDocument();
  });
});

describe('UC-RESERVATION-004 / BHV-RESERVATION-005 화면에서 예약 취소', () => {
  // @ac AC-RESERVATION-048
  // @type integration
  it('TC-RESERVATION-048 — 취소 버튼을 누르면 취소를 요청하고 현황을 다시 불러온다 (BR-RESERVATION-CANCEL-001 / AC-RESERVATION-048)', async () => {
    const user = userEvent.setup();
    let cancelled = false;
    const api = fakeApi({
      list: () => (cancelled ? [] : [reservation({ id: 7, startTime: '11:00', endTime: '12:00', mine: true, cancellable: true })]),
      cancel: () => {
        cancelled = true;
        return { status: 200, body: reservation({ id: 7, status: 'CANCELLED' }) };
      },
    });
    render(<ReservationBoard />);

    await user.click(await screen.findByRole('button', { name: '취소' }));

    expect(await screen.findByText('예약이 없습니다')).toBeInTheDocument();
    expect(api.posts()).toEqual([{ method: 'POST', path: '/api/reservations/7/cancel', body: undefined }]);
    expect(api.listCalls()).toHaveLength(2);
  });

  // @ac AC-RESERVATION-049
  // @type integration
  it('TC-RESERVATION-049 — 취소가 거절되면 걸린 규칙을 현황 화면에 문장으로 보여 준다 (BR-RESERVATION-CANCEL-002 / AC-RESERVATION-049)', async () => {
    const user = userEvent.setup();
    fakeApi({
      list: () => [reservation({ id: 7, startTime: '11:00', endTime: '12:00', mine: true, cancellable: true })],
      cancel: () => ({ status: 422, body: { code: 'CANCEL_TOO_LATE', message: 'too late' } }),
    });
    render(<ReservationBoard />);

    await user.click(await screen.findByRole('button', { name: '취소' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('시작 30분 전까지만 취소할 수 있습니다.');
  });
});
