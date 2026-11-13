/** 거절 코드를 화면에 보일 문장으로 바꾼다. 모르는 코드면 서버가 준 설명(fallback)을 그대로 쓴다. */
export function rejectionMessage(code: string, fallback: string): string {
  const MESSAGES: Record<string, string> = {
    OVERLAP: '같은 회의실의 다른 예약과 시간이 겹칩니다.',
    INVALID_SLOT: '시작과 끝은 정각 또는 30분이어야 합니다.',
    INVALID_DURATION: '예약은 30분 이상 4시간 이하여야 합니다.',
    OUT_OF_HOURS: '예약은 09:00 부터 20:00 사이에만 잡을 수 있습니다.',
    OUT_OF_WINDOW: '지난 시각이나 오늘부터 30일 뒤를 넘는 날짜로는 예약할 수 없습니다.',
    INVALID_ATTENDEES: '참석 인원은 1명 이상이고 회의실 수용 인원 이하여야 합니다.',
    INVALID_TITLE: '제목은 필수이고 50자 이내여야 합니다.',
    NOT_OWNER: '예약한 본인만 취소할 수 있습니다.',
    CANCEL_TOO_LATE: '시작 30분 전까지만 취소할 수 있습니다.',
    ALREADY_CANCELLED: '이미 취소된 예약입니다.',
    ROOM_NOT_FOUND: '회의실을 찾을 수 없습니다.',
    RESERVATION_NOT_FOUND: '예약을 찾을 수 없습니다.',
  };
  return Object.hasOwn(MESSAGES, code) ? MESSAGES[code] : fallback;
}
