// room-join.test.js
// 목적: 백엔드 없이 room-join.html 화면 이동 테스트
// 실전 전환 시 이 파일 대신 room-join.js를 로드

$(function () {

    // 참여하기 버튼 (더미): 코드 입력값 확인 후 협업화면으로 이동
    // 실전: POST /rooms/{code}/join 성공 후 서버가 redirect
    $('#btnTestJoin').on('click', function () {
        var code = $('#roomCodeInput_dummy').val().trim().toUpperCase();
        if (!code) {
            alert('주문방 코드를 입력해 주세요.');
            return;
        }
        // 더미: 코드 검증 없이 바로 이동
        location.href = "room-detail.html";
    });

    // 주문방 협업화면으로 직접 이동
    $('#btnTestGoDetail').on('click', function () {
        location.href = "room-detail.html";
    });

    // 주문방 생성화면으로 이동
    $('#btnTestGoCreate').on('click', function () {
        location.href = "room-create.html";
    });

});
