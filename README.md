# roombook

사내 회의실 예약 서비스. 직원이 웹에서 회의실의 날짜별 예약 현황을 보고, 예약하고, 자기 예약을 취소한다.
기획서는 [docs/prd.md](docs/prd.md) 에 있다.

## 구조

| 경로 | 내용 |
| --- | --- |
| `apps/api` | 백엔드. Java 21, Spring Boot 3, Gradle, JUnit 5, H2 인메모리 |
| `apps/web` | 프론트엔드. React 19, TypeScript, Vite, Vitest |
| `.ai-context/base` | 설계 원장(업무 규칙, API 계약, 행동 명세와 인수 기준, 설계 결정) |

## 실행

```bash
# 백엔드 (http://localhost:8080)
cd apps/api && gradle bootRun

# 프론트엔드 (http://localhost:5173)
cd apps/web && npm install && npm run dev
```

직원은 요청 헤더 `X-Employee-Id` 로 식별한다. 운영에서는 사내 게이트웨이가 이 헤더를 넣는다.
개발 모드(`npm run dev`)에서는 프론트엔드가 이 헤더를 직접 실으며 값은 환경 변수 `VITE_DEV_EMPLOYEE_ID`(기본 `E100`)로 바꾼다.

저장은 H2 인메모리라 서버를 다시 띄우면 예약이 사라진다. 회의실은 `apps/api/src/main/resources/data.sql` 의 초기 데이터다.

## 테스트

```bash
node scripts/test/run-tests.mjs
```

백엔드와 프론트엔드 테스트를 차례로 돌리고 JUnit XML 보고서를 `build/test-reports/` 에 모은다.
