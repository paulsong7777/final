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
	
	function showCreateRoomError(message) {
	    if (!message) return;

	    if (typeof window.moOpenAlert === 'function') {
	        window.moOpenAlert({
	            eyebrow: '주문방',
	            title: '주문방을 열 수 없어요',
	            message: message,
	            confirmText: '확인'
	        });
	        return;
	    }

	    if (typeof window.moShowToast === 'function') {
	        window.moShowToast(message, 'warning');
	        return;
	    }

	    window.alert(message);
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

	if (createRoomBtn) {
	    createRoomBtn.addEventListener('click', () => {
	        const storeIdx = getStoreIdx();
	        const isAuthenticated = createRoomBtn.dataset.authenticated === 'true';

	        if (!isAuthenticated) {
	            if (typeof window.openMoLoginLayer === 'function') {
	                window.openMoLoginLayer();
	            } else {
	                window.location.href = '/login';
	            }
	            return;
	        }

			if (!storeIdx) {
			    showCreateRoomError('가게 정보를 다시 확인한 뒤 시도해 주세요.');
			    return;
			}

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