$(document).ready(function () {
    // 1. 주요 요소 캐싱
    const $menuList = $('#menuList');
    const $searchInput = $('#menuSearchInput');
    const $emptyState = $('#menuSearchEmpty');
    const $visibleCount = $('#visibleMenuCount');
    const $filterButtons = $('#menuStatusFilter .fe02-filter__btn');
	const basePath = window.location.pathname.replace(/\/cart\/?$/, ''); // ex) /rooms/code/ABCD
	const csrfToken = $("meta[name='_csrf']").attr("content");
    
    
    if ($menuList.length === 0) return;

    const $cards = $menuList.find('.fe02-card');
    let activeFilter = 'ALL';

    // 2. 검색 및 필터링 기능
    function render() {
        const keyword = $searchInput.val().trim().toLowerCase();
        let visible = 0;

        $cards.each(function () {
            const $card = $(this);
            const name = ($card.data('menu-name') || '').toString().toLowerCase();
            const desc = ($card.data('menu-desc') || '').toString().toLowerCase();
            const status = $card.data('menu-status') || '';

            const matchesKeyword = !keyword || name.includes(keyword) || desc.includes(keyword);
            const matchesStatus = activeFilter === 'ALL' || status === activeFilter;

            const shouldShow = matchesKeyword && matchesStatus;
            $card.toggleClass('d-none', !shouldShow);
            
            if (shouldShow) visible++;
        });

        if ($visibleCount.length) $visibleCount.text(visible);
        if ($emptyState.length) $emptyState.toggleClass('d-none', visible > 0);
    }

    // 필터 버튼 클릭
    $filterButtons.on('click', function () {
        $filterButtons.removeClass('is-active');
        $(this).addClass('is-active');
        activeFilter = $(this).data('filter') || 'ALL';
        render();
    });

    // 검색창 입력
    $searchInput.on('input', render);

    // 3. 수량 조절 및 장바구니 로직 (이벤트 위임 방식 사용)
    
    // 수량 증가 (+) 및 감소 (-)
    $(document).on('click', '.btn-qty-plus', function() {
        const input = $(this).siblings('.input-qty');
        const currentVal = parseInt(input.val());
        if(currentVal < 99) input.val(currentVal + 1);
    });

    $(document).on('click', '.btn-qty-minus', function() {
        const input = $(this).siblings('.input-qty');
        const currentVal = parseInt(input.val());
        if(currentVal > 1) input.val(currentVal - 1);
    });

    // 장바구니 담기 버튼
	$(document).on('click', '.btn-add-cart', function() {
        const $btn = $(this);
        const menuIdx = $btn.data('menu-idx');
        const menuName = $btn.data('menu-name');
        const menuPrice = parseInt($btn.data('menu-price'));
        const quantity = parseInt($(`#qty-${menuIdx}`).val());

        const cartData = {
            menuIdx: menuIdx,
            itemQuantity: quantity,
            baseAmount: menuPrice,
            itemTotalAmount: menuPrice * quantity
        };

        $.ajax({
            url: basePath + '/cart', 
            method: 'POST',
            data: cartData,
            beforeSend: function(xhr) {
                if (csrfToken) xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken);
            },
            success: function(response) {
                if(response.result) {
                    alert(`${menuName} ${quantity}개를 장바구니에 담았습니다.`);
                    $(`#qty-${menuIdx}`).val(1); // 수량 초기화
                    
                    // 💡 핵심: localCart에 더하는 대신, 서버에서 최신 상태를 다시 불러옵니다!
                    fetchServerCartSummary(); 
                } else {
                    alert("장바구니 담기에 실패했습니다.");
                }
            },
            error: function() {
                alert("서버 통신 중 오류가 발생했습니다.");
            }
        });
    });

	// room-menuselect.js 내에서 주소 생성
	const getCartUrl = basePath + '/cart/get';
	
    // 4. 서버에서 장바구니 데이터를 가져와서 요약 바를 그리는 함수
    function fetchServerCartSummary() {
		
        $.ajax({
            url: getCartUrl, // 장바구니 조회를 담당하는 GET API 경로
            method: 'GET',
            success: function(response) {
				// 만약 받은 데이터가 문자열이고 <!DOCTYPE으로 시작한다면 에러 처리
			    if (typeof response === 'string' && response.trim().startsWith('<!DOCTYPE')) {
			        console.error("서버에서 JSON 대신 HTML을 보냈습니다. 로그인 세션이나 URL을 확인하세요.");
			        return;
			    }
				
				console.log("서버 응답 데이터:", response);
				
                // response가 장바구니 리스트(배열)라고 가정
                let totalQty = 0;
                let totalPrice = 0;

                // 서버에서 가져온 리스트를 순회하며 합계 계산
                if(response && response.length > 0) {
                    response.forEach(item => {
                        totalQty += item.itemQuantity;
                        totalPrice += item.itemTotalAmount;
                    });
                }

                // 화면 업데이트
                $('#totalQtyText').text(totalQty);
                $('#totalPriceText').text(totalPrice.toLocaleString());
                $('#selectionSummaryBar').toggleClass('d-none', totalQty === 0);
            },
            error: function() {
                console.error("장바구니 정보를 불러오지 못했습니다.");
            }
        });
    }

    // 5. [수정] 주문 확인하러 가기 (POST 전송)
	$(document).on('click', '#btnGoConfirm', function() {
	    const path = window.location.pathname; // /rooms/code/{code}/menu
	    const confirmPath = path.replace('/cart', '/confirm'); // /rooms/code/{code}/confirm
	    
	    // 이미 DB에 데이터가 있으므로 GET 방식으로 이동해도 되고, 
	    // 현재 컨트롤러가 @PostMapping 이라면 빈 폼을 제출하면 됩니다.
	    const $form = $('<form></form>', {
	        method: 'POST',
	        action: confirmPath
	    });
	    
	    // CSRF 토큰 추가
	    const csrfToken = $("meta[name='_csrf']").attr("content");
	    if (csrfToken) {
	        $form.append($('<input type="hidden" name="_csrf" value="' + csrfToken + '">'));
	    }
	    
	    $('body').append($form);
	    $form.submit();
	});
	
    // 렌더링 초기 실행
    render();
	
	// 페이지가 처음 로드될 때도 기존에 담아둔 데이터가 있는지 서버에 확인
	fetchServerCartSummary();
});