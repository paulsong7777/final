---
name: moeats-backend-integration-debug
description: Use this skill only for Mo-Eats backend integration analysis, bug fixing, and standards-aligned debugging on cloned debug branches. Trigger when the task involves Spring Boot 3.5.11, MyBatis, Thymeleaf, Bootstrap 5.3, Tier 1 group-order flow, selection_status, payment_mode, selected_delivery_address_idx, payment_share, payment_expires_at, room locking, or OWNER status transitions. Do not use it for greenfield feature ideation, broad redesign, or arbitrary renaming.
---

# Mo-Eats Backend Integration Debug Skill

## Goal
이 스킬의 목적은 Mo-Eats 백엔드 통합 브랜치를 기준 문서와 현재 구현에 맞춰 점검하고, 추정이 아닌 확인 기반으로 디버깅하는 것이다.

## Scope
이 스킬은 아래 범위에서만 사용한다.
- 백엔드 통합 브랜치 점검
- build 실패 원인 확인
- 도메인 / 매퍼 / 서비스 / 컨트롤러 정합성 점검
- 상태값, 컬럼명, nullable 처리, 결제 집계, 잠금 로직 검증
- 기준 문서와 구현 충돌 리포트 작성
- 승인된 최소 수정 패치 작성

이 스킬은 아래 범위에는 쓰지 않는다.
- 프로젝트 전체 재설계
- UI 디자인 재창작
- 임의 명명규칙 변경
- 사용자 승인 없는 스키마 확장

## Non-negotiable rules
- 항상 복제 브랜치에서만 수정한다. 원본 작업 브랜치는 읽기 전용으로 본다.
- 명명규칙은 문서 기준을 그대로 유지한다.
- `selection_status`, `payment_mode`, `participant_role`, `selected_delivery_address_idx`를 임의 변경하지 않는다.
- `selected_delivery_address_idx`는 nullable 기준으로 취급한다.
- `member.default_delivery_address_idx`는 기본 배송지 유일 기준이다.
- `store_category`와 `store_menu_category`는 다른 개념이다.
- 각자결제는 1/N이 아니라 참여자 장바구니 합계 기준이다.
- `payment_started_at ~ payment_expires_at` 5분 흐름을 보존한다.
- 점주 상태 전환은 `ACCEPTED -> PREPARING -> READY -> COMPLETED` 순차 전환만 허용한다.
- Tier 3 상태 노출은 기본적으로 건드리지 않는다.

## Required workflow
1. 먼저 아래 파일이 존재하는지 찾는다.
   - `build.gradle`
   - `src/main/resources/application.properties`
   - `src/main/resources/application-dev.properties`
   - `src/main/resources/mappers/**/*.xml`
   - `src/main/java/**/domain`
   - `src/main/java/**/service`
   - `src/main/java/**/controller`
   - `src/test/**`
2. 아래 문자열을 전수 검색한다.
   - `selection_status`
   - `menu_status`
   - `payment_mode`
   - `selected_delivery_address_idx`
   - `payment_expires_at`
   - `payment_share`
   - `room_code`
   - `ACCEPTED`
   - `PREPARING`
   - `READY`
   - `COMPLETED`
3. build 또는 test를 실행하기 전에 설정 파일과 의존성부터 읽는다.
4. 추정이 아닌 확인된 사실만 요약한다.
5. 문서와 구현 충돌을 발견하면 아래 형식으로 먼저 보고한다.
   - 기준 문서 요구
   - 현재 구현 확인 내용
   - 충돌 유형
   - 수정 필요 여부
6. 사용자가 수정까지 원하면 가장 작은 안전 패치부터 적용한다.
7. 수정 후 반드시 build와 관련 테스트를 다시 돌린다.

## Mandatory checks
### A. Build / config
- `spring-boot-starter-web` 중복 여부
- Java toolchain / sourceCompatibility
- MyBatis mapper-locations
- type-aliases-package
- camelCase 매핑 가정과 alias 충돌
- dev profile 실제 파일 존재 여부
- SecurityConfig 및 접근 정책

### B. Domain / DB consistency
- nullable 컬럼이 primitive 타입으로 선언돼 있지 않은지 확인
- `selectedDeliveryAddressIdx`는 `Integer`여야 하는지 검토
- `room_code` 6자리 / UNIQUE 전제 확인
- `store_category`, `store_menu_category`, `menu_category_idx` 연결 확인
- `payment_share` 집계 구조 확인

### C. Service logic
- 본인 소유 주소만 선택 가능한지
- 주문방 잠금 이후 장바구니 수정 차단되는지
- 각자결제 만료 처리 있는지
- 전원 성공 집계 후 주문 생성되는지
- 점주 상태 점프가 막혀 있는지

### D. Controller / endpoint behavior
- FE-04 주문방 생성 대응 endpoint
- FE-05 방 참여 대응 endpoint
- FE-06 협업 상태 조회 / 수정 endpoint
- FE-07 결제 endpoint
- FE-11 점주 상태 변경 endpoint

## Output format
항상 아래 형식으로 답한다.

### 1. 확인한 기준
- 읽은 파일 / 문서 목록

### 2. 확인된 사실
- 코드에서 직접 확인한 내용만 bullet로 정리

### 3. 충돌 및 리스크
- 기준 문서 대비 불일치 사항
- 아직 확인 안 된 영역

### 4. 수정안
- 바로 고쳐야 하는 것
- 보류 가능한 것

### 5. 실행 로그 요약
- 실행 명령
- 성공 / 실패
- 핵심 로그

## Preferred commands
아래 스크립트를 우선 사용한다.
- `bash .agents/skills/moeats-backend-integration-debug/scripts/check_build.sh`
- `bash .agents/skills/moeats-backend-integration-debug/scripts/grep_critical.sh`
- `bash .agents/skills/moeats-backend-integration-debug/scripts/run_focus_checks.sh`

## Escalation policy
다음 상황에서는 코드 수정 전에 반드시 보고만 한다.
- 스키마 변경 필요
- 상태값 변경 필요
- API URL 변경 필요
- 화면 흐름 변경 필요
- 보안 설정 변경 필요
- 대규모 리팩터링 필요

## Completion criteria
다음이 충족돼야 작업 완료로 본다.
- build 성공 또는 실패 원인 명확화
- 기준 문서 충돌 목록 작성 완료
- 적용 패치가 최소 범위인지 설명 완료
- 남은 리스크 명시 완료
