(function () {
    function initPageSizeForm(formId) {
        const form = document.getElementById(formId);
        if (!form) {
            return;
        }

        const sizeSelect = form.querySelector('[data-role="page-size-select"]');
        const pageInput = form.querySelector('input[name="page"]');
        const currentPageInput = form.querySelector('[data-role="current-page"]');
        const currentSizeInput = form.querySelector('[data-role="current-size"]');

        if (!sizeSelect || !pageInput || !currentPageInput || !currentSizeInput) {
            return;
        }

        sizeSelect.addEventListener("change", function () {
            const currentPage = Number(currentPageInput.value || 1);   // 1-based
            const currentSize = Number(currentSizeInput.value || 20);
            const newSize = Number(sizeSelect.value || 20);

            const currentOffset = (currentPage - 1) * currentSize;
            const newPage = Math.floor(currentOffset / newSize) + 1;   // 1-based

            pageInput.value = newPage;
            form.submit();
        });
    }

    window.PageSizeManager = {
        initPageSizeForm
    };
})();