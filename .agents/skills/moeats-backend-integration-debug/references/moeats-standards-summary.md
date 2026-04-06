# Mo-Eats Standards Summary

## Core naming
- selection_status
- payment_mode = REPRESENTATIVE / INDIVIDUAL
- participant_role = LEADER / PARTICIPANT
- selected_delivery_address_idx
- store_category
- store_menu_category

## Tier 1 flow
방 생성 -> 참여 -> 협업 -> 결제 -> 점주 처리 -> 조회

## FE priority
- FE-04 주문방 생성
- FE-06 주문방 실시간 협업
- FE-07 결제 진행
- FE-11 점주 주문상세 / 상태변경

## Required business rules
- selected_delivery_address_idx nullable 유지
- default_delivery_address_idx 기준 기본 배송지 판단
- store_category 업종 필터
- store_menu_category 섹션형 메뉴 노출
- selection_status는 선택완료 시 SELECTED, 수정 시 NOT_SELECTED 복귀
- payment_started_at ~ payment_expires_at 5분 타이머
- payment_share 전원 성공 집계 후 주문 생성
- OWNER 상태 전환은 ACCEPTED -> PREPARING -> READY -> COMPLETED
- Tier 3 상태 DELIVERING / CHECKED_IN 기본 노출 금지
