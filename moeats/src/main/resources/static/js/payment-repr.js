// payment-representative.js
// 모아결제(대표결제) 방장 화면 전용 실전 JS

document.addEventListener('DOMContentLoaded', function () {
    console.log("🚀 payment-representative.js (실전용) 로드 완료");

    const paymentForm = document.getElementById('paymentForm');

    if (paymentForm) {
        paymentForm.addEventListener('submit', function (e) {
            // 결제 수단 선택 여부 확인
            const selectedMethod = document.querySelector('input[name="payMethod"]:checked');
            
            if (!selectedMethod) {
                e.preventDefault(); // 폼 제출 막기
                alert("결제 수단을 선택해 주세요.");
                return;
            }

            // 실전에서는 여기에 PG사(아임포트/토스페이먼츠 등) SDK 호출 로직이 들어갈 수 있습니다.
            // PG사 결제 성공 콜백 내부에서 서버로 POST 요청을 보내도록 처리해야 합니다.
            if(!confirm('결제를 진행하시겠습니까?')) {
                e.preventDefault();
            }
        });
    }
});