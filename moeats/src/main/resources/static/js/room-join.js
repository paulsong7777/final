// room-join.js
// 주문방 참여 화면 전용 JS

document.addEventListener('DOMContentLoaded', function () {

    const form = document.getElementById('roomJoinForm');
    const roomCodeInput = document.getElementById('roomCodeInput');

    // room_code 입력 시 대문자 자동 변환
    if (roomCodeInput) {
        roomCodeInput.addEventListener('input', function () {
            this.value = this.value.toUpperCase();
        });
    }

    // 폼 제출 전 유효성 검사
    if (form) {
        form.addEventListener('submit', function (e) {
            const code = roomCodeInput ? roomCodeInput.value.trim() : '';
            if (!code) {
                e.preventDefault();
                alert('주문방 코드를 입력해 주세요.');
                return;
            }
            // form action의 {code} 부분을 동적으로 교체
            const action = form.getAttribute('action');
            const updatedAction = action.replace(/\/rooms\/[^/]*\/join/, '/rooms/' + code + '/join');
            form.setAttribute('action', updatedAction);
        });
    }
});
