// payment.js
// 결제 진행 화면 전용 JS

document.addEventListener('DOMContentLoaded', function () {

    const paymentMode      = document.getElementById('paymentMode')?.value;
    const paymentStartedAt = document.getElementById('paymentStartedAt')?.value;

    // ── 5분 타이머 (INDIVIDUAL 각자결제 전용) ──────────────────────────────
    if (paymentMode === 'INDIVIDUAL') {
        startCountdown(paymentStartedAt);
    }

    function startCountdown(startedAtStr) {
        const countdownEl = document.getElementById('countdown');
        if (!countdownEl) return;

        const LIMIT_MS = 5 * 60 * 1000; // 5분
        const startedAt = startedAtStr ? new Date(startedAtStr).getTime() : Date.now();

        const timer = setInterval(function () {
            const elapsed  = Date.now() - startedAt;
            const remaining = LIMIT_MS - elapsed;

            if (remaining <= 0) {
                clearInterval(timer);
                countdownEl.textContent = '00:00';
                alert('결제 시간이 만료되었습니다. 주문이 자동 취소됩니다.');
                // 자동 취소 화면으로 이동
                const orderIdx = document.getElementById('orderIdx')?.value;
                if (orderIdx) {
                    location.href = '/rooms/' + orderIdx + '/cancelled';
                }
                return;
            }

            const minutes = Math.floor(remaining / 60000);
            const seconds = Math.floor((remaining % 60000) / 1000);
            countdownEl.textContent =
                String(minutes).padStart(2, '0') + ':' +
                String(seconds).padStart(2, '0');

            // 1분 이하 경고
            if (remaining <= 60000) {
                countdownEl.style.color = 'red';
            }
        }, 1000);
    }

    // ── 결제 폼 중복 제출 방지 ──────────────────────────────────────────────
    const payForms = [
        document.getElementById('formPayLeader'),
        document.getElementById('formPayIndividual')
    ];

    payForms.forEach(function (form) {
        if (!form) return;
        form.addEventListener('submit', function (e) {
            const btn = form.querySelector('button[type="submit"]');
            if (btn) {
                btn.disabled = true;
                btn.textContent = '처리 중...';
            }
        });
    });

    // ── 결제 현황 폴링 (WebSocket 연동 전 임시) ─────────────────────────────
    // 실제 연동 시 WebSocket STOMP 이벤트로 교체
    if (paymentMode === 'INDIVIDUAL') {
        startPaymentStatusPolling();
    }

    function startPaymentStatusPolling() {
        // 테스트 환경에서는 비활성화 (주석 해제 시 5초마다 갱신)
        /*
        setInterval(function () {
            location.reload();
        }, 5000);
        */
        console.log('[payment] 결제 현황 폴링 준비 완료. 실제 연동 시 WebSocket으로 교체 예정.');
    }
});
