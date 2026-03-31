// room-detail.js
// 주문방 협업 화면 전용 JS

document.addEventListener('DOMContentLoaded', function () {

    const roomIdx    = document.getElementById('roomIdx')?.value;
    const myRole     = document.getElementById('myRole')?.value;
    const roomStatus = document.getElementById('roomStatus')?.value;	

    // ── 방 코드 복사 ────────────────────────────────────────────────────────
    const btnCopyCode = document.getElementById('btnCopyCode');
    if (btnCopyCode) {
        btnCopyCode.addEventListener('click', function () {
            const code = this.dataset.code;
            if (!code) return;
            navigator.clipboard.writeText(code)
                .then(function () { alert('방 코드가 복사되었습니다: ' + code); })
                .catch(function () {
                    // clipboard API 미지원 환경 fallback
                    prompt('코드를 직접 복사하세요:', code);
                });
        });
    }

    // ── 결제 진행 버튼 상태 안내 ────────────────────────────────────────────
    const btnProceed = document.getElementById('btnProceedPayment');
    if (btnProceed && btnProceed.disabled) {
        btnProceed.addEventListener('mouseenter', function () {
            alert('모든 참여자가 선택을 완료하고 최소 주문 금액을 충족해야 결제를 진행할 수 있습니다.');
        });
    }

    // ── WebSocket 연결 (STOMP) ───────────────────────────────────────────────
    // 실제 연동 시 sockjs-client + stomp.js 사용
    // 현재는 테스트용 폴링으로 대체 (3초마다 페이지 갱신)
    if (roomStatus === 'OPEN' || roomStatus === 'SELECTING') {
        startPolling();
    }

    function startPolling() {
        // 테스트 환경에서는 폴링 비활성화 (주석 해제 시 3초마다 갱신)
        /*
        setInterval(function () {
            location.reload();
        }, 3000);
        */
        console.log('[room-detail] 폴링 준비 완료. 실제 연동 시 WebSocket으로 교체 예정.');
    }

    // ── 방 나가기 확인 ──────────────────────────────────────────────────────
    // (th:onclick으로 처리하지 않고 JS로 위임)
    const leaveForm = document.querySelector('form[action*="/members/me"]');
    if (leaveForm) {
        leaveForm.addEventListener('submit', function (e) {
            if (!confirm('주문방에서 나가시겠습니까?')) {
                e.preventDefault();
            }
        });
    }	
		
});
