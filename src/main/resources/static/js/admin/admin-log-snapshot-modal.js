document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("adminLogSnapshotModal");
    const title = document.getElementById("adminLogSnapshotTitle");
    const content = document.getElementById("adminLogSnapshotContent");

    if (!modal || !title || !content) {
        return;
    }

    function readTemplateText(templateElement) {
        if (!templateElement) {
            return "";
        }

        if (templateElement.content) {
            return templateElement.content.textContent || "";
        }

        return templateElement.textContent || "";
    }

    function openModal(button) {
        const targetId = button.dataset.snapshotTarget;
        const modalTitle = button.dataset.snapshotTitle || "관리자 로그 내용";
        const snapshotTemplate = targetId ? document.getElementById(targetId) : null;

        if (!snapshotTemplate) {
            return;
        }

        title.textContent = modalTitle;
        content.textContent = readTemplateText(snapshotTemplate);

        modal.classList.remove("hidden");
        modal.setAttribute("aria-hidden", "false");
        document.body.classList.add("modal-open");
    }

    function closeModal() {
        modal.classList.add("hidden");
        modal.setAttribute("aria-hidden", "true");
        title.textContent = "관리자 로그 내용";
        content.textContent = "";
        document.body.classList.remove("modal-open");
    }

    document.querySelectorAll(".admin-log-snapshot-open-btn").forEach(function (button) {
        button.addEventListener("click", function () {
            openModal(button);
        });
    });

    document.querySelectorAll("[data-admin-log-snapshot-close]").forEach(function (button) {
        button.addEventListener("click", closeModal);
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && !modal.classList.contains("hidden")) {
            closeModal();
        }
    });
});