(function () {
    const storeListRoot = document.getElementById('storeCardList');
    const emptyState = document.getElementById('storeEmptyState');
    const visibleStoreCount = document.getElementById('visibleStoreCount');
    const activeCategoryLabel = document.getElementById('activeCategoryLabel');
    const keywordInput = document.getElementById('storeKeyword');
    const searchButton = document.getElementById('storeSearchBtn');
    const resetButton = document.getElementById('resetStoreFilterBtn');
    const categoryButtons = document.querySelectorAll('.js-category-filter');
	const DESKTOP_PLACEHOLDER = '함께 고르고, 쉽게 모이고, 한 번에 주문';
	const MOBILE_PLACEHOLDER = '함께 고르고, 한 번에 주문';

	function syncSearchPlaceholder() {
	    if (!keywordInput) return;

	    keywordInput.placeholder = window.matchMedia('(max-width: 767.98px)').matches
	        ? MOBILE_PLACEHOLDER
	        : DESKTOP_PLACEHOLDER;
	}
	
    if (!storeListRoot || !emptyState) return;

    const CATEGORY_LABELS = {
        ALL: '전체',
        CHICKEN: '치킨',
        PIZZA: '피자',
        CHINESE: '중식',
        KOREAN: '한식',
        CAFE: '카페'
    };

    let activeCategory = 'ALL';
    let currentKeyword = '';
    let allStores = [];
    let requestSeq = 0;

	function resolveMenuPreviewText(store) {
	    return String(store.menuPreviewText || '').trim();
	}

	function resolveMenuChips(store) {
	    const previewText = resolveMenuPreviewText(store);
	    if (!previewText) return [];

	    return previewText
	        .split('|||')
	        .map((item) => String(item || '').trim())
	        .filter(Boolean)
	        .slice(0, 3);
	}

	function categoryPlaceholderLabel(code) {
	    switch (code) {
	        case 'CHICKEN': return '치킨';
	        case 'PIZZA': return '피자';
	        case 'CHINESE': return '중식';
	        case 'KOREAN': return '한식';
	        case 'CAFE': return '카페';
	        default: return '가게';
	    }
	}

	function hasUsableImage(url) {
	    const value = String(url || '').trim();
	    return !!value && !value.includes('store-placeholder.png');
	}

	function buildPlaceholderMarkup(categoryCode) {
	    const label = categoryPlaceholderLabel(categoryCode);

	    return `
	        <div class="mo-store-card--ajax__placeholder mo-store-card--ajax__placeholder--${categoryCode.toLowerCase()}">
	            <div class="mo-store-card--ajax__placeholder-icon" aria-hidden="true">
	                ${buildCategoryIcon(categoryCode)}
	            </div>
	            <span class="mo-store-card--ajax__placeholder-label">${escapeHtml(label)}</span>
	        </div>
	    `;
	}

	function buildCategoryIcon(categoryCode) {
	    switch (categoryCode) {
	        case 'CHICKEN':
	            return `
	                <svg viewBox="0 0 24 24" fill="none">
	                    <path d="M8 15c0-2.8 2.2-5 5-5 2.2 0 4 1.8 4 4 0 3.3-2.7 6-6 6-1.7 0-3-1.3-3-3v-2Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
	                    <circle cx="18.5" cy="7.5" r="1.5" stroke="currentColor" stroke-width="1.8"/>
	                    <circle cx="6" cy="18" r="1.4" stroke="currentColor" stroke-width="1.8"/>
	                </svg>
	            `;
	        case 'PIZZA':
	            return `
	                <svg viewBox="0 0 24 24" fill="none">
	                    <path d="M4 7c5-2 11-2 16 0l-8 13L4 7Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
	                    <circle cx="11" cy="10.5" r="1.2" fill="currentColor"/>
	                    <circle cx="15" cy="11.5" r="1.2" fill="currentColor"/>
	                </svg>
	            `;
	        case 'CHINESE':
	            return `
	                <svg viewBox="0 0 24 24" fill="none">
	                    <path d="M5 14h14a4 4 0 0 1-4 4H9a4 4 0 0 1-4-4Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
	                    <path d="M8 10c1 0 1-2 2-2s1 2 2 2 1-2 2-2 1 2 2 2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
	                </svg>
	            `;
	        case 'KOREAN':
	            return `
	                <svg viewBox="0 0 24 24" fill="none">
	                    <path d="M4 13c0 4.4 3.6 8 8 8s8-3.6 8-8H4Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
	                    <path d="M7 10h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
	                    <path d="M9 7h6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
	                </svg>
	            `;
	        case 'CAFE':
	            return `
	                <svg viewBox="0 0 24 24" fill="none">
	                    <path d="M7 9h8a0 0 0 0 1 0 0v4a4 4 0 0 1-4 4h0a4 4 0 0 1-4-4V9a0 0 0 0 1 0 0Z" stroke="currentColor" stroke-width="1.8"/>
	                    <path d="M15 10h1.5A2.5 2.5 0 0 1 19 12.5v0A2.5 2.5 0 0 1 16.5 15H15" stroke="currentColor" stroke-width="1.8"/>
	                    <path d="M6 19h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
	                </svg>
	            `;
	        default:
	            return `
	                <svg viewBox="0 0 24 24" fill="none">
	                    <rect x="5" y="6" width="14" height="12" rx="3" stroke="currentColor" stroke-width="1.8"/>
	                </svg>
	            `;
	    }
	}

	function buildMenuChips(store) {
	    const chips = resolveMenuChips(store);
	    if (!chips.length) {
	        return `<div class="mo-store-card--ajax__chips">
	            <span class="mo-store-card--ajax__chip is-empty">대표 메뉴 준비 중</span>
	        </div>`;
	    }

	    return `<div class="mo-store-card--ajax__chips">
	        ${chips.map((chip) => `<span class="mo-store-card--ajax__chip">${escapeHtml(chip)}</span>`).join('')}
	    </div>`;
	}
	
    function escapeHtml(value) {
        return String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function normalize(text) {
        return String(text ?? '').trim().toLowerCase();
    }

    function formatPrice(value) {
        if (value === null || value === undefined || value === '') return '확인 필요';
        const num = Number(value);
        if (Number.isNaN(num)) return '확인 필요';
        return num.toLocaleString('ko-KR') + '원';
    }

    function normalizeCategoryCode(value) {
        const raw = String(value ?? '').trim().toUpperCase();

        if (raw === 'CHICKEN' || raw === '치킨') return 'CHICKEN';
        if (raw === 'PIZZA' || raw === '피자') return 'PIZZA';
        if (raw === 'CHINESE' || raw === '중식') return 'CHINESE';
        if (raw === 'KOREAN' || raw === '한식') return 'KOREAN';
        if (raw === 'CAFE' || raw === '카페') return 'CAFE';
        if (raw === 'ALL' || raw === '전체') return 'ALL';

        return 'ALL';
    }

    function categoryLabel(code) {
        return CATEGORY_LABELS[code] || '전체';
    }

    function resolveCategoryCode(store) {
        return normalizeCategoryCode(store.storeCategory || store.categoryCode || store.categoryName);
    }

    function resolveImageUrl(store) {
        return store.thumbnailUrl
            || store.imageUrl
            || store.storeImageUrl
            || '/images/store/store-placeholder.png';
    }

    function resolveDescription(store) {
        return store.storeDescription || store.description || '';
    }

    function resolveStoreIdx(store) {
        return store.storeIdx || store.id || '';
    }

    function resolveStoreLink(store) {
        const storeIdx = resolveStoreIdx(store);
        return storeIdx ? `/stores/${encodeURIComponent(storeIdx)}/menu` : '#';
    }

    function getStatusPriority(store) {
        const status = String(store.storeStatus || 'ACTIVE').toUpperCase();
        if (status === 'ACTIVE' || status === 'OPEN') return 1;
        if (status === 'PAUSED' || status === 'BREAK') return 2;
        return 3;
    }

    function getStatusText(store) {
        const status = String(store.storeStatus || 'ACTIVE').toUpperCase();
        if (status === 'PAUSED' || status === 'BREAK') return '브레이크 타임';
        if (status !== 'ACTIVE' && status !== 'OPEN') return '영업 종료';
        return '';
    }

    function buildStatusBadge(store) {
        const statusText = getStatusText(store);
        if (!statusText) return '';
        return `<span class="mo-store-card--ajax__status">${escapeHtml(statusText)}</span>`;
    }

    function buildCardAction(store, isAvailable) {
        const storeLink = resolveStoreLink(store);

        if (isAvailable) {
            return `<a href="${storeLink}" class="mo-btn mo-btn-primary w-100">주문 시작</a>`;
        }

        const statusText = getStatusText(store) || '현재 주문 불가';
        return `<button type="button" class="mo-btn w-100" disabled aria-disabled="true" style="background:#f8fafc; color:#6b7280; border:1px solid #d7dde5; cursor:default;">${escapeHtml(statusText)}</button>`;
    }

	function buildStoreCard(store) {
	    const categoryCode = resolveCategoryCode(store);
	    const imageUrl = resolveImageUrl(store);
	    const minimumOrderAmount = formatPrice(store.minimumOrderAmount);
	    const storeLink = resolveStoreLink(store);
	    const storeName = store.storeName || '가게명';
	    const categoryName = categoryLabel(categoryCode);

	    const statusPriority = getStatusPriority(store);
	    const isAvailable = statusPriority === 1;
	    const statusBadge = !isAvailable ? buildStatusBadge(store) : '';

	    const imageMarkup = hasUsableImage(imageUrl)
	        ? `<div class="mo-store-card--ajax__image" style="background-image:url('${escapeHtml(imageUrl)}');"></div>`
	        : buildPlaceholderMarkup(categoryCode);

	    return `
	        <div class="col-12 col-md-6 col-xl-3 js-store-item" data-category="${escapeHtml(categoryCode)}">
	            <article class="mo-store-card mo-store-card--ajax h-100 ${!isAvailable ? 'is-unavailable' : ''}">
	                <div class="mo-store-card--ajax__image-wrap position-relative">
	                    <a href="${storeLink}" class="mo-store-card--ajax__image-link" aria-label="${escapeHtml(storeName)} 상세 보기">
	                        ${imageMarkup}
	                    </a>
	                    <div class="mo-store-card--ajax__overlay-top">
	                        <span class="mo-store-card--ajax__category">${escapeHtml(categoryName)}</span>
	                        ${statusBadge}
	                    </div>
	                </div>

	                <div class="mo-store-card__body mo-store-card--ajax__body d-flex flex-column">
	                    <a href="${storeLink}" class="mo-store-card--ajax__title-link">
	                        <h3 class="mo-store-card__title mo-store-card--ajax__title">${escapeHtml(storeName)}</h3>
	                    </a>

	                    ${buildMenuChips(store)}

	                    <div class="mo-store-card--ajax__meta-row">
	                        <div class="mo-store-card--ajax__meta-item">
	                            <span class="mo-store-card--ajax__meta-label">최소주문</span>
	                            <strong class="mo-store-card--ajax__meta-value">${escapeHtml(minimumOrderAmount)}</strong>
	                        </div>
	                    </div>

	                    <div class="mo-store-card__actions mo-store-card--ajax__actions mt-auto">
	                        ${buildCardAction(store, isAvailable)}
	                    </div>
	                </div>
	            </article>
	        </div>
	    `;
	}

    function sortStores(stores) {
        return [...stores].sort((a, b) => getStatusPriority(a) - getStatusPriority(b));
    }

    function updateSummary(count) {
        if (visibleStoreCount) visibleStoreCount.textContent = count;
        if (activeCategoryLabel) activeCategoryLabel.textContent = categoryLabel(activeCategory);
    }

    function renderStores() {
        const sortedStores = sortStores(allStores);

        if (!sortedStores.length) {
            storeListRoot.innerHTML = '';
            storeListRoot.classList.add('d-none');
            emptyState.classList.remove('d-none');
            updateSummary(0);
            return;
        }

        storeListRoot.innerHTML = sortedStores.map(buildStoreCard).join('');
        storeListRoot.classList.remove('d-none');
        emptyState.classList.add('d-none');
        updateSummary(sortedStores.length);
    }

    function buildStoreQueryString() {
        const params = new URLSearchParams();

        if (activeCategory && activeCategory !== 'ALL') {
            params.set('category', activeCategory);
        }

        const keyword = String(currentKeyword ?? '').trim();
        if (keyword) {
            params.set('keyword', keyword);
        }

        return params.toString();
    }

    async function fetchStores() {
        const queryString = buildStoreQueryString();
        const url = queryString ? `/ajax/stores?${queryString}` : '/ajax/stores';

        const response = await fetch(url, {
            method: 'GET',
            headers: { Accept: 'application/json' }
        });

        if (!response.ok) {
            throw new Error('가게 목록을 불러오지 못했습니다.');
        }

        return response.json();
    }

    async function fetchStoreThumbnails(storeIds) {
        if (!storeIds || !storeIds.length) return [];

        const params = new URLSearchParams();
        storeIds.forEach((storeId) => {
            if (storeId !== null && storeId !== undefined && storeId !== '') {
                params.append('storeIds', storeId);
            }
        });

        const response = await fetch(`/api/store-thumbnails?${params.toString()}`, {
            method: 'GET',
            headers: { Accept: 'application/json' }
        });

        if (!response.ok) {
            throw new Error('가게 썸네일을 불러오지 못했습니다.');
        }

        return response.json();
    }

    function mergeStoreThumbnails(stores, thumbnails) {
        const thumbnailMap = new Map();

        (thumbnails || []).forEach((item) => {
            if (!item) return;
            thumbnailMap.set(String(item.storeIdx), item.storeThumbnailUrl || null);
        });

        return (stores || []).map((store) => {
            const storeIdx = String(resolveStoreIdx(store));
            return {
                ...store,
                thumbnailUrl: thumbnailMap.get(storeIdx) || null
            };
        });
    }

    async function loadStores() {
        const currentSeq = ++requestSeq;

        try {
            const stores = await fetchStores();
            const storeArray = Array.isArray(stores) ? stores : [];

            const storeIds = storeArray
                .map((store) => resolveStoreIdx(store))
                .filter((storeIdx) => storeIdx !== null && storeIdx !== undefined && storeIdx !== '');

            let mergedStores = storeArray;

            try {
                const thumbnails = await fetchStoreThumbnails(storeIds);
                mergedStores = mergeStoreThumbnails(storeArray, thumbnails);
            } catch (thumbnailError) {
                console.error(thumbnailError);
            }

            if (currentSeq !== requestSeq) return;

            allStores = mergedStores;
        } catch (error) {
            console.error(error);

            if (currentSeq !== requestSeq) return;

            allStores = [];
        }

        renderStores();
    }

    function resetFilters() {
        activeCategory = 'ALL';
        currentKeyword = '';

        if (keywordInput) {
            keywordInput.value = '';
        }

        categoryButtons.forEach((button) => {
            button.classList.toggle('is-active', normalizeCategoryCode(button.dataset.category) === 'ALL');
        });

        loadStores();
    }

    categoryButtons.forEach((button) => {
        button.addEventListener('click', function () {
            categoryButtons.forEach((btn) => btn.classList.remove('is-active'));
            this.classList.add('is-active');

            activeCategory = normalizeCategoryCode(this.dataset.category);
            loadStores();
        });
    });

    if (searchButton) {
        searchButton.addEventListener('click', function () {
            currentKeyword = keywordInput ? keywordInput.value.trim() : '';
            loadStores();
        });
    }

    if (keywordInput) {
        keywordInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                currentKeyword = this.value.trim();
                loadStores();
            }
        });

        keywordInput.addEventListener('input', function () {
            if (!this.value.trim()) {
                currentKeyword = '';
                loadStores();
            }
        });
    }

    if (resetButton) {
        resetButton.addEventListener('click', resetFilters);
    }

	window.addEventListener('resize', syncSearchPlaceholder);
	syncSearchPlaceholder();
    loadStores();
})();