(function () {
    const menuList = document.getElementById('menuList');
    const searchInput = document.getElementById('menuSearchInput');
    const emptyState = document.getElementById('menuSearchEmpty');
    const visibleCount = document.getElementById('visibleMenuCount');
    const filterButtons = document.querySelectorAll('#menuStatusFilter .fe02-filter__btn');
    const createRoomBtn = document.getElementById('fe02CreateRoomBtn');

    if (!menuList) return;

    const cards = Array.from(menuList.querySelectorAll('.fe02-card'));
    let activeFilter = 'ALL';

    function normalize(value) {
        return String(value ?? '').trim().toLowerCase();
    }

    function getStoreIdx() {
        const fromButton = createRoomBtn?.dataset.storeIdx;
        if (fromButton && fromButton !== '0') return fromButton;

        const fromRoot = document.querySelector('[data-store-idx]')?.dataset.storeIdx;
        if (fromRoot && fromRoot !== '0') return fromRoot;

        const match = window.location.pathname.match(/\/stores\/(\d+)\/menu/);
        return match ? match[1] : '';
    }

    function matchesKeyword(card, keyword) {
        if (!keyword) return true;

        const name = normalize(card.dataset.menuName);
        const desc = normalize(card.dataset.menuDesc);
        return name.includes(keyword) || desc.includes(keyword);
    }

    function matchesStatus(card) {
        if (activeFilter === 'ALL') return true;
        return (card.dataset.menuStatus || '') === activeFilter;
    }

    function render() {
        const keyword = normalize(searchInput?.value);
        let visible = 0;

        cards.forEach((card) => {
            const shouldShow = matchesKeyword(card, keyword) && matchesStatus(card);
            card.classList.toggle('d-none', !shouldShow);
            if (shouldShow) visible += 1;
        });

        if (visibleCount) {
            visibleCount.textContent = String(visible);
        }

        if (emptyState) {
            emptyState.classList.toggle('d-none', visible > 0);
        }
    }

    filterButtons.forEach((button) => {
        button.addEventListener('click', () => {
            filterButtons.forEach((btn) => btn.classList.remove('is-active'));
            button.classList.add('is-active');
            activeFilter = button.dataset.filter || 'ALL';
            render();
        });
    });

    if (searchInput) {
        searchInput.addEventListener('input', render);
    }

    if (createRoomBtn) {
        createRoomBtn.addEventListener('click', () => {
            const storeIdx = getStoreIdx();

            if (!storeIdx) {
                window.alert('가게 정보가 없어 주문방 생성으로 이동할 수 없습니다.');
                return;
            }

            // 추후 전역 방 생성 모달 엔진이 붙으면 그걸 우선 호출
			if (typeof window.openCreateRoomSheet === 'function') {
			    window.openCreateRoomSheet({
			        storeIdx: Number(storeIdx),
			        storeName: createRoomBtn?.dataset.storeName || '',
			        minimumOrderAmount: createRoomBtn?.dataset.minimumOrderAmount || ''
			    });
			    return;
			}

            // 현재 브랜치 기준 fallback
            window.location.href = `/rooms/new?storeIdx=${encodeURIComponent(storeIdx)}`;
        });
    }

    render();
})();