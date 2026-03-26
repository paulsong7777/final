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

});
