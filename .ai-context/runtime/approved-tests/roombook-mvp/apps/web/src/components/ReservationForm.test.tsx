import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';
import { fakeApi, freezeToday, reservation } from '../testing/fake-api';
import { ReservationBoard } from './ReservationBoard';

beforeEach(() => {
  freezeToday();
});

/** 현황 화면을 오로라(id 1) · 2026-11-17 로 맞추고 예약 폼을 연다. */
async function openForm(user: ReturnType<typeof userEvent.setup>) {
  render(<ReservationBoard />);
  const dateInput = await screen.findByLabelText('날짜');
  await user.clear(dateInput);
  await user.type(dateInput, '2026-11-17');
  await user.click(screen.getByRole('button', { name: '예약하기' }));
  return screen.findByRole('form', { name: '예약 폼' });
}

async function fill(user: ReturnType<typeof userEvent.setup>, form: HTMLElement) {
  await user.selectOptions(within(form).getByLabelText('시작 시각'), '10:00');
  await user.selectOptions(within(form).getByLabelText('끝 시각'), '11:00');
  await user.type(within(form).getByLabelText('제목'), '설계 리뷰');
  const attendees = within(form).getByLabelText('참석 인원');
  await user.clear(attendees);
  await user.type(attendees, '4');
}

describe('UC-RESERVATION-003 / BHV-RESERVATION-003 예약 폼', () => {
  // @ac AC-RESERVATION-032
  // @type integration
  it('TC-RESERVATION-032 — 예약하기 버튼을 누르면 예약 폼이 열린다 (AC-RESERVATION-032)', async () => {
    const user = userEvent.setup();
    fakeApi();

    render(<ReservationBoard />);
    await screen.findByLabelText('날짜');
    expect(screen.queryByRole('form', { name: '예약 폼' })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '예약하기' }));

    const form = await screen.findByRole('form', { name: '예약 폼' });
    const start = within(form).getByLabelText('시작 시각');
    const end = within(form).getByLabelText('끝 시각');
    expect(start.tagName).toBe('SELECT');
    expect(end.tagName).toBe('SELECT');
    expect(within(start).getByRole('option', { name: '09:00' })).toBeInTheDocument();
    expect(within(start).getByRole('option', { name: '09:30' })).toBeInTheDocument();
    expect(within(start).queryByRole('option', { name: '09:15' })).not.toBeInTheDocument();
    expect(within(end).getByRole('option', { name: '20:00' })).toBeInTheDocument();
    expect(within(form).getByLabelText('제목')).toBeRequired();
    expect(within(form).getByLabelText('제목')).toHaveAttribute('maxlength', '50');
    expect(within(form).getByLabelText('참석 인원')).toHaveAttribute('type', 'number');
    expect(within(form).queryByLabelText('회의실')).not.toBeInTheDocument();
    expect(within(form).queryByLabelText('날짜')).not.toBeInTheDocument();
  });

  // @ac AC-RESERVATION-033
  // @type integration
  it('TC-RESERVATION-033 — 폼을 제출하면 현황 화면에서 고른 회의실과 날짜로 예약을 요청한다 (AC-RESERVATION-033)', async () => {
    const user = userEvent.setup();
    const api = fakeApi();
    const form = await openForm(user);
    await fill(user, form);

    await user.click(within(form).getByRole('button', { name: '예약' }));

    await waitFor(() => expect(api.posts()).toHaveLength(1));
    expect(api.posts()[0]).toEqual({
      method: 'POST',
      path: '/api/rooms/1/reservations',
      body: { date: '2026-11-17', startTime: '10:00', endTime: '11:00', title: '설계 리뷰', attendeeCount: 4 },
    });
  });

  // @ac AC-RESERVATION-034
  // @type integration
  it('TC-RESERVATION-034 — 서버가 거절하면 걸린 규칙을 폼 위에 문장으로 보여 준다 (BR-RESERVATION-DURATION-001 / AC-RESERVATION-034)', async () => {
    const user = userEvent.setup();
    fakeApi({ create: () => ({ status: 422, body: { code: 'INVALID_DURATION', message: 'invalid duration' } }) });
    const form = await openForm(user);
    await fill(user, form);

    await user.click(within(form).getByRole('button', { name: '예약' }));

    expect(await within(form).findByRole('alert')).toHaveTextContent('예약은 30분 이상 4시간 이하여야 합니다.');
    expect(screen.getByRole('form', { name: '예약 폼' })).toBeInTheDocument();
    expect(within(form).getByLabelText('제목')).toHaveValue('설계 리뷰');
    expect(within(form).getByLabelText('시작 시각')).toHaveValue('10:00');
  });

  // @ac AC-RESERVATION-036
  // @type integration
  it('TC-RESERVATION-036 — 예약에 성공하면 폼을 닫고 현황을 다시 불러온다 (AC-RESERVATION-036)', async () => {
    const user = userEvent.setup();
    let created = false;
    const api = fakeApi({
      list: () => (created ? [reservation({ id: 99, date: '2026-11-17', title: '설계 리뷰', mine: true, cancellable: true })] : []),
      create: () => {
        created = true;
        return { status: 201, body: reservation({ id: 99, date: '2026-11-17', mine: true, cancellable: true }) };
      },
    });
    const form = await openForm(user);
    await fill(user, form);
    const listCallsBefore = api.listCalls().length;

    await user.click(within(form).getByRole('button', { name: '예약' }));

    await waitFor(() => expect(screen.queryByRole('form', { name: '예약 폼' })).not.toBeInTheDocument());
    const items = await screen.findAllByRole('listitem');
    expect(items[0]).toHaveTextContent('설계 리뷰');
    expect(api.listCalls().length).toBe(listCallsBefore + 1);
    expect(api.listCalls().at(-1)?.path).toBe('/api/rooms/1/reservations?date=2026-11-17');
  });
});
