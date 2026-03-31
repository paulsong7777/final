// room-create.test.js
// 목적: 백엔드 없이 room-create.html 화면 이동 테스트
// 실전 전환 시 이 파일 대신 room-create.js를 로드

$(function () {

    // 주문방 협업화면으로 이동 (더미 roomIdx=1 사용)
    // 실전: POST /room/detailForm 성공 후 서버가 redirect
    $('#btnTestGoDetail').on('click', function () {
        location.href = "room-detail.html";
    });

    // 주문방 참여화면으로 이동
    $('#btnTestGoJoin').on('click', function () {
        location.href = "room-join.html";
    });
	
	// 주문방 참여화면으로 이동
    $('#btnTestGoCfinish').on('click', function () {
        location.href = "room-create-finish.html";
    });
	
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

});
