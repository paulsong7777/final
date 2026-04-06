// payment.test.js
// 목적: 백엔드 없이 payment-wait.html 화면 이동 및 타이머 테스트
// 실전 전환 시 이 파일 대신 payment-wait.js를 로드

$(function() {
    console.log("🛠️ [테스트 모드] 개인 결제 기능 활성화");

    // 1. [더미] 내 금액 결제하기 버튼
    $('#btnTestSubmitIndividual').on('click', function() {
        const method = $('input[name="dummyMethod"]:checked').parent().text().trim();
        
        if(confirm(`[테스트] 내 몫(21,000원)을 ${method}(으)로 결제하시겠습니까?`)) {
            alert("결제 성공! 다른 팀원들이 결제할 때까지 대기실로 이동합니다.");
            // 개인 결제 완료 후에는 다른 사람을 기다리는 Wait 페이지로 이동
            location.href = "payment-wait.html";
        }
    });
	
	// 2. 결제 취소 (더미)
    // 실전: GET /rooms/{roomIdx}/cancelled
    $('#btnTestCancel').on('click', function () {
        if (!confirm('결제를 취소하시겠습니까? 주문 전체가 취소될 수 있습니다.')) return;
        location.href = 'room-detail.html';
    });

    // 3. [네비게이션] 결제완료창으로 강제 이동
    $('#btnTestGoComp').on('click', function() {
        location.href = "payment-comp.html";
    });

    // 4. [네비게이션] 뒤로 가기
    $('#btnTestGoBack').on('click', function() {
        location.href = "room-detail.html";
    });
	
	// 5. 5분 타이머 (더미: paymentStartedAt이 비어있으면 지금 시각 기준으로 시작)
    // 실전: order.paymentStartedAt 값을 hidden input에서 읽어 서버 기준 시각 사용
    var paymentMode = $('#paymentMode').val();
    if (paymentMode === 'INDIVIDUAL') {
        var startedAtStr = $('#paymentStartedAt').val();
        var startedAt = startedAtStr ? new Date(startedAtStr).getTime() : Date.now();
        startCountdown(startedAt);
    }

    function startCountdown(startedAt) {
        var LIMIT_MS = 5 * 60 * 1000;
        var timer = setInterval(function () {
            var remaining = LIMIT_MS - (Date.now() - startedAt);
            if (remaining <= 0) {
                clearInterval(timer);
                $('#countdown').text('00:00').css('color', 'red');
                alert('[더미] 타임아웃! 실전: group_order CANCELLED 처리 후 취소 화면으로 이동');
                location.href = 'room-detail.html';
                return;
            }
            var m = Math.floor(remaining / 60000);
            var s = Math.floor((remaining % 60000) / 1000);
            var display = String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
            $('#countdown').text(display);
            if (remaining <= 60000) $('#countdown').css('color', 'red');
        }, 1000);
    }
});
