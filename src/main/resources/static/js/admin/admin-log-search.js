window.AdminLogSearchManager = {
    init(formId) {
        const form = document.getElementById(formId);
        if (!form) {
            return;
        }

        const periodSelect = form.querySelector('[data-role="period-control"]');
        const customDateRange = document.getElementById('custom-date-range');
        const pageInput = form.querySelector('input[name="page"]');

        if (!periodSelect || !customDateRange) {
            return;
        }

        function isCustomPeriod() {
            return periodSelect.value === 'CUSTOM';
        }

        function showCustomDateRange() {
            customDateRange.classList.toggle('is-visible', isCustomPeriod());
        }

        periodSelect.addEventListener('change', function () {
            if (pageInput) {
                pageInput.value = 1;
            }

            if (isCustomPeriod()) {
                showCustomDateRange();
                return;
            }

            form.submit();
        });

        showCustomDateRange();
    }
};