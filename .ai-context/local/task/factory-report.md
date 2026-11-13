# 팩토리 결과 — roombook-mvp

- 종착: 고치지 못한 사유 (implement · 2026-11-13T15:13:27.060Z)
- 경로: full · 인가 단계: discovery
- 완료할 수 있나: 아니오 (미해결 1 · 열림 0). 완료 도장은 사람이 게이트 #5 에서 찍는다.

## 멈춘 사유

- implement 에서 멈췄다: 고치기와 되돌아가기를 다 쓰고도 막는 사유가 남았다.
- 막은 사유 (1):
  - ticket_evidence_unobserved (티켓 관측 없음) · 판정 20회
- 해 본 횟수: implement 판정 20회 · 이 단계에서 되돌아간 것 2회.
- 사람이 할 일: 게이트 #5 에서 사람이 이 보고서를 보고 정한다. 끝내거나(go · 완료를 막는 사유(HARD)가 남으면 넘지 못한다) 지적해 되돌린다(revisit).

## 단계

| 단계 | 판정 | 결과 |
| --- | --- | --- |
| spec | 1회 | 통과 |
| plan | 10회 | 통과 |
| test | 12회 | 이월 있음 |
| implement | 20회 | 미해결 있음 |

## 남은 것 (2)

- [미해결] ticket_evidence_unobserved (티켓 관측 없음) — implement · 판정 20회
- [이월] ticket_evidence_unobserved (티켓 관측 없음) — test · 판정 12회 · 사람 확인

## 고쳐서 통과 (6)

- evidence_missing (검증 증거 부족) — implement · 판정 1회
- unit_symbol_not_in_code (코드에 없는 UNIT 심볼) — implement · 판정 2회
- scope_impl_unexplained (설계로 설명되지 않는 코드 바뀜) — implement · 판정 15회
- scope_unit_revised (plan 승인 뒤 implement 에서 고친 UNIT) — implement · 판정 7회
- static_baseline_missing (정적 분석 기준선 없음) — implement · 판정 1회
- test_outcome_unmet (테스트 결과 미달) — test · 판정 1회

## 되돌아간 기록 (2)

- implement → plan (2026-11-13T15:05:37.784Z) — scope_impl_unexplained
- implement → plan (2026-11-13T15:11:48.390Z) — findings_unverified, ticket_evidence_unobserved, evidence_missing, scope_unit_revised

## 멈춤과 이어 가기 (2)

- 티켓 미발급 — plan (2026-11-13T14:33:10.633Z) → next 로 다시 돎 (2026-11-13T14:39:49.677Z)
- 고치지 못한 사유 — test (2026-11-13T14:52:07.948Z) → 「확인하고 이어 가기」로 이어 감 (2026-11-13T14:55:25.218Z)

## 다시 만든 승인 테스트 — diff 를 확인하세요

- 2026-11-13T15:07:09.156Z: apps/api/src/test/java/com/roombook/api/domain/ReservationPolicyPropertiesTest.java, apps/api/src/test/java/com/roombook/api/domain/ReservationPolicyTest.java, apps/api/src/test/java/com/roombook/api/domain/ReservationPropertiesTest.java, apps/api/src/test/java/com/roombook/api/domain/ReservationTest.java, apps/api/src/test/java/com/roombook/api/domain/TimeRangePropertiesTest.java, apps/api/src/test/java/com/roombook/api/domain/TimeRangeTest.java, apps/web/src/rejection-messages.test.ts, apps/web/src/time-options.test.ts

구동 9회 · 판정 43회 · 역행 2회 · 경과 48분
