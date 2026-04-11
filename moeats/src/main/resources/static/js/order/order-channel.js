document.addEventListener('DOMContentLoaded', function () {
    const orderIdxInput = document.getElementById('orderIdx');
    const pageTypeInput = document.getElementById('orderSsePageType');
    const expiresAtInput = document.getElementById('paymentExpiresAtMs');

    const orderIdx = orderIdxInput ? orderIdxInput.value : '';
    const pageType = pageTypeInput ? pageTypeInput.value : '';

    let countdownHandled = false;

	let navigationLocked = false;
		
    document.querySelectorAll('form[data-payment-complete-form="true"]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }

            form.dataset.submitting = 'true';

            const submitButton = form.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = '결제 처리 중...';
            }
        });
    });

    function formatRemaining(ms) {
        const totalSeconds = Math.max(0, Math.floor(ms / 1000));
        const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, '0');
        const seconds = String(totalSeconds % 60).padStart(2, '0');
        return minutes + ':' + seconds;
    }

    if (expiresAtInput) {
        const expiresAtMs = Number(expiresAtInput.value || 0);
        const countdownTargets = document.querySelectorAll('[data-payment-countdown]');

        if (expiresAtMs > 0 && countdownTargets.length > 0) {
            let timerId = null;

            const handleCountdownExpired = function () {
                if (countdownHandled) {
                    return;
                }
                countdownHandled = true;

                countdownTargets.forEach(function (target) {
                    target.textContent = '00:00';
                });

                if (timerId) {
                    window.clearInterval(timerId);
                }

                // 무한 새로고침 금지
                // FE-07에서는 1회만 주문 상세로 이동
                if (
                    orderIdx &&
                    (pageType === 'payment-individual'
                        || pageType === 'payment-representative'
                        || pageType === 'payment-wait')
                ) {
                    window.location.href = '/orders/' + orderIdx;
                }
            };

            const renderCountdown = function () {
                const remainingMs = expiresAtMs - Date.now();

				if (remainingMs <= 0) {
				    window.clearInterval(timerId);

				    countdownTargets.forEach(function (target) {
				        target.textContent = '00:00';
				    });

				    if (countdownHandled) {
				        return;
				    }
				    countdownHandled = true;

				    return;
				}

                const remainingText = formatRemaining(remainingMs);

                countdownTargets.forEach(function (target) {
                    target.textContent = remainingText;
                });
            };

            renderCountdown();
            timerId = window.setInterval(renderCountdown, 1000);

            window.addEventListener('beforeunload', function () {
                if (timerId) {
                    window.clearInterval(timerId);
                }
            }, { once: true });
        }
    }

    if (!orderIdx) {
        console.log('[order-channel] skipped');
        return;
    }

    const subscribeUrl = '/orders/' + orderIdx + '/subscribe';
    console.log('[order-channel] subscribing', { orderIdx, pageType, subscribeUrl });

    const eventSource = new EventSource(subscribeUrl);

    eventSource.onopen = function () {
        console.log('[order-channel] onopen');
    };

    eventSource.addEventListener('connect', function (event) {
        console.log('[order-channel] connected', event.data);
    });

	eventSource.addEventListener('paid', function (event) {
	    console.log('[order-channel] paid', event.data);

	    if (pageType === 'payment-wait') {
	        window.location.reload();
	    }
	});

    eventSource.addEventListener('complete', function (event) {
        console.log('[order-channel] complete', event.data);
		if (navigationLocked) {
		    return;
		}
		navigationLocked = true;
		window.location.href = '/orders/' + orderIdx;
    });

    eventSource.addEventListener('cancel', function (event) {
        console.log('[order-channel] cancel', event.data);
        alert('결제가 취소되었습니다.');
		if (navigationLocked) {
		    return;
		}
		navigationLocked = true;
		window.location.href = '/orders/' + orderIdx;
    });

    eventSource.addEventListener('expire', function (event) {
        console.log('[order-channel] expire', event.data);
        alert('결제 시간이 만료되었습니다.');
		if (navigationLocked) {
		    return;
		}
		navigationLocked = true;
		window.location.href = '/orders/' + orderIdx;
    });

    eventSource.addEventListener('change', function (event) {
        console.log('[order-channel] change', event.data);

        if (pageType === 'order-detail') {
            window.location.reload();
            return;
        }

		if (navigationLocked) {
		    return;
		}
		navigationLocked = true;
		window.location.href = '/orders/' + orderIdx;
    });

    eventSource.onerror = function (event) {
        console.warn('[order-channel] error', event);
    };

    window.addEventListener('beforeunload', function () {
        eventSource.close();
    });
});