// payment.js
// 결제 진행 화면 전용 JS
// SSEService.join(roomIdx) 가 연결을 수락
// SSEService.send(roomIdx, event) 가 결제 현황 변경을 브로드캐스트

document.addEventListener('DOMContentLoaded', function () {

	const paymentMode      = document.getElementById('paymentMode')?.value;
	    const paymentStartedAt = document.getElementById('paymentStartedAt')?.value;
	    const orderIdx         = document.getElementById('orderIdx')?.value;
	    const roomCode         = document.getElementById('roomCode')?.value;

	// ── 5분 타이머 (INDIVIDUAL 각자결제 전용) ────────────────────────────────
    // 타이머는 JS에서 독립적으로 돌아감 (서버 시각 기준점: paymentStartedAt)
    // 타이머 만료 시 서버가 자동 취소 처리하고 SSE로 paymentCancelled 이벤트 전송
    if (paymentMode === 'INDIVIDUAL') {
        startCountdown(paymentStartedAt);
	    }

    function startCountdown(startedAtStr) {
        const countdownEl = document.getElementById('countdown');
        if (!countdownEl) return;

        const LIMIT_MS = 5 * 60 * 1000; // 5분
        const startedAt = startedAtStr ? new Date(startedAtStr).getTime() : Date.now();

		const timer = setInterval(function () {
            const remaining = LIMIT_MS - (Date.now() - startedAt);

            if (remaining <= 0) {
                clearInterval(timer);
                countdownEl.textContent = '00:00';
				alert("결제 시간이 만료되었습니다. 주문방으로 돌아갑니다.");
                // 만료 alert는 SSE paymentCancelled 이벤트 수신 시 처리
                // (타이머 만료와 서버 취소 처리 시점이 다를 수 있으므로 SSE 우선)
				// location.href = "/rooms/code/" + document.getElementById('roomCode').value;
                return;
            }

            const minutes = Math.floor(remaining / 60000);
            const seconds = Math.floor((remaining % 60000) / 1000);
            countdownEl.textContent =
                String(minutes).padStart(2, '0') + ':' +
                String(seconds).padStart(2, '0');

            if (remaining <= 60000) {
                countdownEl.style.color = 'red';
            }
        }, 1000);
    }
	
	// 결제 폼 유효성 검사
    const individualForm = document.getElementById('individualPaymentForm');
    if (individualForm) {
        individualForm.addEventListener('submit', function (e) {
            if (!document.querySelector('input[name="payMethod"]:checked')) {
                e.preventDefault();
                alert("결제 수단을 선택해 주세요.");
            }
        });
    }
	
	// ── 결제 폼 중복 제출 방지 ───────────────────────────────────────────────
    [document.getElementById('formPayLeader'),
     document.getElementById('formPayIndividual')
    ].forEach(function (form) {
        if (!form) return;
        form.addEventListener('submit', function () {
            const btn = form.querySelector('button[type="submit"]');
            if (btn) {
                btn.disabled    = true;
                btn.textContent = '처리 중...';
            }
        });
    });

});
