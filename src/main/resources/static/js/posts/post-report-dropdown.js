document.addEventListener("DOMContentLoaded", function () {
    const reportDropdownSelector = "details[data-report-dropdown]";
    const reportDropdowns = document.querySelectorAll(reportDropdownSelector);

    if (reportDropdowns.length === 0) {
        return;
    }

    function closeAllReportDropdownsExcept(activeDropdown) {
        reportDropdowns.forEach(function (dropdown) {
            if (dropdown !== activeDropdown) {
                dropdown.open = false;
            }
        });
    }

    reportDropdowns.forEach(function (dropdown) {
        dropdown.addEventListener("toggle", function () {
            if (dropdown.open) {
                closeAllReportDropdownsExcept(dropdown);
            }
        });
    });

    document.addEventListener("click", function (event) {
        if (event.target.closest(reportDropdownSelector)) {
            return;
        }

        closeAllReportDropdownsExcept(null);
    });

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape") {
            return;
        }

        closeAllReportDropdownsExcept(null);
    });
});