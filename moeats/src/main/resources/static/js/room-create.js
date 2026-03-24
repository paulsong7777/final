// room-create.js
// 주문방 생성 화면 전용 JS

$(function () {

    const $form = $('#roomCreateForm');
    const $storeIdxInput = $('#storeIdxInput');
    const $storeIdxHidden = $('#storeIdx');

    // 테스트용: 직접 입력한 storeIdx를 hidden input에 반영
    $storeIdxInput.on('input', function(){	
		$storeIdxHidden.val($(this).val());	
	});

    // 폼 제출 전 유효성 검사. 공통으로 변수 잇고 값 없으면 alert문 띄우고 입력창으로 커서 옮기고 return(작업중단) 
    $form.on('submit', function(e){
		// 1. 배송지선택여부확인(id선택자 및 값 체크)
		const deliveryAddressIdx = $('#deliveryAddressIdx').val();
		if(!deliveryAddressIdx){
			alert('배송지를 선택해 주세요');	
			$('#deliveryAddressIdx').focus(); // 입력창으로커서이동
			e.preventDefault();
			return;
		}			//1배송지선택여부확인
		
		// 2. 결제 방식 확인(체크된 라디오 버튼 찾기)
		const $paymentMode = $('input[name="paymentMode"]:checked');
		if($paymentMode.length == 0){
			alert('결제방식을 선택해 주세요');
			$('#payRepresentative').focus(); // 선택지창으로커서이동
			e.preventDefault();
			return;
		}			//2결제방식확인
		
		// 3. 가게 정보 확인(히든 필드 값 체크) *hidden값이므로 커서이동이 필요하지 않다. 정상플로우 상으로 가게정보창에서 넘어오는 화면이므로 storeIdx가 빌 일이 없다
		const storeVal = $('#storeIdx').val();
		if(!storeVal){
			alert('가게 정보가 없습니다. 가게 상세 페이지에서 다시 시도해 주세요.');
			e.preventDefault();
			return;
		}			//3가게정보확인
		
		// 아래는 db(컨트롤러) 요청예정사항
		
	});
});
