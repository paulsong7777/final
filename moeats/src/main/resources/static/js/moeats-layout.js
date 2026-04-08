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

    const isMobile = () => window.innerWidth < DESKTOP_BREAKPOINT;

    const getModalInstance = (el) => {
        if (!el || !window.bootstrap) return null;
        return window.bootstrap.Modal.getOrCreateInstance(el);
    };

    const getOffcanvasInstance = (el) => {
        if (!el || !window.bootstrap) return null;
        return window.bootstrap.Offcanvas.getOrCreateInstance(el);
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
    };

    const openLoginLayer = () => {
        const delayed = closeMobileMenu();
        closeAllLayers();

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
                openJoinLayer();
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

    syncHeaderState();
    window.addEventListener('scroll', syncHeaderState, { passive: true });

    bindButtons();
    bindRoomCodeInputs();
    bindAutoFocus();
    bindResponsiveCleanup();
})();
