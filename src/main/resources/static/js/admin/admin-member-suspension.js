window.addEventListener('DOMContentLoaded', function () {
    const forms = document.querySelectorAll('form[data-suspension-form="temporary"]');

    forms.forEach(form => {
        form.addEventListener('submit', function (event) {
            event.preventDefault();

            const reason = window.prompt('기간정지 사유를 입력해주세요.');
            if (reason === null) {
                return;
            }

            const trimmedReason = reason.trim();
            if (trimmedReason === '') {
                window.alert('기간정지 사유를 입력해주세요.');
                return;
            }

            const untilInput = window.prompt(
                '정지 해제 예정 시각을 입력해주세요.\n' +
                '- 정확한 시각: 2026-05-01T12:00\n' +
                '- 또는 자연수 일수: 7'
            );

            if (untilInput === null) {
                return;
            }

            const trimmedUntil = untilInput.trim();
            if (trimmedUntil === '') {
                window.alert('정지 기간을 입력해주세요.');
                return;
            }

            const isDayNumber = /^\d+$/.test(trimmedUntil);
            const isDateTime = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(trimmedUntil);

            if (!isDayNumber && !isDateTime) {
                window.alert('정지 기간은 yyyy-MM-ddTHH:mm 형식 또는 자연수 일수로 입력해주세요.');
                return;
            }

            const confirmed = window.confirm('이 회원을 기간정지 처리하시겠습니까?');
            if (!confirmed) {
                return;
            }

            form.querySelector('input[name="reason"]').value = trimmedReason;
            form.querySelector('input[name="suspendedUntil"]').value = trimmedUntil;
            form.submit();
        });
    });
});