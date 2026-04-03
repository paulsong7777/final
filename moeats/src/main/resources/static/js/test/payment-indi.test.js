// payment.test.js
// 목적: 백엔드 없이 payment.html 화면 이동 및 타이머 테스트
// 실전 전환 시 이 파일 대신 payment.js를 로드

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

    // 2. [네비게이션] 대기실로 강제 이동
    $('#btnTestGoWait').on('click', function() {
        location.href = "payment-wait.html";
    });

    // 3. [네비게이션] 뒤로 가기
    $('#btnTestGoBack').on('click', function() {
        location.href = "room-detail.html";
    });
});
