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
    const usernameInput = document.getElementById("username");
    const emailInput = document.getElementById("email");
    const codeInput = document.getElementById("code");
    const sendButton = document.getElementById("sendCodeBtn");
    const verifyButton = document.getElementById("verifyCodeBtn");
    const messageBox = document.getElementById("messageBox");
    const timerBox = document.getElementById("timerBox");

    let timerInterval = null;

    usernameInput.addEventListener("input", clearAuthFeedback);
    emailInput.addEventListener("input", clearAuthFeedback);

    sendButton.addEventListener("click", async function () {
        clearMessage();

        const username = usernameInput.value.trim();
        const email = emailInput.value.trim();

        if (!username) {
            showMessage("아이디를 입력해주세요.", false);
            return;
        }

        if (!email) {
            showMessage("이메일을 입력해주세요.", false);
            return;
        }

        const response = await fetch("/account-recovery/password/send-code", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...getCsrfHeaders()
            },
            body: JSON.stringify({
                username: username,
                email: email
            })
        });

        const data = await response.json();

        if (!data.success) {
            showMessage(data.message, false);
            return;
        }

        showMessage(data.message, true);

        if (data.remainingSeconds !== null && data.remainingSeconds !== undefined) {
            startTimer(data.remainingSeconds);
        } else {
            clearTimer();
        }
    });

    verifyButton.addEventListener("click", async function () {
        clearMessage();

        const username = usernameInput.value.trim();
        const email = emailInput.value.trim();
        const code = codeInput.value.trim();

        if (!username) {
            showMessage("아이디를 입력해주세요.", false);
            return;
        }

        if (!email) {
            showMessage("이메일을 입력해주세요.", false);
            return;
        }

        if (!code) {
            showMessage("인증코드를 입력해주세요.", false);
            return;
        }

        const response = await fetch("/account-recovery/password/verify-code", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...getCsrfHeaders()
            },
            body: JSON.stringify({
                username: username,
                email: email,
                code: code
            })
        });

        const data = await response.json();

        if (!data.success) {
            showMessage(data.message, false);
            return;
        }

        clearTimer();
        showMessage(data.message, true);

        setTimeout(function () {
            window.location.href = "/members/reset-password";
        }, 600);
    });

    function showMessage(message, success) {
        messageBox.textContent = message;
        messageBox.style.color = success ? "green" : "red";
    }

    function clearMessage() {
        messageBox.textContent = "";
    }

    function clearAuthFeedback() {
        clearMessage();
        clearTimer();
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