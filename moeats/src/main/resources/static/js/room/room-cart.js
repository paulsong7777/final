(function () {
    const root = document.getElementById('roomCartRoot');
    const menuList = document.getElementById('roomCartMenuList');
    const searchInput = document.getElementById('roomCartSearchInput');
    const emptyState = document.getElementById('roomCartSearchEmpty');
    const confirmBtn = document.getElementById('roomCartConfirmBtn');
    const bottomCount = document.getElementById('roomCartBottomCount');
    const bottomAmount = document.getElementById('roomCartBottomAmount');

    if (!root || !menuList) return;

    const roomCode = root.dataset.roomCode || '';
	const nextStep = new URLSearchParams(window.location.search).get('next') || '';
    const cards = Array.from(menuList.querySelectorAll('.rc-card'));
    const stateMap = new Map();

    function normalize(value) {
        return String(value ?? '').trim().toLowerCase();
    }

    function formatMoney(value) {
        return Number(value || 0).toLocaleString('ko-KR') + '원';
    }

	function warn(message) {
	    if (!message) return;

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
            section.className = 'rc-section';

            const head = document.createElement('div');
            head.className = 'rc-section__head';

            const title = document.createElement('h2');
            title.className = 'rc-section__title';
            title.textContent = categoryName;

            const count = document.createElement('span');
            count.className = 'rc-section__count';
            count.textContent = `${groupCards.length}개 메뉴`;

            const grid = document.createElement('div');
            grid.className = 'rc-grid';

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

    function renderSearch() {
        const keyword = normalize(searchInput?.value);
        let visible = 0;

        cards.forEach((card) => {
            const shouldShow = matchesKeyword(card, keyword);
            card.classList.toggle('d-none', !shouldShow);
            if (shouldShow) {
                visible += 1;
            }
        });

        if (emptyState) {
            emptyState.classList.toggle('d-none', visible > 0);
        }

        buildSections();
    }

    function getCardState(card) {
        const menuIdx = Number(card.dataset.menuIdx || 0);
        return stateMap.get(menuIdx);
    }

    function syncCardView(card) {
        const state = getCardState(card);
        if (!state) return;

        const qtyValue = card.querySelector('.rcQtyValue');
        const addBtn = card.querySelector('.rcAddBtn');
        const minusBtn = card.querySelector('.rcQtyBtn[data-delta="-1"]');
        const plusBtn = card.querySelector('.rcQtyBtn[data-delta="1"]');

        if (qtyValue) {
            qtyValue.textContent = String(state.quantity);
        }

        if (minusBtn) {
            minusBtn.disabled = state.quantity <= 0 || state.pending;
        }

        if (plusBtn) {
            plusBtn.disabled = state.pending;
        }

        if (addBtn) {
            addBtn.disabled = state.pending;

            if (state.pending) {
                addBtn.textContent = '처리 중...';
            } else if (state.cartItemIdx && state.quantity === 0) {
                addBtn.textContent = '삭제';
            } else if (state.cartItemIdx) {
                addBtn.textContent = '변경 저장';
            } else {
                addBtn.textContent = '추가';
            }
        }
    }

    function syncBottomBar() {
        let totalQuantity = 0;
        let totalAmountValue = 0;

        stateMap.forEach((state) => {
            totalQuantity += state.quantity;
            totalAmountValue += state.quantity * state.menuPrice;
        });

        if (bottomCount) {
            bottomCount.textContent = `${totalQuantity}개 담김`;
        }

        if (bottomAmount) {
            bottomAmount.textContent = formatMoney(totalAmountValue);
        }

        if (confirmBtn) {
            confirmBtn.disabled = totalQuantity === 0;
        }
    }

    async function postForm(url, formData) {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error('request failed');
        }

        return response.json();
    }

    async function saveCard(card) {
        const state = getCardState(card);
        if (!state || !roomCode) return;

		if (!state.cartItemIdx && state.quantity <= 0) {
		    warn('수량을 먼저 선택해 주세요.');
		    return;
		}

        state.pending = true;
        syncCardView(card);

        const payload = new URLSearchParams();
        payload.set('menuIdx', String(state.menuIdx));
        payload.set('itemQuantity', String(state.quantity));
        payload.set('baseAmount', String(state.menuPrice * state.quantity));
        payload.set('optionExtraAmount', '0');
        payload.set('itemTotalAmount', String(state.menuPrice * state.quantity));

        try {
            let result;

            if (state.cartItemIdx && state.quantity === 0) {
                result = await postForm(
                    `/rooms/code/${encodeURIComponent(roomCode)}/cart/items/${state.cartItemIdx}/delete`,
                    new URLSearchParams()
                );

                if (!result.result) {
                    throw new Error('delete failed');
                }

                state.cartItemIdx = null;
            } else if (state.cartItemIdx) {
                result = await postForm(
                    `/rooms/code/${encodeURIComponent(roomCode)}/cart/items/${state.cartItemIdx}/edit`,
                    payload
                );

                if (!result.result) {
                    throw new Error('update failed');
                }
            } else {
                result = await postForm(
                    `/rooms/code/${encodeURIComponent(roomCode)}/cart`,
                    payload
                );

                if (!result.result) {
                    throw new Error('insert failed');
                }

                state.cartItemIdx = result.cartItemIdx || null;
            }
			} catch (error) {
			    warn('장바구니 반영 중 오류가 발생했습니다.');
			} finally {
            state.pending = false;
            syncCardView(card);
            syncBottomBar();
        }
    }

    function bindRoomCard(card) {
        const menuIdx = Number(card.dataset.menuIdx || 0);
        const menuPrice = Number(card.dataset.menuPrice || 0);
        const initialQuantity = Number(card.dataset.initialQuantity || 0);
        const initialCartItemIdx = card.dataset.cartItemIdx
                ? Number(card.dataset.cartItemIdx)
                : null;

        stateMap.set(menuIdx, {
            menuIdx,
            menuPrice,
            quantity: initialQuantity,
            cartItemIdx: initialCartItemIdx,
            pending: false
        });

        card.querySelectorAll('.rcQtyBtn').forEach((button) => {
            button.addEventListener('click', function () {
                const state = getCardState(card);
                if (!state || state.pending) return;

                const delta = Number(button.dataset.delta || 0);
                state.quantity = Math.max(0, state.quantity + delta);

                syncCardView(card);
                syncBottomBar();
            });
        });

        const addBtn = card.querySelector('.rcAddBtn');
        if (addBtn) {
            addBtn.addEventListener('click', function () {
                saveCard(card);
            });
        }

        syncCardView(card);
    }
	
	async function applyPendingSelection() {
	    const raw = sessionStorage.getItem('moPendingMenuSelection');
	    if (!raw) return;

	    let pending;
	    try {
	        pending = JSON.parse(raw);
	    } catch (e) {
	        sessionStorage.removeItem('moPendingMenuSelection');
	        return;
	    }

	    const currentStoreIdx = Number(document.querySelector('.rc-shell')?.dataset?.storeIdx || 0);
	    if (!pending || Number(pending.storeIdx) !== currentStoreIdx) {
	        return;
	    }

	    const targetCard = cards.find((card) => Number(card.dataset.menuIdx || 0) === Number(pending.menuIdx || 0));
	    if (!targetCard) {
	        sessionStorage.removeItem('moPendingMenuSelection');
	        return;
	    }

	    const state = getCardState(targetCard);
	    if (!state) return;

	    state.quantity = Math.max(1, Number(pending.quantity || 1));
	    syncCardView(targetCard);
	    syncBottomBar();

		await saveCard(targetCard);
		sessionStorage.removeItem('moPendingMenuSelection');

		if (nextStep === 'confirm' && roomCode) {
		    window.location.replace(`/rooms/code/${encodeURIComponent(roomCode)}/confirm`);
		}
	}

    if (searchInput) {
        searchInput.addEventListener('input', renderSearch);
    }

    if (confirmBtn) {
        confirmBtn.addEventListener('click', function () {
            if (confirmBtn.disabled || !roomCode) return;
            window.location.href = `/rooms/code/${encodeURIComponent(roomCode)}/confirm`;
        });
    }

	cards.forEach(bindRoomCard);
	syncBottomBar();
	renderSearch();
	applyPendingSelection();
})();