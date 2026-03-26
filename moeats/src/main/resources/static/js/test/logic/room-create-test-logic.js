JavaScript
/**
 * /static/js/test/room-create-test-logic.js
 * 주문방 생성 테스트 전용 (대표결제 REPRESENTATIVE 전제)
 */
(function() {
    // DOM 로드 완료 후 실행
    window.addEventListener('load', function() {
        console.log("🚀 [테스트 모드] 주문방 생성 가로채기 활성화 (대표결제 전제)");

        const form = document.getElementById('roomCreateForm');

        if (form) {
            // 이벤트 캡처링(true)을 사용하여 기존 room-create.js보다 먼저 실행되게 함
            form.addEventListener('submit', function(e) {
                // 1. 서버 전송(405 에러 원인)을 즉시 중단
                e.preventDefault();
                e.stopImmediatePropagation();

                console.log("테스트 데이터 저장 시작...");

                // 2. 입력값 수집 (없을 경우를 대비한 기본 더미값 포함)
                const storeName = document.querySelector('strong[th\\:text]')?.innerText || "비상구 치킨 본점";
                const selectedAddr = document.getElementById('deliveryAddressIdx');
                const addressText = selectedAddr ? selectedAddr.options[selectedAddr.selectedIndex].text : "서울시 강남구 테스트동 777";
                
                const testData = {
                    storeName: storeName,
                    paymentMode: "REPRESENTATIVE", // 요청하신 대로 대표결제 고정
                    roomCode: "TEST-" + Math.floor(Math.random() * 900 + 100),
                    deliveryAddress: addressText,
                    minimumOrderAmount: 15000 // 기본 최소 주문 금액 설정
                };

                // 3. 로컬 스토리지에 데이터 보관 (detail 페이지에서 사용)
                localStorage.setItem('dummyRoom', JSON.stringify(testData));
                localStorage.setItem('myRole', 'LEADER'); // 생성자는 항상 방장

                alert("테스트: [" + testData.roomCode + "] 방이 생성되었습니다.\n상세 페이지로 이동합니다.");

                // 4. 상세 페이지(HTML 파일)로 직접 이동
                // 현재 URL 경로가 /templates/room/room-create.html 이므로 파일명만 지정
                location.href = "room-detail.html";
                
                return false;
            }, true); 
        }
    });
})();