// room-detail.test.js
// 목적: 백엔드 없이 room-detail.html 화면 이동 및 기본 동작 테스트
// 실전 전환 시 이 파일 대신 room-detail.js를 로드

$(function () {

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
        location.href = '/payment/test-order';
    });

    // 방 나가기 (더미)
    // 실전: POST /rooms/{roomIdx}/members/me (_method=DELETE) → redirect to /
    $('#btnTestLeaveRoom').on('click', function () {
        if (!confirm('주문방에서 나가시겠습니까?')) return;
        alert('[더미] 방 나가기 처리. 실전: DELETE /rooms/{roomIdx}/members/me');
        location.href = '/';
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

});
