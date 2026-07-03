window.addEventListener('DOMContentLoaded', function () {
    const forms = document.querySelectorAll('form[data-reason-required]');

    forms.forEach(form => {
        form.addEventListener('submit', function (event) {
            event.preventDefault();

            const required = form.dataset.reasonRequired === 'true';
            const label = form.dataset.reasonLabel || '사유';
            const requiredMessage = form.dataset.reasonMessage || `${label}를 입력해주세요.`;
            const optionalMessage = form.dataset.reasonOptionalMessage || `${label}를 입력하세요. 비워두면 사유 없이 처리됩니다.`;
            const confirmMessage = form.dataset.confirmMessage || '정말 진행하시겠습니까?';

            const currentReasonInput = form.querySelector('input[name="reason"]');
            if (currentReasonInput) {
                currentReasonInput.remove();
            }

            const reason = window.prompt(required ? requiredMessage : optionalMessage, '');

            if (reason === null) {
                return;
            }

            const trimmed = reason.trim();

            if (required && trimmed === '') {
                window.alert(requiredMessage);
                return;
            }

            const confirmed = window.confirm(confirmMessage);
            if (!confirmed) {
                return;
            }

            if (trimmed !== '') {
                const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'reason';
                hiddenInput.value = trimmed;
                form.appendChild(hiddenInput);
            }

            form.submit();
        });
    });
});