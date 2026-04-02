// room-create.js
// 주문방 생성 화면 전용 실전 JS
// Controller: POST /rooms (@RequestBody OrderRoom) → redirect:/rooms/code/{roomCode}
// form submit 방식 불가 (@RequestBody는 JSON만 수신) → AJAX로 JSON 전송

console.log("room-create.js 파일이 성공적으로 로드");

$(function () {

    const $storeIdxHidden = $('#storeIdx');

    // [이벤트 위임 방식] storeIdxInput 입력 시 hidden 필드에 복사
    $(document).on('input', '#storeIdxInput', function () {
        const val = $(this).val();
        $storeIdxHidden.val(val);
        console.log("입력된 가게 번호: " + val);
        console.log("히든 필드(전송용) 업데이트: " + $storeIdxHidden.val());
    });

    // 생성하기 버튼 클릭 → 유효성 검사 후 AJAX POST
    $('#btnCreateRoom').on('click', function () {

        // 1. 배송지 선택 확인
        const deliveryAddressIdx = $('#deliveryAddressIdx').val();
        if (!deliveryAddressIdx) {
            alert('배송지를 선택해 주세요');
            $('#deliveryAddressIdx').focus();
            return;
        }

        // 2. 결제 방식 확인
        const $paymentMode = $('input[name="paymentMode"]:checked');
        if ($paymentMode.length === 0) {
            alert('결제방식을 선택해 주세요');
            $('#payRepresentative').focus();
            return;
        }

        // 3. 가게 정보 확인
        const storeVal = $storeIdxHidden.val();
        if (!storeVal) {
            alert('가게 정보가 없습니다. 가게 상세 페이지에서 다시 시도해 주세요.');
            return;
        }

        // 4. JSON 데이터 구성
        const requestData = {
            storeIdx:            parseInt(storeVal),
            deliveryAddressIdx:  parseInt(deliveryAddressIdx),
            paymentMode:         $paymentMode.val(),
            maxParticipants:     $('#maxParticipants').val() ? parseInt($('#maxParticipants').val()) : null
        };

        console.log("전송 데이터:", requestData);

        // 5. AJAX POST /rooms
        $.ajax({
            url:         '/rooms',
            method:      'POST',
            contentType: 'application/json',
            data:        JSON.stringify(requestData),
            success: function (response, status, xhr) {
                // Controller가 redirect 응답을 보내므로
                // AJAX에서 redirect 추적: responseURL 또는 Location 헤더 사용
                const redirectUrl = xhr.getResponseHeader('Location') || '/rooms';
                location.href = redirectUrl;
            },
            error: function (xhr) {
                const msg = xhr.responseJSON?.message || '주문방 생성 중 오류가 발생했습니다.';
                alert(msg);
                console.error('오류:', xhr);
            }
        });

    });

    // 취소 버튼
    $('#btnCancelCreate').on('click', function () {
        history.back();
    });

});
