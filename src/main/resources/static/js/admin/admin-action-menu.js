document.addEventListener("DOMContentLoaded", function () {
    const menus = Array.from(document.querySelectorAll(".admin-action-menu"));

    if (menus.length === 0) {
        return;
    }

    const EDGE_GAP = 8;
    const PANEL_GAP = 6;

    function clearPanelPosition(menu) {
        const panel = menu.querySelector(".admin-action-menu-panel");
        if (!panel) {
            return;
        }

        panel.style.top = "";
        panel.style.left = "";
        panel.classList.remove("is-positioned");
    }

    function closeMenu(menu) {
        if (!menu.open) {
            return;
        }

        menu.removeAttribute("open");
        clearPanelPosition(menu);
    }

    function closeOtherMenus(currentMenu) {
        menus.forEach(function (menu) {
            if (menu !== currentMenu) {
                closeMenu(menu);
            }
        });
    }

    function closeAllMenus() {
        menus.forEach(closeMenu);
    }

    function positionMenu(menu) {
        if (!menu.open) {
            return;
        }

        const toggle = menu.querySelector(".admin-action-menu-toggle");
        const panel = menu.querySelector(".admin-action-menu-panel");

        if (!toggle || !panel) {
            return;
        }

        panel.classList.remove("is-positioned");

        const toggleRect = toggle.getBoundingClientRect();
        const panelRect = panel.getBoundingClientRect();

        let left = toggleRect.right - panelRect.width;
        let top = toggleRect.bottom + PANEL_GAP;

        if (left < EDGE_GAP) {
            left = EDGE_GAP;
        }

        if (left + panelRect.width > window.innerWidth - EDGE_GAP) {
            left = window.innerWidth - panelRect.width - EDGE_GAP;
        }

        const hasSpaceBelow = top + panelRect.height <= window.innerHeight - EDGE_GAP;
        const hasSpaceAbove = toggleRect.top - panelRect.height - PANEL_GAP >= EDGE_GAP;

        if (!hasSpaceBelow && hasSpaceAbove) {
            top = toggleRect.top - panelRect.height - PANEL_GAP;
        } else if (!hasSpaceBelow) {
            top = Math.max(EDGE_GAP, window.innerHeight - panelRect.height - EDGE_GAP);
        }

        panel.style.left = `${Math.round(left)}px`;
        panel.style.top = `${Math.round(top)}px`;
        panel.classList.add("is-positioned");
    }

    menus.forEach(function (menu) {
        menu.addEventListener("toggle", function () {
            if (menu.open) {
                closeOtherMenus(menu);
                positionMenu(menu);
            } else {
                clearPanelPosition(menu);
            }
        });
    });

    document.addEventListener("click", function (event) {
        if (event.target.closest(".admin-action-menu")) {
            return;
        }

        closeAllMenus();
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeAllMenus();
        }
    });

    window.addEventListener("resize", closeAllMenus);

    window.addEventListener("scroll", function () {
        closeAllMenus();
    }, true);
});