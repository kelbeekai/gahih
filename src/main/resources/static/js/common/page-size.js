(function () {
    function calculatePageAfterSizeChange(form, newSize) {
        const pageInput = form.querySelector('input[name="page"]');
        const currentPageInput = form.querySelector('[data-role="current-page"]');
        const currentSizeInput = form.querySelector('[data-role="current-size"]');

        if (!pageInput || !currentPageInput || !currentSizeInput) {
            return;
        }

        const currentPage = Number(currentPageInput.value || 1);   // 1-based
        const currentSize = Number(currentSizeInput.value || 20);
        const currentOffset = (currentPage - 1) * currentSize;
        const newPage = Math.floor(currentOffset / newSize) + 1;   // 1-based

        pageInput.value = newPage;
    }

    function resetToFirstPage(form) {
        const pageInput = form.querySelector('input[name="page"]');

        if (pageInput) {
            pageInput.value = 1;
        }
    }

    function initPageSizeForm(formId) {
        const form = document.getElementById(formId);
        if (!form) {
            return;
        }

        const sizeSelect = form.querySelector('[data-role="page-size-select"]');

        if (sizeSelect) {
            sizeSelect.addEventListener("change", function () {
                const newSize = Number(sizeSelect.value || 20);

                calculatePageAfterSizeChange(form, newSize);
                form.submit();
            });
        }

        const instantControls = form.querySelectorAll('[data-role="instant-search-control"]');

        instantControls.forEach(function (control) {
            control.addEventListener("change", function () {
                resetToFirstPage(form);
                form.submit();
            });
        });
    }

    window.PageSizeManager = {
        initPageSizeForm
    };
})();