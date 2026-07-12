function getCsrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (!token || !header) {
        return {};
    }

    return {
        [header]: token
    };
}

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("editForm");
    const emailInput = document.getElementById("email");
    const codeInput = document.getElementById("emailCode");
    const sendButton = document.getElementById("sendEmailCodeBtn");
    const verifyButton = document.getElementById("verifyEmailCodeBtn");
    const submitButton = document.getElementById("editSubmitBtn");
    const messageBox = document.getElementById("emailAuthMessage");
    const timerBox = document.getElementById("emailAuthTimer");
    const verifiedBox = document.getElementById("emailAuthVerified");

    const originalEmail = (form.dataset.originalEmail || "").trim().toLowerCase();
    let verified = form.dataset.emailVerified === "true";
    let timerInterval = null;

    applySubmitState();

    emailInput.addEventListener("input", function () {
        verified = false;
        clearTimer();
        clearMessage();
        verifiedBox.textContent = "";
        applySubmitState();
    });

    sendButton.addEventListener("click", async function () {
        clearMessage();
        verifiedBox.textContent = "";

        const email = emailInput.value.trim();
        if (!email) {
            showMessage("이메일을 입력해주세요.", false);
            return;
        }

        if (email.toLowerCase() === originalEmail) {
            showMessage("현재 사용 중인 이메일입니다.", false);
            return;
        }

        const response = await fetch("/email-auth/change-email/send-code", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...getCsrfHeaders()
            },
            body: JSON.stringify({ email: email })
        });

        const data = await response.json();

        if (!data.success) {
            showMessage(data.message, false);
            return;
        }

        verified = false;
        applySubmitState();
        showMessage(data.message, true);

        if (data.remainingSeconds !== null && data.remainingSeconds !== undefined) {
            startTimer(data.remainingSeconds);
        } else {
            clearTimer();
        }
    });

    verifyButton.addEventListener("click", async function () {
        clearMessage();

        const email = emailInput.value.trim();
        const code = codeInput.value.trim();

        if (!email) {
            showMessage("이메일을 입력해주세요.", false);
            return;
        }

        if (!code) {
            showMessage("인증코드를 입력해주세요.", false);
            return;
        }

        const response = await fetch("/email-auth/change-email/verify-code", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...getCsrfHeaders()
            },
            body: JSON.stringify({
                email: email,
                code: code
            })
        });

        const data = await response.json();

        if (!data.success) {
            showMessage(data.message, false);
            return;
        }

        verified = true;
        clearTimer();
        clearMessage();
        applySubmitState();
        verifiedBox.textContent = data.message;
    });

    function applySubmitState() {
        const currentEmail = emailInput.value.trim().toLowerCase();
        submitButton.disabled = currentEmail !== originalEmail && !verified;
    }

    function showMessage(message, success) {
        messageBox.textContent = message;
        messageBox.style.color = success ? "green" : "red";
    }

    function clearMessage() {
        messageBox.textContent = "";
    }

    function clearTimer() {
        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }
        timerBox.textContent = "";
    }

    function startTimer(remainingSeconds) {
        clearTimer();

        let remaining = Number(remainingSeconds);
        if (!Number.isFinite(remaining) || remaining <= 0) {
            timerBox.textContent = "인증코드가 만료되었습니다. 다시 요청해주세요.";
            return;
        }

        renderTimer(remaining);

        timerInterval = setInterval(function () {
            remaining -= 1;

            if (remaining <= 0) {
                clearTimer();
                timerBox.textContent = "인증코드가 만료되었습니다. 다시 요청해주세요.";
                return;
            }

            renderTimer(remaining);
        }, 1000);
    }

    function renderTimer(remainingSeconds) {
        const minutes = Math.floor(remainingSeconds / 60);
        const seconds = Math.floor(remainingSeconds % 60);

        timerBox.textContent = "남은 시간: " + minutes + "분 " + seconds + "초";
    }
});