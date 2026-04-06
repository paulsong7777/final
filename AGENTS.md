# AGENTS.md

## 저장소 공통 작업 원칙
- 이 저장소에서는 기획 문서, DB 기준서, 상태값 표준안, 프론트 계획서, 회의 변경사항 문서와 충돌 없는 구현을 우선한다.
- 명명규칙은 임의 변경 금지다.
- 기능 범위를 임의 확장하지 않는다.
- 기준 문서와 구현이 충돌하면 먼저 충돌 리포트를 작성하고, 사용자가 명시적으로 허용한 뒤에만 구조 변경을 제안한다.
- AI 티가 나는 문장, 과도한 장식, 불필요한 소개 문구를 문서나 코드 주석에 넣지 않는다.

## 현재 고정 기술 스택
- Java 21
- Spring Boot 3.5.11
- Spring MVC
- Thymeleaf
- Bootstrap 5.3
- MyBatis
- MySQL
- 모바일 우선 반응형 웹

## 핵심 제품 기준
- Tier 1 핵심 흐름은 방 생성 -> 참여 -> 협업 -> 결제 -> 점주 처리 -> 조회다.
- 결제 방식 값은 `REPRESENTATIVE`, `INDIVIDUAL`만 사용한다.
- 참여자 역할 값은 `LEADER`, `PARTICIPANT`만 사용한다.
- 참여자 선택 상태는 `selection_status`만 사용한다. `menu_status` 같은 혼용은 금지한다.
- `selected_delivery_address_idx`는 주문방 생성 시 선택 배송지 참조값이며 nullable 기준을 유지한다.
- 기본 배송지 판단은 `member.default_delivery_address_idx` 기준으로만 처리한다.
- `store_category`는 업종 분류, `store_menu_category`는 가게 내부 메뉴 섹션이다. 절대 혼동하지 않는다.
- 가게 상세 메뉴는 `store_menu_category` 기준 섹션형 표시를 전제로 본다.
- 각자결제는 균등 1/N이 아니라 참여자별 장바구니 합계 기준이다.
- 각자결제 타이머는 `payment_started_at` ~ `payment_expires_at` 기준으로 본다.
- 점주 상태 변경은 `ACCEPTED -> PREPARING -> READY -> COMPLETED` 순차 전환만 허용한다.
- Tier 3 전에는 `DELIVERING`, `CHECKED_IN`을 기본 노출하지 않는다.

## 회의 반영 UX 기준
- 모바일 우선과 UX 최우선을 유지한다.
- USER 헤더에는 6자리 방 참여 진입점이 항상 있어야 한다.
- 방 생성은 가게 상세 페이지 맥락에서 시작한다.
- 메뉴 담기와 선택완료는 구분한다.
- 명시적 선택완료 버튼을 눌렀을 때만 `selection_status=SELECTED`로 바뀐다.
- 메뉴 수정, 수량 변경, 삭제, 추가가 발생하면 `selection_status`는 `NOT_SELECTED`로 되돌아가야 한다.

## Codex 작업 방식
- 먼저 읽기와 검색으로 현황을 파악한 뒤 작업한다.
- 첫 응답에서는 아래 3가지를 항상 요약한다.
  1. 확인한 기준 파일
  2. 추정이 아닌 확인 사실
  3. 아직 확인되지 않은 영역
- 수정 전에는 영향 파일 목록을 제시한다.
- 수정 후에는 아래를 반드시 보고한다.
  - 변경 파일 목록
  - 실행한 명령
  - build / test 결과
  - 기준 문서 충돌 여부
  - 남은 리스크
- 빌드나 테스트가 실패하면 실패 로그 핵심 구간을 그대로 인용해 설명한다.

## 금지 사항
- 사용자가 요청하지 않은 대규모 리팩터링 금지
- 상태값, 컬럼명, FE 번호, URL, 파일명 임의 변경 금지
- 스키마와 충돌하는 임시 필드 추가 금지
- 로컬 비밀정보, 키, 토큰을 코드나 로그에 출력 금지
- 인터넷이 꼭 필요하지 않으면 사용하지 않는다.

## 우선 점검 순서
1. build.gradle 의존성 및 버전 충돌
2. application.properties / application-dev.properties / MyBatis 설정
3. domain / dto / mapper xml / service / controller 정합성
4. selection_status, payment_mode, participant_role, selected_delivery_address_idx 전수 점검
5. payment_share / payment_expires_at / room lock 흐름 점검
6. OWNER 상태 전환과 USER 화면 요구사항 충돌 여부 점검
