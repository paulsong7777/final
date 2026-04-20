# Mo-Eats

"함께 고르고, 쉽게 모이고, 한 번에 주문"하는 그룹 주문 서비스입니다.

Mo-Eats는 여러 사용자가 하나의 주문방에 모여 같은 가게의 메뉴를 고르고, 대표결제 또는 각자결제로 주문을 마무리하는 흐름을 중심으로 설계된 반응형 웹 프로젝트입니다.

## 프로젝트 개요

단체 주문은 메뉴 취합이 번거롭고, 누락이 잦고, 결제 부담이 한 사람에게 몰리기 쉽습니다. Mo-Eats는 이 문제를 **주문방 기반 협업**, **실시간 선택 상태 공유**, **대표결제 / 각자결제 분기**, **점주 순차 상태 처리**로 풀어내는 것을 목표로 합니다.

### 핵심 목표
- **Tier 1**: 방 생성 → 참여 → 협업 → 결제 → 점주 처리 → 조회까지 1사이클 완주
- **Tier 2**: 옵션 선택, 리뷰, 로그/알림 연결
- **Tier 3**: DELIVERY / ONSITE 정식 분기, QR 생성/확인, 운영 인사이트

## 주요 기능

### 사용자
- 가게 탐색 및 상세 조회
- 주문방 생성 및 참여
- 참여자별 메뉴 선택 상태 공유
- 대표결제 / 각자결제 진행
- 주문 결과 및 주문 내역 조회

### 점주
- 주문 수신
- 주문 상태 순차 변경
- 주문 상세 확인

## 핵심 도메인 규칙
- `selection_status`를 기준으로 참여자의 선택 완료 여부를 관리합니다.
- `payment_mode`는 `REPRESENTATIVE`, `INDIVIDUAL` 두 값만 사용합니다.
- `participant_role`은 `LEADER`, `PARTICIPANT`로 통일합니다.
- 결제 단계 진입 시 주문방은 잠기며, 신규 참여와 장바구니 수정이 차단됩니다.
- 각자결제는 균등 분할이 아니라 **참여자 본인 주문 금액 기준**으로 처리합니다.
- 각자결제는 5분 제한 시간을 기준으로 집계 및 취소 규칙을 적용합니다.

## 기술 스택
- **Backend**: Java 21, Spring Boot 3.5.11, Spring MVC
- **Template**: Thymeleaf
- **Persistence**: MyBatis 3.0.5
- **Database**: MySQL
- **Build**: Gradle
- **Frontend 기준**: 반응형 웹, Bootstrap 5 계열 기준 문서 운영

## 공개 레포 기준 현재 구조

현재 공개 `main` 브랜치 기준으로 레포에는 Spring Boot 기본 골격과 설정 파일, 그리고 Tier 1 기본 SQL이 먼저 올라와 있습니다.

```text
final/
└─ moeats/
   ├─ build.gradle
   ├─ settings.gradle
   ├─ gradle/
   ├─ gradlew
   ├─ gradlew.bat
   ├─ HELP.md
   └─ src/
      ├─ main/
      │  ├─ java/
      │  │  └─ com/moeats/MoeatsApplication.java
      │  └─ resources/
      │     ├─ application.properties
      │     └─ SQL/
      │        └─ 01_moeats_tier1_schema.sql
      └─ test/
```

## 실행 방법

### 1) 프로젝트 진입
```bash
cd moeats
```

### 2) 로컬 DB 준비
MySQL에 `moeats` 데이터베이스를 준비한 뒤, 아래 SQL을 먼저 실행합니다.

```text
src/main/resources/SQL/01_moeats_tier1_schema.sql
```

### 3) 로컬 설정 추가
현재 공개 레포의 `application.properties`에는 애플리케이션 이름, `dev` 프로필, Thymeleaf, MyBatis, 포트, 인코딩 설정이 포함되어 있습니다. 실제 실행을 위해서는 로컬 환경에 맞는 DB 접속 정보가 추가로 필요합니다.

권장 방식은 `application-dev.properties`를 별도로 만들어 아래 항목을 채우는 것입니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/moeats?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
kakao.api-key=YOUR_KAKAOMAP_API

toss.payments.secret-key=test_sk_YOUR_TOSS_API
toss.payments.api-base-url=https://api.tosspayments.com
toss.payments.success-url=http://localhost:8080/sandbox/toss/success
toss.payments.fail-url=http://localhost:8080/sandbox/toss/fail
```

필요에 따라 MyBatis 로그, SQL 초기화, 파일 업로드, 시크릿 키 같은 로컬 전용 설정도 이 파일에 함께 분리해서 관리하는 방식을 권장합니다.

### 4) 실행
```bash
./gradlew bootRun
```

Windows 환경이라면:

```bash
gradlew.bat bootRun
```

기본 포트는 `8080`입니다.

## 데이터베이스 핵심 테이블
- `member`
- `delivery_address`
- `store`
- `store_menu`
- `menu_image`
- `order_room`
- `room_participant`
- `group_cart_item`
- `group_order`
- `group_order_item`
- `payment`
- `payment_share`
- `order_delivery`

## 화면 범위
- `FE-01` 홈 / 가게리스트
- `FE-02` 가게상세 / 메뉴목록
- `FE-03` 로그인 / 회원 / 주소록
- `FE-04` 주문방 생성
- `FE-05` 주문방 참여
- `FE-06` 주문방 실시간 협업
- `FE-07` 결제 진행
- `FE-08` 주문 결과 / 주문 상세
- `FE-09` 내 주문 내역
- `FE-10` 점주 주문대시보드
- `FE-11` 점주 주문상세 / 상태변경

## 상태값 기준

### 주문방
- `OPEN`
- `SELECTING`
- `PAYMENT_PENDING`
- `ORDER_CONFIRMED`
- `CANCELLED`
- `EXPIRED`

### 참여자
- `selection_status`: `NOT_SELECTED`, `SELECTED`
- `participant_role`: `LEADER`, `PARTICIPANT`
- `payment_status`: `UNPAID`, `PAID`

### 결제
- `payment_mode`: `REPRESENTATIVE`, `INDIVIDUAL`
- `payment_status`: `READY`, `IN_PROGRESS`, `PAID`, `CANCELLED`
- `share_status`: `PENDING`, `PAID_SELF`, `PAID_BY_REPRESENTATIVE`, `CANCELLED`

### 주문
- `order_status`: `PAYMENT_PENDING`, `PAID`, `ACCEPTED`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED`

## UI / UX 원칙
- 모바일 우선 반응형 구조를 기본으로 합니다.
- 카드형 정보 구조와 Sticky CTA를 우선합니다.
- 핵심 CTA는 오렌지, 구조 요소는 네이비 계열로 정리합니다.
- 내부 식별자(`*_idx`)는 화면에 직접 노출하지 않습니다.
- 점주 상태 변경은 허용된 다음 단계만 노출하는 순차 전환을 유지합니다.

## 프로젝트 방향
Mo-Eats는 단순 주문 페이지가 아니라,
**여럿이 함께 선택하고, 결제 부담을 분산하고, 점주 처리까지 흐름을 명확하게 연결하는 그룹 주문 경험**을 목표로 합니다.
