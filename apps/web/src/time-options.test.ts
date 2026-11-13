import { describe, expect, it } from 'vitest';
import { timeOptions } from './time-options';

describe('BHV-RESERVATION-003 / UNIT-RESERVATION-017 timeOptions', () => {
  // @ac AC-RESERVATION-032
  // @unit UNIT-RESERVATION-017
  // @bhv BHV-RESERVATION-003
  // @type unit
  it('TC-RESERVATION-151 — 09:00 부터 20:00 까지 30분 간격의 시각을 낸다 (BR-RESERVATION-SLOT-001)', () => {
    const options = timeOptions();

    expect(options[0]).toBe('09:00');
    expect(options[1]).toBe('09:30');
    expect(options.at(-1)).toBe('20:00');
    expect(options).toHaveLength(23);
    for (const option of options) {
      expect(option).toMatch(/^\d{2}:(00|30)$/);
    }
    expect([...options].sort()).toEqual(options);
  });
});
