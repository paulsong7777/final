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
                // 만료 alert는 SSE paymentCancelled 이벤트 수신 시 처리
                // (타이머 만료와 서버 취소 처리 시점이 다를 수 있으므로 SSE 우선)
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

	// ── SSE 연결 (실시간 결제 현황 갱신) ────────────────────────────────────
    // SSEService.join(roomIdx) 에 연결 → roomMap 에 등록됨
    // 결제 화면도 같은 방(roomIdx)의 SSE 채널을 공유
    // 백엔드 Controller: room-detail과 동일한 엔드포인트 재사용
    //   GET /rooms/code/{room_code}/sse → sseService.join(roomIdx)
    //
    // 수신 이벤트 목록 (백엔드가 SSEService.send()로 전송):
    //   "connect"         - 최초 연결 확인 
    //   "paymentUpdate"    - 결제 완료 인원 변경 시
    //                        data: JSON { paidCount, totalCount, shares: [...] }
    //   "paymentCompleted" - 전원 결제 완료 시 (대표결제 포함)
    //                        data: JSON { orderIdx }
    //                        → /orders/{orderIdx}/complete 로 이동
    //   "paymentCancelled" - 5분 타임아웃 또는 결제 취소 시
    //                        data: JSON { roomCode }
    //                        → 취소 화면으로 이동 + 타이머 정지
    //
    // 단방향이므로 실제 결제 요청은 기존 HTTP form POST 그대로 유지
	
	if (paymentMode === 'INDIVIDUAL' || paymentMode === 'REPRESENTATIVE') {
        startSSE();
    }
	
	function startSSE() {
        const eventSource = new EventSource('/rooms/code/' + roomCode + '/sse');

        // 최초 연결 확인
        // 주의: SSEService에서 이벤트명을 "connnect"(n 3개)로 전송하므로 반드시 맞춰야 함
        eventSource.addEventListener('connnect', function (e) {
            console.log('[SSE] 결제화면 연결 성공:', e.data);
        });

        // 결제 현황 변경 (누군가 결제 완료할 때마다 수신)
        // data 형식: JSON { paidCount, totalCount, shares: [{ memberName, amount, status }] }
        eventSource.addEventListener('paymentUpdate', function (e) {
            const data = JSON.parse(e.data);
            updatePaymentStatusTable(data.shares, data.paidCount, data.totalCount);
        });

        // 전원 결제 완료 → 주문 완료 화면 이동
        // data 형식: JSON { orderIdx }
        eventSource.addEventListener('paymentCompleted', function (e) {
            const data = JSON.parse(e.data);
            eventSource.close();
            location.href = '/orders/' + data.orderIdx + '/complete';
        });

        // 결제 취소 (타임아웃 또는 수동 취소)
        // data 형식: JSON { roomCode }
        eventSource.addEventListener('paymentCancelled', function (e) {
            const data = JSON.parse(e.data);
            eventSource.close();
            alert('결제가 취소되었습니다. 주문방으로 돌아갑니다.');
            location.href = '/rooms/code/' + data.roomCode;
        });

        eventSource.onerror = function (e) {
            console.warn('[SSE] 연결 오류 또는 재연결 중:', e);
        };

        window.addEventListener('beforeunload', function () {
            eventSource.close();
        });
    }

    // ── 결제 현황 테이블 DOM 갱신 함수 (SSE paymentUpdate 수신 시 호출) ──────
    // 예상 데이터 구조: shares = [{ memberName, amount, status: 'PAID'|'PENDING' }]
    function updatePaymentStatusTable(shares, paidCount, totalCount) {
        const tbody = document.querySelector('#paymentStatus table tbody');
        if (!tbody || !shares) return;

        tbody.innerHTML = shares.map(function (s) {
            const statusLabel = s.status === 'PAID' ? '[완료]' : '[미결제]';
            return `<tr>
                        <td>${s.memberName}</td>
                        <td>${(s.amount || 0).toLocaleString()}원</td>
                        <td>${statusLabel}</td>
                    </tr>`;
        }).join('');

        // 완료/전체 카운트 갱신
        const paidEl  = document.querySelector('#paymentStatus p span:first-child');
        const totalEl = document.querySelector('#paymentStatus p span:last-child');
        if (paidEl)  paidEl.textContent  = paidCount  || 0;
        if (totalEl) totalEl.textContent = totalCount || 0;
    }
	
	// 구형코드	
    // ── 결제 현황 폴링 (WebSocket 연동 전 임시) ─────────────────────────────
    // 실제 연동 시 WebSocket STOMP 이벤트로 교체
    // if (paymentMode === 'INDIVIDUAL') {
    //    startPaymentStatusPolling();
    // }

    // function startPaymentStatusPolling() {
    //    // 테스트 환경에서는 비활성화 (주석 해제 시 5초마다 갱신)
    //    /*
    //    setInterval(function () {
    //        location.reload();
    //    }, 5000);
    //    */
    //    console.log('[payment] 결제 현황 폴링 준비 완료. 실제 연동 시 WebSocket으로 교체 예정.');
    //	}
});
