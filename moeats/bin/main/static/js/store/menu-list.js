(function () {
    // 1. 주요 요소(DOM) 선점
    const menuList = document.getElementById('menuList');
    const searchInput = document.getElementById('menuSearchInput');
    const emptyState = document.getElementById('menuSearchEmpty');
    const createRoomBtn = document.getElementById('fe02CreateRoomBtn');
    const cartBtn = document.getElementById('fe02CartBtn');
    const fabPriceDisplay = document.getElementById('fabTotalPrice');

    // [상태 관리] 장바구니 총액
    let totalBasketAmount = 0;

    if (!menuList) return;

    // 2. 실시간 금액 업데이트 함수 (전역 등록)
    window.updateFabText = function(amount) {
        totalBasketAmount += amount;
        
        // 표시할 타겟 설정 (스팬이 없으면 버튼 자체에 표시)
        const target = fabPriceDisplay || createRoomBtn;
        if (!target) return;

        if (totalBasketAmount > 0) {
            target.innerText = totalBasketAmount.toLocaleString() + "원 담겼어요!";
        } else {
            target.innerText = "주문하기";
        }
    };

    // 3. 가게 정보 추출 로직
    function getStoreIdx() {
        const fromButton = createRoomBtn?.dataset.storeIdx;
        if (fromButton && fromButton !== '0') return fromButton;

        const fromRoot = document.querySelector('[data-store-idx]')?.dataset.storeIdx;
        if (fromRoot && fromRoot !== '0') return fromRoot;

        const match = window.location.pathname.match(/\/stores\/(\d+)\/menu/);
        return match ? match[1] : '';
    }

    // 4. 검색 필터링 로직
    const cards = Array.from(menuList.querySelectorAll('.fe02-card'));

    function render() {
        const keyword = searchInput?.value.trim().toLowerCase() || '';
        let visibleCount = 0;

        cards.forEach((card) => {
            // HTML 데이터 속성(data-menu-name 등) 기반 검색
            const name = (card.dataset.menuName || '').toLowerCase();
            const desc = (card.dataset.menuDesc || '').toLowerCase();
            const shouldShow = name.includes(keyword) || desc.includes(keyword);

            card.classList.toggle('d-none', !shouldShow);
            if (shouldShow) visibleCount++;
        });

        if (emptyState) {
            emptyState.classList.toggle('d-none', visibleCount > 0);
        }
    }

    // 5. 이벤트 리스너 (중복 제거 및 통합)

    // 검색 입력 시
    if (searchInput) {
        searchInput.addEventListener('input', render);
    }

    // [주문하기 / 방 생성] 버튼 클릭 시
    if (createRoomBtn) {
        createRoomBtn.addEventListener('click', () => {
            const storeIdx = getStoreIdx();
            if (!storeIdx) {
                window.alert('가게 정보가 없습니다.');
                return;
            }

            // A. 모달/시트 오픈 함수가 정의되어 있다면 실행 (현재 페이지 유지)
            if (typeof window.openCreateRoomSheet === 'function') {
                window.openCreateRoomSheet({
                    storeIdx: Number(storeIdx),
                    storeName: createRoomBtn.dataset.storeName || '',
                    minimumOrderAmount: createRoomBtn.dataset.minimumOrderAmount || ''
                });
            } 
            // B. 함수가 없다면 페이지 이동 (Fallback)
            else {
                window.location.href = `/rooms/new?storeIdx=${encodeURIComponent(storeIdx)}`;
            }
        });
    }

    // [장바구니 / 참여중인 방] 버튼 클릭 시
    if (cartBtn) {
        cartBtn.addEventListener('click', () => {
            const shell = document.querySelector('.fe02-shell');
            const roomCode = shell?.getAttribute('data-room-code') || 
                             document.querySelector('[data-room-code]')?.dataset.roomCode;

            if (roomCode) {
                window.location.href = `/rooms/${roomCode}`;
            } else {
                window.alert('참여 중인 방 정보를 찾을 수 없습니다.');
            }
        });
    }

    // 초기 실행
    render();
})();