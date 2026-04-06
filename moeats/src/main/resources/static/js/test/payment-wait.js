// payment.test.js
// 목적: 백엔드 없이 payment.html 화면 이동 및 타이머 테스트
// 실전 전환 시 이 파일 대신 payment.js를 로드

$(function () {    

    // 각자결제 - 내 금액 결제하기 (더미)
    // 실전: POST /orders/{orderIdx}/payment/complete (paymentMode=INDIVIDUAL, paymentMethod=선택값)
   /*
	 $('#btnTestPayIndividual').on('click', function () {
        var method = $('#payMethodIndividual_dummy').val();
        if (!confirm('결제 수단 [' + method + ']으로 결제하시겠습니까?')) return;
        alert('[더미] 결제 완료 처리. 실전: POST /orders/{orderIdx}/payment/complete');
        location.href = '/orders/test-order/complete';
    });
	*/


    // 테스트 이동 버튼
    $('#btnTestGoComplete').on('click', function () {
        location.href = '/orders/test-order/complete';
    });
    $('#btnTestGoCancelled').on('click', function () {
        location.href = '/rooms/test-room/cancelled';
    });
    $('#btnTestGoDetail').on('click', function () {
        location.href = "room-detail.html";
    });
	$('#btnTestGoPayRepr').on('click', function () {
        location.href = "payment-repr-test.html";
    });
	$('#btnTestGoPayIndi').on('click', function () {
        location.href = "payment-indi-test.html";
    });

});
