/** 예약할 수 있는 시각. 09:00 부터 20:00 까지 30분 간격의 HH:mm 목록이다. */
export function timeOptions(): string[] {
  const openingMinutes = 9 * 60;
  const closingMinutes = 20 * 60;
  const slotMinutes = 30;
  const options: string[] = [];
  for (let minutes = openingMinutes; minutes <= closingMinutes; minutes += slotMinutes) {
    const hh = String(Math.floor(minutes / 60)).padStart(2, '0');
    const mm = String(minutes % 60).padStart(2, '0');
    options.push(`${hh}:${mm}`);
  }
  return options;
}
