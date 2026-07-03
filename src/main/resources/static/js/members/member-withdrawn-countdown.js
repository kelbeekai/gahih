window.addEventListener('DOMContentLoaded', function () {
    const page = document.getElementById('withdrawn-page');
    if (!page) {
        return;
    }

    const expireAtText = page.dataset.expireAt;
    const serverNowText = page.dataset.serverNow;
    const expired = page.dataset.expired === 'true';

    const countdownEl = document.getElementById('withdraw-countdown');
    const restoreButton = document.getElementById('restore-button');

    if (!countdownEl || !expireAtText || !serverNowText || expired) {
        return;
    }

    const expireAt = new Date(expireAtText);
    const serverNow = new Date(serverNowText);
    const clientNowAtLoad = new Date();

    const offset = serverNow.getTime() - clientNowAtLoad.getTime();

    function renderCountdown() {
        const now = new Date(Date.now() + offset);
        const diff = expireAt.getTime() - now.getTime();

        if (diff <= 0) {
            countdownEl.textContent = '복구 가능 시간이 만료되었습니다.';

            if (restoreButton) {
                restoreButton.disabled = true;
                restoreButton.textContent = '복구 불가';
            }
            return;
        }

        const totalSeconds = Math.floor(diff / 1000);
        const days = Math.floor(totalSeconds / 86400);
        const hours = Math.floor((totalSeconds % 86400) / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;

        let text = '';
        if (days > 0) {
            text += days + '일 ';
        }

        text += String(hours).padStart(2, '0') + '시간 ';
        text += String(minutes).padStart(2, '0') + '분 ';
        text += String(seconds).padStart(2, '0') + '초';

        countdownEl.textContent = text;
    }

    renderCountdown();
    setInterval(renderCountdown, 1000);
});