(function () {
    const menuList = document.getElementById('menuList');
    const searchInput = document.getElementById('menuSearchInput');
    const emptyState = document.getElementById('menuSearchEmpty');
    const createRoomBtn = document.getElementById('fe02CreateRoomBtn');

    if (!menuList) return;

    const cards = Array.from(menuList.querySelectorAll('.fe02-card'));

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
	    const category = normalize(card.dataset.categoryName);

	    return name.includes(keyword) || desc.includes(keyword) || category.includes(keyword);
	}

	function buildSections() {
	    const visibleCards = cards.filter((card) => !card.classList.contains('d-none'));
	    menuList.innerHTML = '';

	    if (!visibleCards.length) {
	        return;
	    }

	    const groups = new Map();

	    visibleCards.forEach((card) => {
	        const categoryName = card.dataset.categoryName || '기타 메뉴';
	        if (!groups.has(categoryName)) {
	            groups.set(categoryName, []);
	        }
	        groups.get(categoryName).push(card);
	    });

	    groups.forEach((groupCards, categoryName) => {
	        const section = document.createElement('section');
	        section.className = 'fe02-section';

	        const head = document.createElement('div');
	        head.className = 'fe02-section__head';

	        const title = document.createElement('h2');
	        title.className = 'fe02-section__title';
	        title.textContent = categoryName;

	        const count = document.createElement('span');
	        count.className = 'fe02-section__count';
	        count.textContent = `${groupCards.length}개 메뉴`;

	        const grid = document.createElement('div');
	        grid.className = 'fe02-section__grid';

	        head.appendChild(title);
	        head.appendChild(count);
	        section.appendChild(head);
	        section.appendChild(grid);

	        groupCards.forEach((card) => {
	            grid.appendChild(card);
	        });

	        menuList.appendChild(section);
	    });
	}	
	
	function render() {
	    const keyword = normalize(searchInput?.value);
	    let visible = 0;

	    cards.forEach((card) => {
	        const shouldShow = matchesKeyword(card, keyword);
	        card.classList.toggle('d-none', !shouldShow);
	        if (shouldShow) visible += 1;
	    });

	    if (emptyState) {
	        emptyState.classList.toggle('d-none', visible > 0);
	    }

	    buildSections();
	}

    if (searchInput) {
        searchInput.addEventListener('input', render);
    }

	// menu-list.js 내부의 createRoomBtn 이벤트 리스너 수정
	if (createRoomBtn) {
	    createRoomBtn.addEventListener('click', () => {
	        const storeIdx = getStoreIdx();
	        const isAuthenticated = createRoomBtn.dataset.authenticated === 'true';

	        if (!isAuthenticated) {
	            // 로그인 유도 로직 (기존 유지)
	            if (typeof window.openMoLoginLayer === 'function') {
	                window.openMoLoginLayer();
	            } else {
	                window.location.href = '/login';
	            }
	            return;
	        }

	        // [추가] 배송지 등록 여부 체크 (header.html의 배송지 리스트 활용)
	        // 전역 함수나 변수가 없다면 UI 요소 존재 여부로 판단 가능
	        const addressItems = document.querySelectorAll('#moAddressModal .mo-address-item');
	        if (addressItems.length === 0) {
	            // 배송지가 없는 경우 공통 컨펌 모달 띄우기
	            const confirmModalEl = document.getElementById('moConfirmModal');
	            if (confirmModalEl) {
	                confirmModalEl.querySelector('[data-mo-confirm-title]').textContent = '배송지를 먼저 등록해주세요 🛵';
	                confirmModalEl.querySelector('[data-mo-confirm-message]').innerHTML = 
	                    '주문방을 만들기 위해 배송지 정보가 필요합니다.<br>주소 등록 페이지로 이동할까요?';
	                
	                const acceptBtn = confirmModalEl.querySelector('[data-mo-confirm-accept]');
	                acceptBtn.textContent = '주소 등록하기';
	                acceptBtn.onclick = function() {
	                    window.location.href = '/members/me/addresses/new';
	                };

	                const confirmModal = new bootstrap.Modal(confirmModalEl);
	                confirmModal.show();
	                return; // 프로세스 중단
	            }
	        }

	        // 기존 주문방 생성 시트 호출 로직 (기존 유지)
	        if (typeof window.openCreateRoomSheet === 'function') {
	            window.openCreateRoomSheet({
	                storeIdx: Number(storeIdx),
	                storeName: createRoomBtn?.dataset.storeName || '',
	                minimumOrderAmount: createRoomBtn?.dataset.minimumOrderAmount || ''
	            });
	            return;
	        }
	        window.location.href = `/rooms/new?storeIdx=${encodeURIComponent(storeIdx)}`;
	    });
	}

    render();
})();