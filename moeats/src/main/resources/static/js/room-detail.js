// room-detail.js
// 주문방 협업 화면 전용 실전 JS

document.addEventListener('DOMContentLoaded', function () {

    const roomIdx    = document.getElementById('roomIdx')?.value;
    const roomCode   = document.getElementById('roomCode')?.value;  // URL에 사용
    const myRole     = document.getElementById('myRole')?.value;
    const roomStatus = document.getElementById('roomStatus')?.value;

    // ── 방 코드 복사 ─────────────────────────────────────────────────────────
    const btnCopyCode = document.getElementById('btnCopyCode');
    if (btnCopyCode) {
        btnCopyCode.addEventListener('click', function () {
            const code = this.dataset.code;
            if (!code) return;
            navigator.clipboard.writeText(code)
                .then(function () { alert('방 코드가 복사되었습니다: ' + code); })
                .catch(function () {
                    prompt('코드를 직접 복사하세요:', code);
                });
        });
    }

    // ── 결제 진행 버튼 비활성화 안내 ─────────────────────────────────────────
    const btnProceed = document.getElementById('btnProceedPayment');
    if (btnProceed && btnProceed.disabled) {
        btnProceed.addEventListener('mouseenter', function () {
            alert('모든 참여자가 선택을 완료하고 최소 주문 금액을 충족해야 결제를 진행할 수 있습니다.');
        });
    }

	// ── KICK (강제 퇴장) ─────────────────────────────────────────────────────
    // 실전 동작: 방장만 접근 가능 (서버에서도 leaderMemberIdx로 재검증)
    // method : POST /rooms/code/{roomCode}/kick
    // 파라미터: memberIdx (form hidden input)
    // 성공   : redirect:/rooms/code/{roomCode} (현재 페이지 갱신)
    document.querySelectorAll('.formKick').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            const name = this.dataset.name || '이 참여자';
            if (!confirm(name + '님을 방에서 내보내시겠습니까?')) {
                e.preventDefault();
            }
        });
    });
	
	// SELECT(선택완료) / UNSELECT(선택취소) 방어 로직 (선택적 추가)
	const selectForm = document.querySelector('form[action$="/select"]');
	if(selectForm) {
	    selectForm.addEventListener('submit', function(e) {
	        if(!confirm('메뉴 선택을 완료하시겠습니까? 완료 후에는 장바구니를 수정할 수 없습니다.')) {
	            e.preventDefault();
	        }
	    });
	}
	
    // ── 방 나가기 확인 ───────────────────────────────────────────────────────
    // 수정: form action이 /leave 를 포함하도록 선택자 변경
    // (기존: form[action*="/members/me"] → 수정: form[action*="/leave"])
    const leaveForm = document.getElementById('formLeaveRoom');
    if (leaveForm) {
        leaveForm.addEventListener('submit', function (e) {
            if (!confirm('주문방에서 나가시겠습니까?')) {
                e.preventDefault();
            }
        });
    }

    // ── WebSocket 연결 (STOMP) ────────────────────────────────────────────────
    // 실제 연동 시 sockjs-client + stomp.js 사용
    // 현재는 폴링 비활성화 (주석 해제 시 3초마다 참여자 현황 갱신)
    if (roomStatus === 'OPEN' || roomStatus === 'SELECTING') {
        startPolling();
    }

    function startPolling() {
        /*
        setInterval(function () {
            // location.reload() 대신 필요한 데이터만 AJAX로 가져와 DOM 업데이트
            $.get('/rooms/code/' + roomCode + '/status', function (data) {
                // 참여자 현황 테이블 업데이트 예정
            });
        }, 3000);
        */
        console.log('[room-detail] 폴링 준비 완료. 실제 연동 시 WebSocket으로 교체 예정.');
    }

});
