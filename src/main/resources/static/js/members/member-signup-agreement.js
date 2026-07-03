document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("signupForm");
    const clientErrorBox = document.getElementById("signupClientError");

    if (!form) {
        return;
    }

    initAgreementModals();
    initSignupClientValidation();

    function initAgreementModals() {
        document.querySelectorAll("[data-agreement-open]").forEach(function (trigger) {
            trigger.addEventListener("click", function (event) {
                event.preventDefault();
                openAgreementModal(trigger.dataset.agreementOpen);
            });
        });

        document.querySelectorAll("[data-agreement-checkbox]").forEach(function (checkbox) {
            checkbox.dataset.agreed = checkbox.checked ? "true" : "false";

            checkbox.addEventListener("click", function (event) {
                const alreadyAgreed = checkbox.dataset.agreed === "true";

                if (alreadyAgreed) {
                    checkbox.checked = false;
                    checkbox.dataset.agreed = "false";
                    clearClientError();
                    return;
                }

                event.preventDefault();
                checkbox.checked = false;
                openAgreementModal(checkbox.dataset.agreementCheckbox);
            });
        });

        document.querySelectorAll("[data-agreement-close]").forEach(function (button) {
            button.addEventListener("click", function () {
                closeAgreementModal(button.dataset.agreementClose);
            });
        });

        document.querySelectorAll("[data-agreement-confirm]").forEach(function (checkbox) {
            checkbox.addEventListener("click", function () {
                if (!checkbox.checked) {
                    return;
                }

                const type = checkbox.dataset.agreementConfirm;
                const pageCheckbox = document.querySelector('[data-agreement-checkbox="' + type + '"]');

                if (pageCheckbox) {
                    pageCheckbox.checked = true;
                    pageCheckbox.dataset.agreed = "true";
                }

                closeAgreementModal(type);
                clearClientError();
            });
        });

        document.addEventListener("keydown", function (event) {
            if (event.key !== "Escape") {
                return;
            }

            document.querySelectorAll(".agreement-modal:not(.hidden)").forEach(function (modal) {
                modal.classList.add("hidden");
                modal.setAttribute("aria-hidden", "true");
            });
        });
    }

    function openAgreementModal(type) {
        const modal = document.querySelector('[data-agreement-modal="' + type + '"]');
        const confirmCheckbox = document.querySelector('[data-agreement-confirm="' + type + '"]');

        if (!modal) {
            return;
        }

        if (confirmCheckbox) {
            confirmCheckbox.checked = false;
        }

        modal.classList.remove("hidden");
        modal.setAttribute("aria-hidden", "false");
    }

    function closeAgreementModal(type) {
        const modal = document.querySelector('[data-agreement-modal="' + type + '"]');

        if (!modal) {
            return;
        }

        modal.classList.add("hidden");
        modal.setAttribute("aria-hidden", "true");
    }

    function initSignupClientValidation() {
        form.addEventListener("submit", function (event) {
            clearInvalidStates();

            const message = findFirstValidationMessage();

            if (!message) {
                return;
            }

            event.preventDefault();
            showClientError(message);
        });
    }

    function findFirstValidationMessage() {
        const username = document.getElementById("username");
        const password = document.getElementById("password");
        const passwordConfirm = document.getElementById("passwordConfirm");
        const nickname = document.getElementById("nickname");
        const email = document.getElementById("email");

        if (isBlank(username)) {
            markInvalid(username);
            return "아이디를 입력해주세요.";
        }

        if (username.value.length < 4 || username.value.length > 20) {
            markInvalid(username);
            return "아이디는 4자 이상 20자 이하여야 합니다.";
        }

        if (!/^[a-z0-9](?:[a-z0-9._]*[a-z0-9])?$/.test(username.value)) {
            markInvalid(username);
            return "아이디는 영문 소문자, 숫자, 밑줄(_), 마침표(.)만 사용할 수 있으며 시작과 끝은 영문 소문자 또는 숫자여야 합니다.";
        }

        if (isBlank(password)) {
            markInvalid(password);
            return "비밀번호를 입력해주세요.";
        }

        if (password.value.length < 8 || password.value.length > 64) {
            markInvalid(password);
            return "비밀번호는 8자 이상 64자 이하여야 합니다.";
        }

        if (/\s/.test(password.value) || !/\d/.test(password.value) || !/[!@#$%^&*()_+\-={}\[\]:;"'<>,.?/\\|`~]/.test(password.value)) {
            markInvalid(password);
            return "비밀번호는 공백 없이 숫자 1개 이상과 특수문자 1개 이상을 포함해야 합니다.";
        }

        if (isBlank(passwordConfirm)) {
            markInvalid(passwordConfirm);
            return "비밀번호 확인을 입력해주세요.";
        }

        if (password.value !== passwordConfirm.value) {
            markInvalid(passwordConfirm);
            return "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
        }

        if (isBlank(nickname)) {
            markInvalid(nickname);
            return "닉네임을 입력해주세요.";
        }

        if (nickname.value.length < 2 || nickname.value.length > 12) {
            markInvalid(nickname);
            return "닉네임은 2자 이상 12자 이하여야 합니다.";
        }

        if (!/^[가-힣a-zA-Z0-9_]+$/.test(nickname.value)) {
            markInvalid(nickname);
            return "닉네임은 한글, 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.";
        }

        if (isBlank(email)) {
            markInvalid(email);
            return "이메일을 입력해주세요.";
        }

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
            markInvalid(email);
            return "올바른 이메일 형식이어야 합니다.";
        }

        if (email.value.length < 5 || email.value.length > 100) {
            markInvalid(email);
            return "이메일은 5자 이상 100자 이하여야 합니다.";
        }

        if (form.dataset.emailVerified !== "true") {
            return "이메일 인증을 완료해주세요.";
        }

        if (!isChecked("terms")) {
            return "이용약관에 동의해주세요.";
        }

        if (!isChecked("privacy")) {
            return "개인정보 처리방침에 동의해주세요.";
        }

        if (!isChecked("policy")) {
            return "커뮤니티 운영정책에 동의해주세요.";
        }

        return "";
    }

    function isBlank(input) {
        return !input || !input.value.trim();
    }

    function markInvalid(input) {
        if (!input) {
            return;
        }

        input.classList.add("form-control-invalid");

        input.addEventListener("input", function () {
            input.classList.remove("form-control-invalid");
        }, { once: true });

        input.focus();
    }

    function clearInvalidStates() {
        document.querySelectorAll(".form-control-invalid").forEach(function (input) {
            input.classList.remove("form-control-invalid");
        });
    }

    function isChecked(type) {
        const checkbox = document.querySelector('[data-agreement-checkbox="' + type + '"]');
        return checkbox && checkbox.checked;
    }

    function showClientError(message) {
        if (!clientErrorBox) {
            alert(message);
            return;
        }

        clientErrorBox.textContent = message;
        clientErrorBox.classList.remove("hidden");
        clientErrorBox.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });
    }

    function clearClientError() {
        if (!clientErrorBox) {
            return;
        }

        clientErrorBox.textContent = "";
        clientErrorBox.classList.add("hidden");
    }
});