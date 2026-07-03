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
    const emailInput = document.getElementById("email");
    const codeInput = document.getElementById("code");
    const sendButton = document.getElementById("sendCodeBtn");
    const verifyButton = document.getElementById("verifyCodeBtn");
    const messageBox = document.getElementById("messageBox");
    const timerBox = document.getElementById("timerBox");

    let timerInterval = null;

    sendButton.addEventListener("click", async function () {
        clearMessage();

        const email = emailInput.value.trim();
        if (!email) {
            showMessage("이메일을 입력해주세요.", false);
            return;
        }

        const response = await fetch("/account-recovery/username/send-code", {
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

        showMessage(data.message, true);

        if (data.expiresAt) {
            startTimer(data.expiresAt);
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

        const response = await fetch("/account-recovery/username/verify-code", {
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

        clearTimer();
        showMessage(data.message, true);
    });

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

    function startTimer(expiresAt) {
        clearTimer();

        const targetTime = new Date(expiresAt).getTime();

        timerInterval = setInterval(function () {
            const now = Date.now();
            const diff = targetTime - now;

            if (diff <= 0) {
                clearTimer();
                timerBox.textContent = "인증코드가 만료되었습니다. 다시 요청해주세요.";
                return;
            }

            const minutes = Math.floor(diff / 1000 / 60);
            const seconds = Math.floor((diff / 1000) % 60);

            timerBox.textContent = "남은 시간: " + minutes + "분 " + seconds + "초";
        }, 1000);
    }
});