(function () {
    const header = document.querySelector('.js-mo-header');
    if (!header) {
        return;
    }

    const syncHeaderState = () => {
        if (window.scrollY > 10) {
            header.classList.add('is-scrolled');
        } else {
            header.classList.remove('is-scrolled');
        }
    };

    syncHeaderState();
    window.addEventListener('scroll', syncHeaderState, { passive: true });
})();
