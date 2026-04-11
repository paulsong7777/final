document.addEventListener('DOMContentLoaded', function () {
    const copyButton = document.querySelector('.js-copy-room-code');
    const root = document.getElementById('roomDetailRoot');

    const roomCode = root ? root.dataset.roomCode || '' : '';
    const roomStatus = root ? root.dataset.roomStatus || '' : '';

    console.log('[room-detail] loaded', { roomCode, roomStatus });

    if (copyButton) {
        copyButton.addEventListener('click', async function () {
            const copiedRoomCode = this.dataset.roomCode || '';
            if (!copiedRoomCode) return;

            try {
                await navigator.clipboard.writeText(copiedRoomCode);
                alert('방 코드를 복사했습니다.');
            } catch (error) {
                window.prompt('방 코드를 직접 복사해주세요.', copiedRoomCode);
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

    function bindConfirm(selector, messageBuilder) {
        document.querySelectorAll(selector).forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (form.dataset.submitting === 'true') {
                    return;
                }

                const message = typeof messageBuilder === 'function'
                    ? messageBuilder(form)
                    : '계속하시겠습니까?';

                if (!window.confirm(message)) {
                    event.preventDefault();
                    return;
                }

                event.preventDefault();
                form.dataset.submitting = 'true';

                closeRoomEventSource();

                // SSE 연결 정리 시간을 아주 짧게 준 뒤 submit
                window.setTimeout(function () {
                    form.submit();
                }, 60);
            });
        });
    }

    bindConfirm('.js-confirm-kick', function (form) {
        const targetName = form.dataset.targetName || '선택한 참여자';
        return targetName + '님을 방에서 내보내시겠습니까?';
    });

    bindConfirm('.js-confirm-unselect', function () {
        return '메뉴를 수정하면 선택 완료 상태가 해제됩니다. 계속하시겠습니까?';
    });

    bindConfirm('.js-confirm-checkout', function () {
        return '전원 선택 완료 상태입니다. 결제를 시작하시겠습니까?';
    });

    bindConfirm('.js-confirm-leave', function () {
        return '주문방에서 나가시겠습니까?';
    });

    bindConfirm('.js-confirm-cancel', function () {
        return '주문방을 종료하시겠습니까? 종료 후에는 다시 되돌릴 수 없습니다.';
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
            alert('주문방이 종료되었습니다.');
            window.location.href = '/main';
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