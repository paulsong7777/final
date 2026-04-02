// room-join.js
// 주문방 참여 화면 전용 실전 JS
// Controller: GET /rooms/code/{room_code} → 자동 참가 처리 후 room-detail 반환
// POST /rooms/{code}/join 엔드포인트 없음 → GET redirect 방식으로 전환

document.addEventListener('DOMContentLoaded', function () {

    const roomCodeInput = document.getElementById('roomCodeInput');

    // room_code 입력 시 대문자 자동 변환
    if (roomCodeInput) {
        roomCodeInput.addEventListener('input', function () {
            this.value = this.value.toUpperCase();
        });
    }

    // 참여하기 버튼 클릭 → 유효성 확인 후 GET /rooms/code/{code} 로 이동
    // Controller의 GET /rooms/code/{room_code} 가 참가 처리 + room-detail 렌더링을 한 번에 수행
    const btnJoinRoom = document.getElementById('btnJoinRoom');
    if (btnJoinRoom) {
        btnJoinRoom.addEventListener('click', function () {
            const code = roomCodeInput ? roomCodeInput.value.trim() : '';
            if (!code) {
                alert('주문방 코드를 입력해 주세요.');
                roomCodeInput && roomCodeInput.focus();
                return;
            }
            // GET /rooms/code/{code} 로 이동
            // → Controller가 방 존재 확인, 자동 참가, room-detail 반환
            location.href = '/rooms/code/' + code;
        });
    }

    // 링크 공유로 직접 접근한 경우 roomCode input에 자동 채워진 값 감지
    // Thymeleaf가 th:value로 채워줬다면 input에 이미 값이 있음
    if (roomCodeInput && roomCodeInput.value.trim()) {
        console.log('링크 공유 접근 감지. 자동 입력된 코드:', roomCodeInput.value);
    }

});
