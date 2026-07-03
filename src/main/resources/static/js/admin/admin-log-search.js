window.AdminLogSearchManager = {
    init(formId) {
        const form = document.getElementById(formId);
        if (!form) {
            return;
        }

        const periodSelect = form.querySelector('#period');
        const customDateRange = document.getElementById('custom-date-range');

        function toggleCustomDateRange() {
            if (!periodSelect || !customDateRange) {
                return;
            }

            const isCustom = periodSelect.value === 'CUSTOM';
            customDateRange.style.display = isCustom ? 'block' : 'none';
        }

        if (periodSelect) {
            periodSelect.addEventListener('change', toggleCustomDateRange);
            toggleCustomDateRange();
        }
    }
};