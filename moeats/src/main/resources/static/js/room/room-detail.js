document.addEventListener('DOMContentLoaded', function () {
    const copyButton = document.querySelector('.js-copy-room-code');
    const root = document.getElementById('roomDetailRoot');
    const showToast = function (message, variant) {
        if (typeof window.moShowToast === 'function') {
            window.moShowToast(message, variant);
        }
    };
    const requestConfirm = function (options) {
        if (typeof window.moOpenConfirm === 'function') {
            return window.moOpenConfirm(options);
        }
        return Promise.resolve(false);
    };

    const roomCode = root ? root.dataset.roomCode || '' : '';
    const roomStatus = root ? root.dataset.roomStatus || '' : '';

    console.log('[room-detail] loaded', { roomCode, roomStatus });

    if (copyButton) {
        copyButton.addEventListener('click', async function () {
            const copiedRoomCode = this.dataset.roomCode || '';
            if (!copiedRoomCode) return;

            try {
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    await navigator.clipboard.writeText(copiedRoomCode);
                } else {
                    const fallbackInput = document.createElement('textarea');
                    fallbackInput.value = copiedRoomCode;
                    fallbackInput.setAttribute('readonly', '');
                    fallbackInput.style.position = 'absolute';
                    fallbackInput.style.left = '-9999px';
                    document.body.appendChild(fallbackInput);
                    fallbackInput.select();
                    document.execCommand('copy');
                    document.body.removeChild(fallbackInput);
                }
                showToast('방 코드를 복사했어요.', 'success');
            } catch (error) {
                showToast('복사에 실패했어요. 방 코드를 길게 눌러 직접 복사해 주세요.', 'warning');
            }
        });
    }

    let eventSource = null;

    function closeRoomEventSource() {
        if (eventSource) {
            try {
                eventSource.close();
            } catch (e) {
                console.warn('[room-detail] close ignored', e);
            }
            eventSource = null;
        }
    }

	function bindConfirm(selector, messageBuilder, beforeConfirm) {
	    document.querySelectorAll(selector).forEach(function (form) {
	        form.addEventListener('submit', async function (event) {
                event.preventDefault();

	            if (form.dataset.submitting === 'true') {
	                return;
	            }

	            if (typeof beforeConfirm === 'function') {
	                const precheck = beforeConfirm(form);
	                if (precheck === false || (precheck && precheck.valid === false)) {
                        if (precheck && precheck.message) {
                            showToast(precheck.message, precheck.variant || 'warning');
                        }
	                    return;
	                }
	            }

	            const confirmOptions = typeof messageBuilder === 'function'
	                ? messageBuilder(form)
	                : { message: '계속하시겠습니까?' };
                const normalizedOptions = typeof confirmOptions === 'string'
                    ? { message: confirmOptions }
                    : confirmOptions;

	            const confirmed = await requestConfirm(normalizedOptions);
	            if (!confirmed) {
	                return;
	            }

	            form.dataset.submitting = 'true';

	            closeRoomEventSource();

	            window.setTimeout(function () {
	                form.submit();
	            }, 60);
	        });
	    });
	}

    bindConfirm('.js-confirm-kick', function (form) {
        const targetName = form.dataset.targetName || '선택한 참여자';
        return {
            eyebrow: '참여자 관리',
            title: '참여자를 내보낼까요?',
            message: targetName + '님을 주문방에서 내보냅니다.',
            confirmText: '내보내기',
            cancelText: '닫기'
        };
    });

    bindConfirm('.js-confirm-unselect', function () {
        return {
            eyebrow: '주문 다시 확인',
            title: '선택 완료를 해제할까요?',
            message: '메뉴를 다시 확인하면 선택 완료 상태가 해제되고 수정 가능한 흐름으로 돌아갑니다.',
            confirmText: '다시 확인하기',
            cancelText: '취소'
        };
    });

	bindConfirm(
	    '.js-confirm-checkout',
	    function () {
	        return {
                eyebrow: '결제 시작',
                title: '결제를 시작할까요?',
                message: '지금 결제를 시작하면 주문방이 잠기고 결제 화면으로 이동합니다.',
                confirmText: '결제 시작',
                cancelText: '취소'
            };
	    },
	    function () {
	        const totalAmount = Number(root?.dataset?.roomGrandTotal || 0);
	        const minimumOrderAmount = Number(root?.dataset?.minimumOrderAmount || 0);

	        if (minimumOrderAmount > 0 && totalAmount < minimumOrderAmount) {
	            const diff = minimumOrderAmount - totalAmount;
                return {
                    valid: false,
                    message: `최소주문금액까지 ${diff.toLocaleString()}원이 더 필요합니다. 현재 팀 총액은 ${totalAmount.toLocaleString()}원입니다.`
                };
	        }

	        return true;
	    }
	);

    bindConfirm('.js-confirm-leave', function () {
        return {
            eyebrow: '주문방 나가기',
            title: '주문방에서 나갈까요?',
            message: '지금 나가면 현재 주문방 화면에서 바로 빠져나갑니다.',
            confirmText: '나가기',
            cancelText: '머무르기'
        };
    });

    bindConfirm('.js-confirm-cancel', function () {
        return {
            eyebrow: '주문방 종료',
            title: '주문방을 종료할까요?',
            message: '주문방을 종료하면 참여자 전원이 더 이상 이 방을 사용할 수 없습니다.',
            confirmText: '종료하기',
            cancelText: '취소'
        };
    });

    if (roomCode && ['OPEN', 'SELECTING', 'PAYMENT_PENDING'].includes(roomStatus)) {
        const subscribeUrl = '/rooms/code/' + roomCode + '/subscribe';
        console.log('[room-detail] subscribing', subscribeUrl);

        eventSource = new EventSource(subscribeUrl);

        eventSource.onopen = function () {
            console.log('[room-detail] onopen');
        };

        eventSource.addEventListener('connect', function (event) {
            console.log('[room-detail] SSE connected', event.data);
        });

        eventSource.addEventListener('participantUpdate', function (event) {
            console.log('[room-detail] participantUpdate', event.data);
            window.location.reload();
        });

        eventSource.addEventListener('to_order', function (event) {
            console.log('[room-detail] to_order raw', event.data);

            try {
                const data = JSON.parse(event.data);
                if (data && data.orderIdx) {
                    closeRoomEventSource();
                    window.location.href = '/orders/' + data.orderIdx + '/payment';
                    return;
                }
            } catch (e) {
                console.warn('[room-detail] to_order parse failed', e);
            }

            window.location.reload();
        });

        eventSource.addEventListener('cancel', function (event) {
            console.log('[room-detail] cancel', event.data);
            closeRoomEventSource();
            showToast('주문방이 종료되어 홈으로 이동합니다.', 'warning');
            window.setTimeout(function () {
                window.location.href = '/main';
            }, 700);
        });

        eventSource.onerror = function (event) {
            console.warn('[room-detail] SSE error', event);
        };

        window.addEventListener('beforeunload', function () {
            closeRoomEventSource();
        });
    } else {
        console.log('[room-detail] SSE skipped');
    }
});
