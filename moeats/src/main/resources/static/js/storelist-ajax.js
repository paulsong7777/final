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
    let requestSeq = 0;

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

    function buildStoreCard(store) {
        const categoryCode = resolveCategoryCode(store);
        const imageUrl = resolveImageUrl(store);
        const description = resolveDescription(store);
        const etaText = resolveEtaText(store);
        const minimumOrderAmount = formatPrice(store.minimumOrderAmount);
        const storeLink = resolveStoreLink(store);
        const storeName = store.storeName || '가게명';

        const statusPriority = getStatusPriority(store);
        const statusText = getStatusText(store);
        const isAvailable = statusPriority === 1;

        let cardStyle = '';
        let imageStyle = '';
        let textStyle = '';
        let imageOverlay = '';
        let actionButtons = '';
        let imageLinkStart = '';
        let imageLinkEnd = '';

        if (isAvailable) {
            actionButtons = `
                <a href="${storeLink}" class="mo-btn mo-btn-outline flex-fill">상세 보기</a>
                <a href="${storeLink}" class="mo-btn mo-btn-primary flex-fill">주문 시작</a>
            `;
        } else if (statusPriority === 2) {
            cardStyle = 'opacity: 0.96;';
            imageStyle = 'filter: saturate(0.92) brightness(0.88);';
            textStyle = 'color: var(--mo-navy-strong);';

            imageOverlay = `
                <div class="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
                     style="background: linear-gradient(180deg, rgba(22, 34, 68, 0.04) 0%, rgba(22, 34, 68, 0.18) 100%); z-index: 2;">
                   <span class="fw-bold" style="color: #8a6700; background: rgba(255, 248, 214, 0.94); border: 1px solid rgba(255, 193, 7, 0.55); padding: 6px 14px; border-radius: 999px;">
                        ${statusText}
                   </span>
                </div>`;

            actionButtons = `
                <button type="button" class="mo-btn flex-fill" disabled aria-disabled="true" style="background: #fff8de; color: #8a6700; border: 1px solid rgba(255, 193, 7, 0.45); cursor: default;">
                    브레이크 타임 안내
                </button>
            `;
        } else {
            cardStyle = 'opacity: 0.92;';
            imageStyle = 'filter: grayscale(0.45) brightness(0.72);';
            textStyle = 'color: #6b7280 !important;';

            imageOverlay = `
                <div class="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
                     style="background: linear-gradient(180deg, rgba(18, 24, 38, 0.12) 0%, rgba(18, 24, 38, 0.34) 100%); z-index: 2;">
                   <span class="fw-bold" style="color: #ffffff; background: rgba(31, 41, 55, 0.78); padding: 6px 14px; border-radius: 999px;">${statusText}</span>
                </div>`;

            actionButtons = `
                <button type="button" class="mo-btn flex-fill" disabled aria-disabled="true" style="background: #f8fafc; color: #6b7280; border: 1px solid #d7dde5; cursor: default;">
                    영업 준비 중
                </button>
            `;
        }

        if (isAvailable) {
            imageLinkStart = `<a href="${storeLink}" class="mo-store-card--ajax__image-link" aria-label="${escapeHtml(storeName)} 상세 보기">`;
            imageLinkEnd = '</a>';
        } else {
            imageLinkStart = '<div class="mo-store-card--ajax__image-link" aria-disabled="true" style="cursor: default;">';
            imageLinkEnd = '</div>';
        }

        return `
            <div class="col-12 col-md-6 col-xl-3 js-store-item" data-category="${escapeHtml(categoryCode)}">
                <article class="mo-store-card mo-store-card--ajax h-100" style="${cardStyle}">
                    <div class="mo-store-card--ajax__image-wrap position-relative">
                        ${imageOverlay}
                        ${imageLinkStart}
                            <div class="mo-store-card--ajax__image" style="background-image:url('${escapeHtml(imageUrl)}'); ${imageStyle}"></div>
                        ${imageLinkEnd}
                    </div>

                    <div class="mo-store-card__body mo-store-card--ajax__body d-flex flex-column">
                        <div class="mo-store-card--ajax__top">
                            <h3 class="mo-store-card__title mo-store-card--ajax__title js-store-title" style="${textStyle}">${escapeHtml(storeName)}</h3>
                            ${statusPriority === 1 ? `<span class="mo-chip mo-store-card--ajax__eta">${escapeHtml(etaText)}</span>` : ''}
                        </div>

                        <div class="mo-store-card--ajax__middle">
                            <p class="mo-store-card__desc mo-store-card--ajax__desc js-store-desc" style="${textStyle}">${escapeHtml(description)}</p>

                            <div class="mo-store-card--ajax__minimum-inline">
                                <span class="mo-store-card--ajax__minimum-inline-label" style="${textStyle}">최소주문</span>
                                <strong class="mo-store-card--ajax__minimum-inline-value" style="${textStyle}">${escapeHtml(minimumOrderAmount)}</strong>
                            </div>
                        </div>

                        <div class="mo-store-card__actions mo-store-card--ajax__actions mt-auto d-flex gap-2">
                            ${actionButtons}
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

    loadStores();
})();
