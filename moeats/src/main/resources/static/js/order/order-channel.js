document.addEventListener('DOMContentLoaded', function () {
    const orderIdxInput = document.getElementById('orderIdx');
    const pageTypeInput = document.getElementById('orderSsePageType');

    const orderIdx = orderIdxInput ? orderIdxInput.value : '';
    const pageType = pageTypeInput ? pageTypeInput.value : '';

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

        if (pageType === 'payment-individual'
            || pageType === 'payment-representative'
            || pageType === 'payment-wait') {
            window.location.reload();
        }
    });

    eventSource.addEventListener('complete', function (event) {
        console.log('[order-channel] complete', event.data);
        window.location.href = '/orders/' + orderIdx;
    });

    eventSource.addEventListener('cancel', function (event) {
        console.log('[order-channel] cancel', event.data);
        alert('결제가 취소되었습니다.');
        window.location.href = '/orders/' + orderIdx;
    });

    eventSource.addEventListener('expire', function (event) {
        console.log('[order-channel] expire', event.data);
        alert('결제 시간이 만료되었습니다.');
        window.location.href = '/orders/' + orderIdx;
    });

    eventSource.addEventListener('change', function (event) {
        console.log('[order-channel] change', event.data);

        if (pageType === 'order-detail') {
            window.location.reload();
            return;
        }

        window.location.href = '/orders/' + orderIdx;
    });

    eventSource.onerror = function (event) {
        console.warn('[order-channel] error', event);
    };

    window.addEventListener('beforeunload', function () {
        eventSource.close();
    });
});