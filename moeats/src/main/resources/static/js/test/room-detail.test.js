// room-detail.test.js
// 목적: 백엔드 없이 room-detail.html 화면 이동 및 기본 동작 테스트
// 실전 전환 시 이 파일 대신 room-detail.js를 로드

$(function () {

	const testAddBtn = document.getElementById('btnTestAddRow');
	
    // 방 코드 복사
    // 실전: 동일 로직 그대로 사용 가능
    $('#btnCopyCode').on('click', function () {
        var code = $(this).data('code');
        if (!code) return;
        if (navigator.clipboard) {
            navigator.clipboard.writeText(code).then(function () {
                alert('방 코드 복사 완료: ' + code);
            });
        } else {
            prompt('코드를 직접 복사하세요:', code);
        }
    });

    // 선택 완료 버튼 (더미)
    // 실전: POST /rooms/{roomIdx}/members/{memberIdx} (_method=PATCH, selectionStatus=SELECTED)
    $('#btnTestSelectDone').on('click', function () {
        alert('[더미] 선택 완료 처리. 실전: PATCH /rooms/{roomIdx}/members/{memberIdx}');
        $(this).prop('disabled', true).text('[선택 완료됨]');
    });

    // 결제 진행하기 (더미)
    // 실전: POST /rooms/{roomIdx}/orders → redirect to payment.html
    $('#btnTestProceedPayment').on('click', function () {
        if (!confirm('결제를 진행합니다. 방이 잠기고 결제 화면으로 이동합니다.')) return;
        location.href = "payment-test.html";
    });

    // 방 나가기 (더미)
    // 실전: POST /rooms/{roomIdx}/members/me (_method=DELETE) → redirect to /
    $('#btnTestLeaveRoom').on('click', function () {
        if (!confirm('주문방에서 나가시겠습니까?')) return;
        alert('[더미] 방 나가기 처리. 실전: DELETE /rooms/{roomIdx}/members/me');
        location.href = '/';
    });
	
	// "메뉴 추가" 버튼 클릭 시 이동 (더미) (기존 a태그나 버튼에 연결)
    // room-detail.html에 있는 '+ 메뉴 추가' 버튼의 ID나 클래스를 타겟팅합니다.
    $(document).on('click', '#btnGoAddMenu, a[href*="stores"] button', function(e) {
        e.preventDefault();
        console.log("테스트: 메뉴 선택 페이지로 이동합니다.");
        location.href = "cart-item.html";
    });

    // 테스트 이동 버튼
    $('#btnTestGoPayment').on('click', function () {
        location.href = "payment-test.html";
    });
    $('#btnTestGoCreate').on('click', function () {
        location.href = "room-create.html";
    });
    $('#btnTestGoJoin').on('click', function () {
        location.href = "room-join.html";
    });
	$('#btnTestGoDetailDesign').on('click', function () {
        location.href = "room-detail-designtest.html";
    });
	
	// ── 메뉴에 더미행 추가 ──────────────────────────────────────────────────────	
	// 1. 더미 행 추가 버튼 이벤트
    $('#btnTestAddRow').on('click', function () {
        const dummyMenus = [
            { name: "치즈김밥", price: 4500 },
            { name: "라볶이", price: 6500 },
            { name: "돈까스", price: 9500 }
        ];
        const item = dummyMenus[Math.floor(Math.random() * dummyMenus.length)];

        const newRow = `
            <tr>
                <td>${item.name}</td>
                <td>1</td>
                <td>${item.price.toLocaleString()}원</td>
                <td class="subtotal">${item.price.toLocaleString()}원</td>
                <td>
                    <button type="button">수정 (더미)</button>
                    <button type="button" class="btn-delete-test">삭제 (더미)</button>
                </td>
            </tr>`;

        $('#myCartSection tbody').append(newRow);
        
        // ★ 행 추가 후 합계 업데이트
        updateAllTotals();
    });

    // 2. 삭제 버튼 이벤트 (동적 생성 요소 대응)
    $(document).on('click', '.btn-delete-test', function() {
        $(this).closest('tr').remove();
        
        // ★ 행 삭제 후 합계 업데이트
        updateAllTotals();
    });

    // 3. [핵심] 모든 금액 합산 및 화면 반영 함수
    function updateAllTotals() {
        let myTotal = 10000; // 디폴트화면이 10000원임

        // '내 장바구니' 섹션의 소계(.subtotal)들 합산
        $('#myCartSection .subtotal').each(function() {
            const val = parseInt($(this).text().replace(/[^0-9]/g, '')) || 0;
            myTotal += val;
        });

        // 화면 반영: 내 주문 합계
        $('#myCartSection strong').text(myTotal.toLocaleString() + "원");
		$('#moneyCal').text(myTotal.toLocaleString() + "원");

        // 화면 반영: 전체 합계 섹션 (실제로는 다른 참여자 금액도 더해야 하지만 테스트용으로 연동)
        // 여기서는 예시로 내 합계에 배달비 3,000원을 더해 최종 합계를 갱신해봅니다.
        const deliveryFee = 3000;
        const grandTotal = 21500 + myTotal + deliveryFee; // 디폴트화면이 12000+9500+10000(myTotal)임

        $('#totalSection table tr:nth-child(1) td').text(myTotal.toLocaleString() + "원"); // 메뉴 합계
        $('#totalSection strong').text(grandTotal.toLocaleString() + "원"); // 최종 합계
        
        // 최소 주문 금액 미달 여부 체크 (더미 최소금액 12,000원 기준)
        if(grandTotal < 40000) {
            console.log("최소 주문 금액 미달!");
        }
    }

});
