(function () {
    const DESKTOP_BREAKPOINT = 992;

    const header = document.querySelector('.js-mo-header');
    const mobileMenuEl = document.getElementById('moMobileMenu');

    const loginModalEl = document.getElementById('moLoginModal');
    const joinModalEl = document.getElementById('moJoinModal');
    const addressModalEl = document.getElementById('moAddressModal');

    const mobileLoginSheetEl = document.getElementById('moMobileLoginSheet');
    const mobileJoinSheetEl = document.getElementById('moMobileJoinSheet');
    const mobileAddressSheetEl = document.getElementById('moMobileAddressSheet');

	const createRoomModalEl = document.getElementById('moCreateRoomModal');
	const mobileCreateRoomSheetEl = document.getElementById('moMobileCreateRoomSheet');
    const confirmModalEl = document.getElementById('moConfirmModal');
    const confirmEyebrowEl = document.querySelector('[data-mo-confirm-eyebrow]');
    const confirmTitleEl = document.querySelector('[data-mo-confirm-title]');
    const confirmMessageEl = document.querySelector('[data-mo-confirm-message]');
    const confirmAcceptButton = document.querySelector('[data-mo-confirm-accept]');
    const confirmCancelButton = document.querySelector('[data-mo-confirm-cancel]');
    const toastEl = document.getElementById('moGlobalToast');
    const toastMessageEl = document.querySelector('[data-mo-toast-message]');

    const isMobile = () => window.innerWidth < DESKTOP_BREAKPOINT;
    let pendingConfirmResolve = null;

    const getModalInstance = (el) => {
        if (!el || !window.bootstrap) return null;
        return window.bootstrap.Modal.getOrCreateInstance(el);
    };

    const getOffcanvasInstance = (el) => {
        if (!el || !window.bootstrap) return null;
        return window.bootstrap.Offcanvas.getOrCreateInstance(el);
    };

	const toastPalette = {
	    success: {
	        background: '#252D59',
	        color: '#ffffff'
	    },
	    warning: {
	        background: '#F85E28',
	        color: '#ffffff'
	    },
	    info: {
	        background: '#252D59',
	        color: '#ffffff'
	    }
	};

    const showToast = (message, variant = 'info') => {
        if (!toastEl || !toastMessageEl || !window.bootstrap || !message) return;

        const palette = toastPalette[variant] || toastPalette.info;
        toastMessageEl.textContent = message;
        toastEl.style.background = palette.background;
        toastEl.style.color = palette.color;

        window.bootstrap.Toast.getOrCreateInstance(toastEl, {
            delay: 2200
        }).show();
    };

    const resolvePendingConfirm = (value) => {
        if (!pendingConfirmResolve) return;
        const resolver = pendingConfirmResolve;
        pendingConfirmResolve = null;
        resolver(value);
    };

    const openConfirm = ({
        eyebrow = '확인',
        title = '한 번 더 확인해 주세요',
        message = '이 작업을 계속 진행할까요?',
        confirmText = '확인',
        cancelText = '취소'
    } = {}) => {
        if (!confirmModalEl || !window.bootstrap) {
            return Promise.resolve(false);
        }

        if (pendingConfirmResolve) {
            resolvePendingConfirm(false);
        }

        if (confirmEyebrowEl) confirmEyebrowEl.textContent = eyebrow;
        if (confirmTitleEl) confirmTitleEl.textContent = title;
        if (confirmMessageEl) confirmMessageEl.textContent = message;
        if (confirmAcceptButton) confirmAcceptButton.textContent = confirmText;
        if (confirmCancelButton) confirmCancelButton.textContent = cancelText;

        const confirmModal = getModalInstance(confirmModalEl);
        if (confirmModal) {
            confirmModal.show();
        }

        return new Promise((resolve) => {
            pendingConfirmResolve = resolve;
        });
    };

    const getCreateRoomValidationMessage = (form) => {
        const storeIdx = form.querySelector('input[name="storeIdx"]')?.value;
        const selectedDeliveryAddressIdx = form.querySelector('input[name="selectedDeliveryAddressIdx"]:checked')?.value;
        const paymentMode = form.querySelector('input[name="paymentMode"]:checked')?.value;

        if (!storeIdx) return '가게 정보를 다시 확인해 주세요.';
        if (!selectedDeliveryAddressIdx) return '배송지를 선택해 주세요.';
        if (!paymentMode) return '결제 방식을 선택해 주세요.';
        return '';
    };

    const setCreateRoomFeedback = (form, message = '') => {
        const feedback = form.querySelector('[data-mo-create-feedback]');
        if (!feedback) return;

        feedback.textContent = message;
        feedback.classList.toggle('d-none', !message);
    };

    const syncHeaderState = () => {
        if (!header) return;
        header.classList.toggle('is-scrolled', window.scrollY > 10);
    };

    const closeMobileMenu = () => {
        if (!mobileMenuEl || !mobileMenuEl.classList.contains('show')) return false;

        const mobileMenu = getOffcanvasInstance(mobileMenuEl);
        if (mobileMenu) {
            mobileMenu.hide();
            return true;
        }

        return false;
    };

	const closeLayer = (desktopEl, mobileEl) => {
	    const desktop = getModalInstance(desktopEl);
	    const mobile = getOffcanvasInstance(mobileEl);

	    if (desktopEl && desktopEl.contains(document.activeElement)) {
	        document.activeElement.blur();
	    }

	    if (mobileEl && mobileEl.contains(document.activeElement)) {
	        document.activeElement.blur();
	    }

	    if (desktopEl && desktopEl.classList.contains('show') && desktop) {
	        desktop.hide();
	    }

	    if (mobileEl && mobileEl.classList.contains('show') && mobile) {
	        mobile.hide();
	    }
	};

    const closeAllLayers = () => {
        closeLayer(loginModalEl, mobileLoginSheetEl);
        closeLayer(joinModalEl, mobileJoinSheetEl);
        closeLayer(addressModalEl, mobileAddressSheetEl);
		closeLayer(createRoomModalEl, mobileCreateRoomSheetEl);
    };

	const getCurrentReturnUrl = () => {
	    return window.location.pathname + window.location.search + window.location.hash;
	};

	const syncLoginReturnUrl = () => {
	    const returnUrl = getCurrentReturnUrl();
	    document.querySelectorAll('.js-login-return-url').forEach((input) => {
	        input.value = returnUrl;
	    });
	};
	
	const openLoginLayer = () => {
	    const delayed = closeMobileMenu();
	    closeAllLayers();
	    syncLoginReturnUrl();

	    window.setTimeout(() => {
            if (isMobile()) {
                const mobileLoginSheet = getOffcanvasInstance(mobileLoginSheetEl);
                if (mobileLoginSheet) mobileLoginSheet.show();
            } else {
                const loginModal = getModalInstance(loginModalEl);
                if (loginModal) loginModal.show();
            }
        }, delayed ? 240 : 0);
    };

    const openJoinLayer = () => {
        const delayed = closeMobileMenu();
        closeAllLayers();

        window.setTimeout(() => {
            if (isMobile()) {
                const mobileJoinSheet = getOffcanvasInstance(mobileJoinSheetEl);
                if (mobileJoinSheet) mobileJoinSheet.show();
            } else {
                const joinModal = getModalInstance(joinModalEl);
                if (joinModal) joinModal.show();
            }
        }, delayed ? 240 : 0);
    };
	
	const moveToActiveRoom = (roomCode) => {
	        const delayed = closeMobileMenu();
	        closeAllLayers();

	        window.setTimeout(() => {
	            window.location.href = `/rooms/code/${encodeURIComponent(roomCode)}`;
	        }, delayed ? 240 : 0);
	    };

	    const openJoinEntry = (button) => {
	        const activeRoomCode = button?.dataset?.moActiveRoomCode?.trim();

	        if (activeRoomCode) {
	            moveToActiveRoom(activeRoomCode);
	            return;
	        }

	        openJoinLayer();
	    };

    const openAddressLayer = () => {
        const delayed = closeMobileMenu();
        closeAllLayers();

        window.setTimeout(() => {
            if (isMobile()) {
                const mobileAddressSheet = getOffcanvasInstance(mobileAddressSheetEl);
                if (mobileAddressSheet) mobileAddressSheet.show();
            } else {
                const addressModal = getModalInstance(addressModalEl);
                if (addressModal) addressModal.show();
            }
        }, delayed ? 240 : 0);
    };

	const formatMinimumOrderText = (value) => {
	    const sanitized = String(value ?? '').replace(/[^\d.-]/g, '');
	    const num = Number(sanitized);

	    if (!sanitized || Number.isNaN(num)) {
	        return '최소주문금액 정보 없음';
	    }

	    return `최소주문금액 ${num.toLocaleString('ko-KR')}원`;
	};



	const syncCreateRoomForms = (payload) => {
	    document.querySelectorAll('[data-mo-create-room-form]').forEach((form) => {
	        const storeIdxInput = form.querySelector('input[name="storeIdx"]');
	        const storeNameEl = form.querySelector('[data-mo-store-name]');
	        const minimumEl = form.querySelector('[data-mo-store-minimum]');
	        const submitButton = form.querySelector('[data-mo-create-submit]');
	        const afterCreateInput = form.querySelector('input[name="afterCreate"]');
            const contextEl = form.querySelector('[data-mo-create-context]');

	        if (storeIdxInput) storeIdxInput.value = payload.storeIdx ?? '';
	        if (storeNameEl) storeNameEl.textContent = payload.storeName || '가게를 선택해 주세요';
	        if (minimumEl) minimumEl.textContent = formatMinimumOrderText(payload.minimumOrderAmount);
	        if (afterCreateInput) afterCreateInput.value = payload.afterCreate || '';
            if (contextEl) {
                contextEl.textContent = payload.contextMessage || '선택한 가게와 배송지, 결제 방식을 확인해 주세요.';
            }
            setCreateRoomFeedback(form);

	        if (submitButton) {
	            submitButton.disabled = !validateCreateRoomForm(form);
	        }
	    });
	};

	const validateCreateRoomForm = (form) => {
	    return !getCreateRoomValidationMessage(form);
	};

	const bindCreateRoomForms = () => {
	    document.querySelectorAll('[data-mo-create-room-form]').forEach((form) => {
	        const refresh = () => {
	            const submitButton = form.querySelector('[data-mo-create-submit]');
                const validationMessage = getCreateRoomValidationMessage(form);
	            if (submitButton) {
	                submitButton.disabled = Boolean(validationMessage);
	            }
                if (!validationMessage) {
                    setCreateRoomFeedback(form);
                }
	        };

	        form.addEventListener('change', refresh);

	        form.addEventListener('submit', (event) => {
                const validationMessage = getCreateRoomValidationMessage(form);
	            if (validationMessage) {
	                event.preventDefault();
                    setCreateRoomFeedback(form, validationMessage);
	                return;
	            }
				
				// --- 추가된 로직 시작 ---
				            // 1. 세션 스토리지에서 저장된 총액을 가져옵니다.
				            const totalAmount = sessionStorage.getItem('totalOrderAmount') || '0';
				            
				            // 2. 폼 안에 있는 hidden input 중 이름이 'totalAmount'인 것을 찾습니다.
				            let amountInput = form.querySelector('input[name="totalAmount"]');
				            
				            // 3. 만약 hidden input이 없다면 즉석에서 만들어 넣어줍니다.
				            if (!amountInput) {
				                amountInput = document.createElement('input');
				                amountInput.type = 'hidden';
				                amountInput.name = 'totalAmount';
				                form.appendChild(amountInput);
				            }
				            
				            // 4. 최종 금액을 할당합니다.
				            amountInput.value = totalAmount;
				            // --- 추가된 로직 끝 ---
	        });

	        refresh();
	    });
	};

	const openCreateRoomLayer = (payload) => {
	    const delayed = closeMobileMenu();
	    closeAllLayers();
	    syncCreateRoomForms(payload);

	    window.setTimeout(() => {
	        if (isMobile()) {
	            const mobileCreateSheet = getOffcanvasInstance(mobileCreateRoomSheetEl);
	            if (mobileCreateSheet) mobileCreateSheet.show();
	        } else {
	            const createRoomModal = getModalInstance(createRoomModalEl);
	            if (createRoomModal) createRoomModal.show();
	        }
	    }, delayed ? 240 : 0);
	};

	window.openCreateRoomSheet = (payload = {}) => {
	    if (!payload.storeIdx) {
            showToast('가게 정보를 확인한 뒤 다시 시도해 주세요.', 'warning');
	        return;
	    }

		openCreateRoomLayer({
		    storeIdx: payload.storeIdx,
		    storeName: payload.storeName || '',
		    minimumOrderAmount: payload.minimumOrderAmount || '',
		    afterCreate: payload.afterCreate || '',
            contextMessage: payload.contextMessage || ''
		});
	};

	window.openMoLoginLayer = () => {
	    openLoginLayer();
	};
    window.moShowToast = showToast;
    window.moOpenConfirm = openConfirm;
	
	const bindButtons = () => {
	    document.querySelectorAll('[data-mo-open-mobile-menu]').forEach((button) => {
	        button.addEventListener('click', (event) => {
	            event.preventDefault();
	            const mobileMenu = getOffcanvasInstance(mobileMenuEl);
	            if (mobileMenu) mobileMenu.show();
	        });
	    });

	    document.querySelectorAll('[data-mo-open-login]').forEach((button) => {
	        button.addEventListener('click', (event) => {
	            event.preventDefault();
	            openLoginLayer();
	        });
	    });

	    document.querySelectorAll('[data-mo-open-join]').forEach((button) => {
	        button.addEventListener('click', (event) => {
	            event.preventDefault();
	            openJoinEntry(button);
	        });
	    });

	    document.querySelectorAll('[data-mo-open-address]').forEach((button) => {
	        button.addEventListener('click', (event) => {
	            event.preventDefault();
	            openAddressLayer();
	        });
	    });
	};

    const bindRoomCodeInputs = () => {
        document.querySelectorAll('.mo-room-code-input').forEach((input) => {
            input.addEventListener('input', () => {
                input.value = input.value.replace(/\D/g, '').slice(0, 6);
            });

            input.addEventListener('paste', (event) => {
                event.preventDefault();
                const pasted = (event.clipboardData || window.clipboardData).getData('text');
                input.value = pasted.replace(/\D/g, '').slice(0, 6);
            });
        });
    };

    const bindAutoFocus = () => {
        if (loginModalEl) {
            loginModalEl.addEventListener('shown.bs.modal', () => {
                const target = loginModalEl.querySelector('input[name="memberEmail"]');
                if (target) target.focus();
            });
        }

        if (joinModalEl) {
            joinModalEl.addEventListener('shown.bs.modal', () => {
                const target = joinModalEl.querySelector('.mo-room-code-input');
                if (target) target.focus();
            });
        }

        if (addressModalEl) {
            addressModalEl.addEventListener('shown.bs.modal', () => {
                const target = addressModalEl.querySelector('.mo-address-item');
                if (target) target.focus();
            });
        }

        if (mobileLoginSheetEl) {
            mobileLoginSheetEl.addEventListener('shown.bs.offcanvas', () => {
                const target = mobileLoginSheetEl.querySelector('input[name="memberEmail"]');
                if (target) target.focus();
            });
        }

        if (mobileJoinSheetEl) {
            mobileJoinSheetEl.addEventListener('shown.bs.offcanvas', () => {
                const target = mobileJoinSheetEl.querySelector('.mo-room-code-input');
                if (target) target.focus();
            });
        }

        if (mobileAddressSheetEl) {
            mobileAddressSheetEl.addEventListener('shown.bs.offcanvas', () => {
                const target = mobileAddressSheetEl.querySelector('.mo-address-item');
                if (target) target.focus();
            });
        }
    };

    const bindResponsiveCleanup = () => {
        let lastIsMobile = isMobile();

        window.addEventListener('resize', () => {
            const currentIsMobile = isMobile();

            if (lastIsMobile !== currentIsMobile) {
                closeAllLayers();
                closeMobileMenu();
                lastIsMobile = currentIsMobile;
            }
        });
    };

    if (confirmModalEl) {
        confirmModalEl.addEventListener('hidden.bs.modal', () => {
            resolvePendingConfirm(false);
        });
    }

    if (confirmAcceptButton) {
        confirmAcceptButton.addEventListener('click', () => {
            resolvePendingConfirm(true);
            const confirmModal = getModalInstance(confirmModalEl);
            if (confirmModal) {
                confirmModal.hide();
            }
        });
    }

    syncHeaderState();
    window.addEventListener('scroll', syncHeaderState, { passive: true });

    bindButtons();
    bindRoomCodeInputs();
	bindCreateRoomForms();
    bindAutoFocus();
    bindResponsiveCleanup();
	
	
	// ==========================================
	// [공통] 입력창 유효성 검사 (특수문자 차단 및 천지인 호환)
	// 사용법: input 태그에 class="mo-valid-name" 추가
	// ==========================================
	document.addEventListener('DOMContentLoaded', function() {
	    const targetInputs = document.querySelectorAll('.mo-valid-name');
	    
	    targetInputs.forEach(input => {
	        let isComposing = false;

	        // 한글 조합(천지인 포함) 상태 감지
	        input.addEventListener('compositionstart', () => isComposing = true);
	        input.addEventListener('compositionend', () => {
	            isComposing = false;
	            filterInput(input);
	        });

	        // 입력 중: 특수문자 실시간 차단
	        input.addEventListener('input', function() {
	            if (isComposing) return; // 조합 중이면 건드리지 않음
	            filterInput(this);
	        });

	        function filterInput(el) {
	            const currentVal = el.value;
	            // 특수문자 차단 (영어, 숫자, 한글 자모음, 띄어쓰기만 허용)
	            const regex = /[^a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣\s]/g; 
	            
	            if (regex.test(currentVal)) {
	                el.value = currentVal.replace(regex, '');
	            }
	        }

	        // 포커스 아웃: 단독 자음/모음 찌꺼기 걸러내기
	        input.addEventListener('blur', function() {
	            const currentVal = this.value;
	            const invalidRegex = /[ㄱ-ㅎㅏ-ㅣ]/g; 
	            
	            if (invalidRegex.test(currentVal)) {
                    showToast('자음이나 모음만 단독으로 입력할 수 없습니다. 예: ㅋㅋ, ㅠㅠ', 'warning');
	                this.value = currentVal.replace(invalidRegex, '');
	            }
	        });
	    });
	});
	
})();
