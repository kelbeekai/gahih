document.addEventListener('DOMContentLoaded', function () {
    initBoardNavScroll();
    initPageScrollButtons();
    initHeaderDropdowns();
});

function initBoardNavScroll() {
    const scrollArea = document.querySelector('[data-board-scroll]');
    const leftButton = document.querySelector('[data-board-scroll-button="left"]');
    const rightButton = document.querySelector('[data-board-scroll-button="right"]');

    if (!scrollArea || !leftButton || !rightButton) {
        return;
    }

    function updateButtons() {
        const maxScrollLeft = scrollArea.scrollWidth - scrollArea.clientWidth;
        leftButton.disabled = scrollArea.scrollLeft <= 0;
        rightButton.disabled = scrollArea.scrollLeft >= maxScrollLeft - 1;
    }

    function scrollByPage(direction) {
        const amount = Math.max(scrollArea.clientWidth * 0.75, 240);
        scrollArea.scrollBy({
            left: direction * amount,
            behavior: 'smooth'
        });
    }

    leftButton.addEventListener('click', function () {
        scrollByPage(-1);
    });

    rightButton.addEventListener('click', function () {
        scrollByPage(1);
    });

    scrollArea.addEventListener('scroll', updateButtons);
    window.addEventListener('resize', updateButtons);

    updateButtons();
}

function initPageScrollButtons() {
    const topButton = document.querySelector('[data-page-scroll="top"]');
    const bottomButton = document.querySelector('[data-page-scroll="bottom"]');

    if (topButton) {
        topButton.addEventListener('click', function () {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }

    if (bottomButton) {
        bottomButton.addEventListener('click', function () {
            window.scrollTo({
                top: document.documentElement.scrollHeight,
                behavior: 'smooth'
            });
        });
    }
}

function initHeaderDropdowns() {
    const dropdowns = document.querySelectorAll('.header-dropdown');

    document.addEventListener('click', function (event) {
        dropdowns.forEach(function (dropdown) {
            if (!dropdown.contains(event.target)) {
                dropdown.removeAttribute('open');
            }
        });
    });

    dropdowns.forEach(function (dropdown) {
        dropdown.addEventListener('toggle', function () {
            if (!dropdown.open) {
                return;
            }

            dropdowns.forEach(function (otherDropdown) {
                if (otherDropdown !== dropdown) {
                    otherDropdown.removeAttribute('open');
                }
            });
        });
    });
}