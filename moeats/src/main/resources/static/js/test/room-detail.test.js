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
	
	// ── KICK 더미 처리 ───────────────────────────────────────────────────────
    // 실전: .formKick의 submit → POST /rooms/code/{roomCode}/kick (memberIdx 전송)
    // 더미: 해당 참여자 행 제거 + 해당 참여자의 장바구니 행도 함께 제거
    $(document).on('click', '.btnKickTest', function () {
        const name = $(this).data('name');
        if (!confirm(name + '님을 방에서 내보내시겠습니까?')) return;

        const $participantRow = $(this).closest('tr');
        const memberIdx = $participantRow.data('member-idx');

        // 참여자 테이블 행 제거
        $participantRow.remove();

        // 해당 참여자의 장바구니 행 제거 (data-member-idx 매칭)
        $('#myCartSection tbody tr[data-member-idx="' + memberIdx + '"]').remove();

        alert('[더미] ' + name + '님이 내보내졌습니다.\n실전: POST /rooms/code/{roomCode}/kick (memberIdx=' + memberIdx + ')');
    });
	
	// ── 임의 참여자 추가 (테스트 전용) ──────────────────────────────────────
    const dummyParticipants = [
        { name: '김철남', idx: 10, menus: [{ name: '김치찌개', qty: 1, price: 8000 }] },
        { name: '정한입', idx: 11, menus: [{ name: '제육볶음', qty: 1, price: 9000 }, { name: '공기밥', qty: 1, price: 1000 }] },
        { name: '강줄서', idx: 12, menus: [{ name: '돈까스', qty: 2, price: 10000 }] },
        { name: '윤참고', idx: 13, menus: [{ name: '라볶이', qty: 1, price: 7000 }, { name: '튀김세트', qty: 1, price: 5000 }] },
        { name: '한참뒤', idx: 14, menus: [{ name: '순대국밥', qty: 1, price: 8000 }] },
    ];
    let addedCount = 0;

    $('#btnTestAddParticipant').on('click', function () {
        if (addedCount >= dummyParticipants.length) {
            alert('추가할 수 있는 더미 참여자가 없습니다. (최대 ' + dummyParticipants.length + '명)');
            return;
        }

        const p = dummyParticipants[addedCount];
        addedCount++;

        // 1. 참여자 테이블에 행 추가
        const cartTotal = p.menus.reduce((sum, m) => sum + m.price * m.qty, 0);
        const participantRow = `
            <tr data-role="PARTICIPANT" data-member-idx="${p.idx}">
                <td>${p.name}</td>
                <td>참여자</td>
                <td>[선택중]</td>
                <td class="participantCartTotal">${cartTotal.toLocaleString()}원</td>
                <td><button type="button" class="btnKickTest" data-name="${p.name}">내보내기 (더미)</button></td>
            </tr>`;
        $('#participantTbody').append(participantRow);

        // 2. 타 참여자의 주문 행을 '팀 장바구니 섹션'에 추가 (수정됨)
        p.menus.forEach(function (menu) {
            const subtotal = menu.price * menu.qty;
            const cartRow = `
                <tr data-member-idx="${p.idx}" data-member-name="${p.name}">
                    <td>${menu.name}</td>
                    <td>${menu.qty}</td>
                    <td>${menu.price.toLocaleString()}원</td>
                    <td class="team-subtotal">${subtotal.toLocaleString()}원</td> <td><span style="color: gray; font-size: 0.85em;">${p.name}</span></td>
                </tr>`;
            $('#teamCartSection tbody').append(cartRow); // 타겟 변경
        });

        // 3. 합계 갱신
        updateAllTotals();

        console.log('[테스트] 참여자 추가:', p.name, '(memberIdx:', p.idx, ')');
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
	$('#btnTestGoPayRepr').on('click', function () {
        location.href = "payment-repr-test.html";
    });
	$('#btnTestGoPayIndi').on('click', function () {
        location.href = "payment-indi-test.html";
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
                <td class="my-subtotal">${item.price.toLocaleString()}원</td> <td>
                    <button type="button">수정 (더미)</button>
                    <button type="button" class="btn-delete-test">삭제 (더미)</button>
                </td>
            </tr>`;

        $('#myCartSection tbody').append(newRow);
        
        updateAllTotals();
    });

    // 2. 삭제 버튼 이벤트
    $(document).on('click', '.btn-delete-test', function() {
        $(this).closest('tr').remove();
        updateAllTotals();
    });

    // 3. [핵심] 모든 금액 합산 및 화면 반영 함수 (완전 수정됨)
    function updateAllTotals() {
        let myTotal = 0;
        let teamTotal = 0;

        // '내 장바구니' 소계(.my-subtotal) 계산
        $('#myCartSection .my-subtotal').each(function() {
            const val = parseInt($(this).text().replace(/[^0-9]/g, '')) || 0;
            myTotal += val;
        });

        // '팀 장바구니' 소계(.team-subtotal) 계산
        $('#teamCartSection .team-subtotal').each(function() {
            const val = parseInt($(this).text().replace(/[^0-9]/g, '')) || 0;
            teamTotal += val;
        });

        // 화면 반영: 내 주문 합계
        $('#myCartTotal').text(myTotal.toLocaleString() + "원");
        $('#moneyCal').text(myTotal.toLocaleString() + "원"); // 참여자 목록의 내 금액 갱신

        // 화면 반영: 팀 주문 합계
        $('#teamCartTotal').text(teamTotal.toLocaleString() + "원");

        // 화면 반영: 전체 합계 섹션
        const totalMenuAmount = myTotal + teamTotal; // 메뉴 전체 합계
        const deliveryFee = 3000;
        const grandTotal = totalMenuAmount + deliveryFee;

        // totalSection 업데이트
        $('#totalSection table tr:nth-child(1) td').text(totalMenuAmount.toLocaleString() + "원"); // 메뉴 합계
        $('#totalSection strong').text(grandTotal.toLocaleString() + "원"); // 최종 합계
        
        // 최소 주문 금액 미달 여부 체크 (더미 최소금액 40,000원 기준)
        if(grandTotal < 40000) {
            console.log("최소 주문 금액 미달!");
        }
    }

    // 문서 로드 시 초기 합계 세팅 실행
    updateAllTotals();

});
