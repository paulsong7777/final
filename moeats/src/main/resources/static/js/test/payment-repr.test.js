// payment.test.js
// 목적: 백엔드 없이 payment.html 화면 이동 및 타이머 테스트
// 실전 전환 시 이 파일 대신 payment.js를 로드

$(function () {

    console.log("🛠️ [테스트 모드] 대표 결제 로직 활성화");

    // 1. [더미] 결제하기 버튼 클릭 이벤트
    $('#btnTestSubmitPayment').on('click', function() {
        const method = $('input[name="dummyMethod"]:checked').parent().text().trim();
        
        if(confirm(`[테스트] ${method}(으)로 40,000원을 결제하시겠습니까?\n(실전: PG사 연동 후 POST /orders/{idx}/payment/complete)`)) {
            alert("결제가 완료되었습니다! \n모아결제이므로 방장의 결제 즉시 주문이 완료됩니다.");
            // 방장 모아결제 완료 시 바로 주문 현황(status)창으로 넘어감
            location.href = "order-status.html";
        }
    });

    // 2. [네비게이션] 테스트 이동 버튼
	// 2-1. [네비게이션] 대기실로 강제 이동	
	$('#btnTestGoWait').on('click', function() {
        location.href = "payment-wait.html";
    });
	
	// 2-2. [네비게이션] 뒤로 가기
    $('#btnTestGoDetail').on('click', function () {
        location.href = "room-detail.html";
    });

});
