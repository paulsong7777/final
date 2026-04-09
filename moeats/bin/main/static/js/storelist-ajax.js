(function () {
    const storeListRoot = document.getElementById('storeCardList');
    const emptyState = document.getElementById('storeEmptyState');
    const visibleStoreCount = document.getElementById('visibleStoreCount');
    const activeCategoryLabel = document.getElementById('activeCategoryLabel');
    const keywordInput = document.getElementById('storeKeyword');
    const searchButton = document.getElementById('storeSearchBtn');
    const resetButton = document.getElementById('resetStoreFilterBtn');
    const categoryButtons = document.querySelectorAll('.js-category-filter');

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
        return store.storeDescription || store.description || '단체 주문에 적합한 대표 인기 메뉴 운영';
    }

    function resolveEtaText(store) {
        return store.etaText || '20~30분';
    }

    function resolveSupportText(store) {
        if (store.supportText) return store.supportText;

        const supports = [];
        if (store.supportsDelivery) supports.push('배달 가능');
        if (store.supportsOnsite) supports.push('현장 가능');

        return supports.length ? supports.join(' / ') : '지원 정보 확인';
    }

    function resolveStoreIdx(store) {
        return store.storeIdx || store.id || '';
    }

    function resolveStoreLink(store) {
        const storeIdx = resolveStoreIdx(store);
        return storeIdx ? `/stores/${encodeURIComponent(storeIdx)}` : '#';
    }

    function buildStoreCard(store) {
        const categoryCode = resolveCategoryCode(store);
        const categoryName = categoryLabel(categoryCode);
        const imageUrl = resolveImageUrl(store);
        const description = resolveDescription(store);
        const etaText = resolveEtaText(store);
        const supportText = resolveSupportText(store);
        const minimumOrderAmount = formatPrice(store.minimumOrderAmount);
        const storeLink = resolveStoreLink(store);

        return `
            <div class="col-12 col-md-6 col-xl-4 js-store-item" data-category="${escapeHtml(categoryCode)}">
                <article class="mo-store-card h-100">
                    <div class="mo-store-card__image-wrap">
                        <div class="mo-store-card__image" style="background-image:url('${escapeHtml(imageUrl)}');"></div>
                        <span class="mo-chip mo-chip--navy">${escapeHtml(categoryName)}</span>
                    </div>
                    <div class="mo-store-card__body d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-start gap-3">
                            <div>
                                <h3 class="mo-store-card__title js-store-title">${escapeHtml(store.storeName || '가게명')}</h3>
                                <p class="mo-store-card__desc js-store-desc">${escapeHtml(description)}</p>
                            </div>
                            <span class="mo-chip">${escapeHtml(etaText)}</span>
                        </div>
                        <div class="mo-store-card__meta-grid">
                            <div>
                                <span class="mo-store-card__meta-label">최소주문</span>
                                <strong>${escapeHtml(minimumOrderAmount)}</strong>
                            </div>
                            <div>
                                <span class="mo-store-card__meta-label">지원 정보</span>
                                <strong>${escapeHtml(supportText)}</strong>
                            </div>
                        </div>
                        <div class="mt-3 mb-3">
                            <div class="d-flex flex-wrap gap-2">
                                ${store.supportsDelivery ? '<span class="mo-chip">배달</span>' : ''}
                                ${store.supportsOnsite ? '<span class="mo-chip">현장</span>' : ''}
                                ${store.minimumOrderAmount !== null && store.minimumOrderAmount !== undefined ? '<span class="mo-chip">최소주문</span>' : ''}
                            </div>
                        </div>
                        <div class="mo-store-card__actions mt-auto">
                            <a href="${storeLink}" class="mo-btn mo-btn-outline flex-fill">상세 보기</a>
                            <a href="${storeLink}" class="mo-btn mo-btn-primary flex-fill">주문 시작</a>
                        </div>
                    </div>
                </article>
            </div>
        `;
    }

    function matchesCategory(store) {
        return activeCategory === 'ALL' || resolveCategoryCode(store) === activeCategory;
    }

    function matchesKeyword(store) {
        if (!currentKeyword) return true;

        const searchable = normalize([
            store.storeName,
            store.storeDescription,
            store.description,
            categoryLabel(resolveCategoryCode(store))
        ].join(' '));

        return searchable.includes(normalize(currentKeyword));
    }

    function getFilteredStores() {
        return allStores.filter((store) => matchesCategory(store) && matchesKeyword(store));
    }

    function updateSummary(count) {
        if (visibleStoreCount) visibleStoreCount.textContent = count;
        if (activeCategoryLabel) activeCategoryLabel.textContent = categoryLabel(activeCategory);
    }

    function renderStores() {
        const filteredStores = getFilteredStores();

        if (!filteredStores.length) {
            storeListRoot.innerHTML = '';
            storeListRoot.classList.add('d-none');
            emptyState.classList.remove('d-none');
            updateSummary(0);
            return;
        }

        storeListRoot.innerHTML = filteredStores.map(buildStoreCard).join('');
        storeListRoot.classList.remove('d-none');
        emptyState.classList.add('d-none');
        updateSummary(filteredStores.length);
    }

    async function fetchStores() {
        const response = await fetch('/ajax/stores', {
            method: 'GET',
            headers: { Accept: 'application/json' }
        });

        if (!response.ok) {
            throw new Error('가게 목록을 불러오지 못했습니다.');
        }

        return response.json();
    }

    async function fetchStoreThumbnails(storeIds) {
        if (!storeIds || !storeIds.length) {
            return [];
        }

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

    function resetFilters() {
        activeCategory = 'ALL';
        currentKeyword = '';

        if (keywordInput) {
            keywordInput.value = '';
        }

        categoryButtons.forEach((button) => {
            button.classList.toggle('is-active', normalizeCategoryCode(button.dataset.category) === 'ALL');
        });

        renderStores();
    }

    async function init() {
        try {
            const stores = await fetchStores();
            const storeArray = Array.isArray(stores) ? stores : [];

            const storeIds = storeArray
                .map((store) => resolveStoreIdx(store))
                .filter((storeIdx) => storeIdx !== null && storeIdx !== undefined && storeIdx !== '');

            try {
                const thumbnails = await fetchStoreThumbnails(storeIds);
                allStores = mergeStoreThumbnails(storeArray, thumbnails);
            } catch (thumbnailError) {
                console.error(thumbnailError);
                allStores = storeArray;
            }
        } catch (error) {
            console.error(error);
            allStores = [];
        }

        renderStores();
    }

    categoryButtons.forEach((button) => {
        button.addEventListener('click', function () {
            categoryButtons.forEach((btn) => btn.classList.remove('is-active'));
            this.classList.add('is-active');
            activeCategory = normalizeCategoryCode(this.dataset.category);
            renderStores();
        });
    });

    if (searchButton) {
        searchButton.addEventListener('click', function () {
            currentKeyword = keywordInput ? keywordInput.value.trim() : '';
            renderStores();
        });
    }

    if (keywordInput) {
        keywordInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                currentKeyword = this.value.trim();
                renderStores();
            }
        });

        keywordInput.addEventListener('input', function () {
            if (!this.value.trim()) {
                currentKeyword = '';
                renderStores();
            }
        });
    }

    if (resetButton) {
        resetButton.addEventListener('click', resetFilters);
    }

    init();
})();