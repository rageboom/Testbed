import { describe, expect, it } from 'vitest';
import { rejectionMessage } from './rejection-messages';

const CODES = [
  'OVERLAP',
  'INVALID_SLOT',
  'INVALID_DURATION',
  'OUT_OF_HOURS',
  'OUT_OF_WINDOW',
  'INVALID_ATTENDEES',
  'INVALID_TITLE',
];

describe('BHV-RESERVATION-003 · BHV-RESERVATION-005 / UNIT-RESERVATION-016 rejectionMessage', () => {
  // @ac AC-RESERVATION-035
  // @type unit
  it('TC-RESERVATION-035 — 거절 코드마다 그 규칙의 문장이 정해져 있다 (AC-RESERVATION-035)', () => {
    const sentences = CODES.map((code) => rejectionMessage(code, '서버 메시지'));

    for (const sentence of sentences) {
      expect(sentence).not.toBe('');
      expect(sentence).not.toBe('서버 메시지');
      expect(sentence).toMatch(/[가-힣]/);
    }
    expect(new Set(sentences).size).toBe(CODES.length);
    expect(rejectionMessage('SOMETHING_NEW', '서버가 준 설명')).toBe('서버가 준 설명');
  });

  // @ac AC-RESERVATION-034 AC-RESERVATION-049
  // @unit UNIT-RESERVATION-016
  // @bhv BHV-RESERVATION-003
  // @type unit
  it('TC-RESERVATION-141 — 길이 위반과 취소 기한 위반의 문장은 정해진 그대로다', () => {
    expect(rejectionMessage('INVALID_DURATION', 'x')).toBe('예약은 30분 이상 4시간 이하여야 합니다.');
    expect(rejectionMessage('CANCEL_TOO_LATE', 'x')).toBe('시작 30분 전까지만 취소할 수 있습니다.');
  });

  // @ac AC-RESERVATION-035 AC-RESERVATION-049
  // @unit UNIT-RESERVATION-016
  // @bhv BHV-RESERVATION-005
  // @type unit
  it('TC-RESERVATION-142 — 취소 거절 코드에도 문장이 있고 모르는 코드는 받은 설명을 그대로 돌려준다', () => {
    expect(rejectionMessage('NOT_OWNER', 'x')).toMatch(/본인/);
    expect(rejectionMessage('ALREADY_CANCELLED', 'x')).toMatch(/취소/);
    expect(rejectionMessage('', '설명')).toBe('설명');
    expect(rejectionMessage('UNKNOWN', '설명')).toBe('설명');
  });
});
