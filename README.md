# Mo-Eats Codex Skill Set Complete

이 패키지는 `work/integration-assembly-20260405` 계열 브랜치를 대상으로 Codex에서 안정적으로 백엔드 통합 점검과 디버깅을 수행하기 위한 실행용 세트다.

포함 구성:
- `AGENTS.md`: 저장소 루트 공통 지침
- `.agents/skills/moeats-backend-integration-debug/SKILL.md`: 핵심 디버깅 스킬
- `.agents/skills/moeats-backend-integration-debug/agents/openai.yaml`: Codex 앱용 메타데이터
- `.agents/skills/moeats-backend-integration-debug/scripts/*`: 점검 스크립트
- `.agents/skills/moeats-backend-integration-debug/references/*`: 기준 문서 요약
- `.codex/config.toml`: 예시 설정
- `codex/rules/default.rules`: 외부 실행 규칙 예시
- `cloud-setup.sh`: Codex cloud 환경 셋업 예시
- `cloud-maintenance.sh`: 캐시 재사용 시 점검 예시

권장 사용 순서:
1. 디버그용 복제 브랜치 생성
2. 이 패키지의 `AGENTS.md`와 `.agents/skills`를 저장소 루트에 반영
3. `.codex/config.toml`과 `codex/rules/default.rules`를 사용자 환경에 맞게 적용
4. Codex Cloud Environment에 `cloud-setup.sh`, 필요 시 `cloud-maintenance.sh` 등록
5. Codex에 아래처럼 지시

예시:
- `$moeats-backend-integration-debug work/integration-assembly-20260405-codex-debug 브랜치에서 build 실패 원인과 기준 문서 충돌을 먼저 리포트하고, 승인 없이 명명 변경은 하지 마라.`
- `$moeats-backend-integration-debug selected_delivery_address_idx nullable 처리, selection_status 혼용 여부, payment_expires_at / payment_share 흐름을 집중 점검해라.`
