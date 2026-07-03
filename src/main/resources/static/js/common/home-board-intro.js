document.addEventListener('DOMContentLoaded', function () {
    const titleEl = document.getElementById('board-intro-title');
    const descEl = document.getElementById('board-intro-description');

    if (!titleEl || !descEl) {
        return;
    }

    const defaultTitle = titleEl.dataset.defaultTitle || titleEl.textContent;
    const defaultDescription = descEl.dataset.defaultDescription || descEl.textContent;

    const menuItems = document.querySelectorAll('[data-board-menu-item="true"]');

    function restoreDefault() {
        titleEl.textContent = defaultTitle;
        descEl.textContent = defaultDescription;
    }

    menuItems.forEach(function (item) {
        item.addEventListener('mouseenter', function () {
            const boardName = item.dataset.boardName;
            const boardDescription = item.dataset.boardDescription;

            if (!boardName || !boardDescription) {
                return;
            }

            titleEl.textContent = boardName;
            descEl.textContent = boardDescription;
        });

        item.addEventListener('mouseleave', restoreDefault);

        item.addEventListener('focus', function () {
            const boardName = item.dataset.boardName;
            const boardDescription = item.dataset.boardDescription;

            if (!boardName || !boardDescription) {
                return;
            }

            titleEl.textContent = boardName;
            descEl.textContent = boardDescription;
        });

        item.addEventListener('blur', restoreDefault);
    });
});