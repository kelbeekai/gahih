document.addEventListener("DOMContentLoaded", function () {
    const openButtons = document.querySelectorAll(".comment-edit-open-btn");
    const cancelButtons = document.querySelectorAll(".comment-edit-cancel-btn");

    function closeAllEditModes() {
        document.querySelectorAll(".comment-item").forEach(function (commentItem) {
            const contentView = commentItem.querySelector(".comment-content-view");
            const contentEdit = commentItem.querySelector(".comment-content-edit");

            if (contentView) {
                contentView.classList.remove("hidden");
            }
            if (contentEdit) {
                contentEdit.classList.add("hidden");
            }
        });
    }

    openButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const commentId = button.dataset.commentId;
            const commentItem = document.querySelector('.comment-item[data-comment-id="' + commentId + '"]');

            if (!commentItem) {
                return;
            }

            closeAllEditModes();

            const contentView = commentItem.querySelector(".comment-content-view");
            const contentEdit = commentItem.querySelector(".comment-content-edit");

            if (contentView) {
                contentView.classList.add("hidden");
            }
            if (contentEdit) {
                contentEdit.classList.remove("hidden");
            }

            initializeMentionState(commentItem);
        });
    });

    cancelButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const commentId = button.dataset.commentId;
            const commentItem = document.querySelector('.comment-item[data-comment-id="' + commentId + '"]');

            if (!commentItem) {
                return;
            }

            const contentView = commentItem.querySelector(".comment-content-view");
            const contentEdit = commentItem.querySelector(".comment-content-edit");

            if (contentView) {
                contentView.classList.remove("hidden");
            }
            if (contentEdit) {
                contentEdit.classList.add("hidden");
            }
        });
    });

    function escapeHtml(value) {
        if (value == null) {
            return "";
        }
        return value
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }

    function getMentionTargets(textarea) {
        const raw = textarea.dataset.mentionTargets || "";
        if (!raw) {
            return [];
        }

        return raw
            .split("||")
            .map(item => item.trim())
            .filter(item => item.length > 0);
    }

    function getMentionRegex(textarea) {
        const regexSource = textarea.dataset.mentionRegex || "@([가-힣A-Za-z0-9_]{2,12})";
        return new RegExp(regexSource, "g");
    }

    function extractMentionTokens(textarea) {
        const text = textarea.value || "";
        const regex = getMentionRegex(textarea);
        const matches = [];
        let match;

        while ((match = regex.exec(text)) !== null) {
            matches.push({
                nickname: match[1],
                start: match.index,
                end: match.index + match[0].length
            });
        }

        return matches;
    }

    function analyzeMentions(textarea) {
        const text = textarea.value || "";
        const targets = new Set(getMentionTargets(textarea));
        const tokens = extractMentionTokens(textarea);

        const uniqueValidNicknames = new Set();
        const seenHighlightNicknames = new Set();

        const analyzedTokens = tokens.map(token => {
            const valid = targets.has(token.nickname);
            const highlight = valid && !seenHighlightNicknames.has(token.nickname);

            if (valid) {
                uniqueValidNicknames.add(token.nickname);
            }
            if (highlight) {
                seenHighlightNicknames.add(token.nickname);
            }

            return {
                ...token,
                valid,
                highlight
            };
        });

        return {
            text,
            tokens: analyzedTokens,
            uniqueValidNicknames,
            validCount: uniqueValidNicknames.size
        };
    }

    function renderMentionPreview(textarea) {
        const analysis = analyzeMentions(textarea);
        if (analysis.validCount === 0) {
            return "";
        }

        let cursor = 0;
        let html = "";

        analysis.tokens.forEach(token => {
            html += escapeHtml(analysis.text.substring(cursor, token.start));

            const rawMentionText = analysis.text.substring(token.start, token.end);
            if (token.highlight) {
                html += `<span class="comment-mention">${escapeHtml(rawMentionText)}</span>`;
            } else {
                html += escapeHtml(rawMentionText);
            }

            cursor = token.end;
        });

        html += escapeHtml(analysis.text.substring(cursor));
        return html;
    }

    function updateMentionState(container) {
        if (!container) {
            return;
        }

        const textarea = container.querySelector(".mention-aware-textarea");
        const guideBox = container.querySelector(".mention-guide");
        const errorBox = container.querySelector(".mention-limit-error");
        const countText = container.querySelector(".mention-count-text");
        const submitButton = container.querySelector(".comment-submit-btn");
        const previewBox = container.querySelector(".mention-preview");
        const previewContent = container.querySelector(".mention-preview-content");

        if (!textarea || !guideBox || !errorBox || !countText || !submitButton || !previewBox || !previewContent) {
            return;
        }

        const analysis = analyzeMentions(textarea);
        const limitEnabled = textarea.dataset.mentionLimitEnabled === "true";
        const maxMentions = Number(textarea.dataset.maxMentions || "3");
        const exceeded = limitEnabled && analysis.validCount > maxMentions;

        if (limitEnabled) {
            countText.textContent = `태그 ${analysis.validCount}명 / 최대 ${maxMentions}명`;
        } else {
            countText.textContent = `태그 ${analysis.validCount}명`;
        }

        const hasValidMentions = analysis.validCount > 0;

        if (hasValidMentions) {
            guideBox.classList.remove("hidden");
            previewBox.classList.remove("hidden");
            previewContent.innerHTML = renderMentionPreview(textarea);
        } else {
            guideBox.classList.add("hidden");
            previewBox.classList.add("hidden");
            previewContent.innerHTML = "";
        }

        if (exceeded) {
            errorBox.classList.remove("hidden");
            submitButton.disabled = true;
        } else {
            errorBox.classList.add("hidden");
            submitButton.disabled = false;
        }
    }

    function initializeMentionState(container) {
        if (!container) {
            return;
        }

        const textarea = container.querySelector(".mention-aware-textarea");
        if (!textarea) {
            return;
        }

        if (!textarea.dataset.mentionBound) {
            textarea.addEventListener("input", function () {
                updateMentionState(container);
            });
            textarea.dataset.mentionBound = "true";
        }

        updateMentionState(container);
    }

    document.querySelectorAll("form").forEach(function (form) {
        if (form.querySelector(".mention-aware-textarea")) {
            initializeMentionState(form);
        }
    });

    function getActiveMentionTextarea() {
        const visibleEditTextarea = document.querySelector(".comment-content-edit:not(.hidden) .mention-aware-textarea");
        if (visibleEditTextarea) {
            return visibleEditTextarea;
        }

        return document.querySelector('form[action$="/comments"] textarea[name="content"]');
    }

    document.querySelectorAll(".mention-insert-btn").forEach(function (button) {
        button.addEventListener("click", function () {
            const mention = button.dataset.mention;
            if (!mention) {
                return;
            }

            const activeTextarea = getActiveMentionTextarea();

            if (activeTextarea) {
                const form = activeTextarea.closest("form");
                const maxMentions = Number(activeTextarea.dataset.maxMentions || "3");
                const analysis = analyzeMentions(activeTextarea);

                if (analysis.uniqueValidNicknames.has(mention)) {
                    activeTextarea.focus();
                    return;
                }

                const limitEnabled = activeTextarea.dataset.mentionLimitEnabled === "true";

                if (limitEnabled && analysis.validCount >= maxMentions) {
                    const errorBox = form.querySelector(".mention-limit-error");
                    if (errorBox) {
                        errorBox.classList.remove("hidden");
                    }
                    updateMentionState(form);
                    alert(`댓글에서 태그는 최대 ${maxMentions}명까지만 가능합니다.`);
                    return;
                }

                const text = "@" + mention + " ";
                activeTextarea.focus();

                const start = activeTextarea.selectionStart ?? activeTextarea.value.length;
                const end = activeTextarea.selectionEnd ?? activeTextarea.value.length;

                activeTextarea.value =
                    activeTextarea.value.slice(0, start) +
                    text +
                    activeTextarea.value.slice(end);

                const nextPos = start + text.length;
                activeTextarea.setSelectionRange(nextPos, nextPos);

                updateMentionState(form);
                return;
            }

            const currentUrl = new URL(window.location.href);
            currentUrl.searchParams.set("mention", mention);
            currentUrl.searchParams.set("focus", "comment");
            currentUrl.hash = "";

            const redirectUrl = currentUrl.pathname + currentUrl.search;
            window.location.href = "/members/login?redirectURL=" + encodeURIComponent(redirectUrl);
        });
    });

    const commentCreateTextarea = document.querySelector('form[action$="/comments"] textarea[name="content"]');
    if (commentCreateTextarea && commentCreateTextarea.dataset.focusComment === "true") {
        commentCreateTextarea.focus();
        const valueLength = commentCreateTextarea.value.length;
        commentCreateTextarea.setSelectionRange(valueLength, valueLength);
        commentCreateTextarea.scrollIntoView({ behavior: "smooth", block: "center" });
    }
});