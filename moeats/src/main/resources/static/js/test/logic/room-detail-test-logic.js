/**
 * /static/js/test/room-detail-test-logic.js
 * 주문방 상세 테스트 전용 (데이터 바인딩 및 초기화)
 */
$(function() {
    console.log("🚀 [테스트 모드] 상세 페이지 데이터 바인딩 시작");

    // 1. 로컬 스토리지에서 데이터 읽기
    const saved = localStorage.getItem('dummyRoom');
    const role = localStorage.getItem('myRole') || 'LEADER';

    if (saved) {
        const data = JSON.parse(saved);
        
        // 2. 화면 문구 교체 (Thymeleaf 변수 자리에 데이터 주입)
        // 가게 이름
        $('h2 span').text(`[${data.storeName}]`);
        // 방 코드 및 복사 데이터
        $('strong:contains("A7B2X9")').text(data.roomCode);
        $('#btnCopyCode').attr('data-code', data.roomCode);
        // 결제 방식
        $('td:contains("각자결제")').text("대표결제");
        // 배송지
        $('td:contains("서울시 강남구 ...")').text(data.deliveryAddress);
        // 최소 주문 금액
        $('td:contains("0원")').last().text(data.minimumOrderAmount.toLocaleString() + "원");

        // 3. 기존 room-detail.js가 읽을 수 있도록 Hidden Input 값 강제 세팅
        // (값이 없을 경우를 대비해 value 업데이트)
        $('#myRole').val(role);
        $('#roomStatus').val('SELECTING');
        
        // 4. 참여자 더미 데이터 생성 (화면이 비어보이지 않게 함)
        if ($('#participantSection tbody tr').length <= 1) {
            const dummyParticipant = `
                <tr>
                    <td>김방장(나)</td>
                    <td>방장</td>
                    <td><span>[선택중]</span></td>
                    <td>0원</td>
                </tr>
            `;
            $('#participantSection tbody').html(dummyParticipant);
        }

        console.log("✅ 테스트 데이터 적용 완료 (역할: " + role + ")");
    } else {
        console.warn("표시할 테스트 데이터가 없습니다. 생성 페이지부터 시작하세요.");
    }
});