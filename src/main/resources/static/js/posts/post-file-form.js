window.PostFileForm = (function () {
    function init(options) {
        const maxFileCount = options.maxFileCount ?? 3;
        const maxSingleFileSize = options.maxSingleFileSize ?? 10 * 1024 * 1024;
        const maxTotalSize = options.maxTotalSize ?? 30 * 1024 * 1024;
        const existingAttachmentCount = options.existingAttachmentCount ?? 0;
        const existingAttachmentTotalSize = options.existingAttachmentTotalSize ?? 0;

        const form = document.getElementById(options.formId);
        const fileInput = document.getElementById(options.fileInputId);
        const selectedAttachments = document.getElementById(options.selectedAttachmentsId);
        const attachmentCount = document.getElementById(options.attachmentCountId);
        const attachmentSize = document.getElementById(options.attachmentSizeId);
        const attachmentWarning = document.getElementById(options.attachmentWarningId);

        let selectedFiles = [];

        const submitButton = form ? form.querySelector('button[type="submit"]') : null;

        fileInput.addEventListener('change', function (event) {
            const newFiles = Array.from(event.target.files || []);
            if (newFiles.length === 0) {
                syncFileInput();
                render();
                return;
            }

            selectedFiles = selectedFiles.concat(newFiles);
            syncFileInput();
            render();
        });

        form.addEventListener('submit', function (event) {
            if (!validateFiles()) {
                event.preventDefault();
            }
        });

        function removeFile(index) {
            selectedFiles.splice(index, 1);
            syncFileInput();
            render();
        }

        function syncFileInput() {
            const dataTransfer = new DataTransfer();
            selectedFiles.forEach(file => dataTransfer.items.add(file));
            fileInput.files = dataTransfer.files;
        }

        function render() {
            selectedAttachments.innerHTML = '';

            selectedFiles.forEach((file, index) => {
                const row = document.createElement('div');
                row.className = 'form-attachment-card';

                if (isImageFile(file)) {
                    const preview = document.createElement('div');
                    preview.className = 'form-attachment-preview';

                    const image = document.createElement('img');
                    image.src = URL.createObjectURL(file);
                    image.alt = file.name;

                    preview.appendChild(image);
                    row.appendChild(preview);
                }

                const metaRow = document.createElement('div');
                metaRow.className = 'form-attachment-row';

                const fileName = document.createElement('span');
                fileName.className = 'form-attachment-name';
                fileName.textContent = file.name + ' (' + formatBytes(file.size) + ')';

                const actionRow = document.createElement('div');
                actionRow.className = 'form-attachment-actions';

                const removeButton = document.createElement('button');
                removeButton.type = 'button';
                removeButton.className = 'btn btn-danger btn-small';
                removeButton.textContent = '첨부파일 삭제';
                removeButton.addEventListener('click', function () {
                    removeFile(index);
                });

                actionRow.appendChild(removeButton);
                metaRow.appendChild(fileName);
                metaRow.appendChild(actionRow);

                row.appendChild(metaRow);
                selectedAttachments.appendChild(row);
            });

            attachmentCount.textContent = selectedFiles.length;
            attachmentSize.textContent = formatBytes(getTotalSize());

            updateSubmitButton();
        }

        function validateFiles() {
            const count = existingAttachmentCount + selectedFiles.length;
            const totalSize = getTotalSize();
            const oversizeFile = selectedFiles.find(file => file.size > maxSingleFileSize);

            if (count > maxFileCount) {
                attachmentWarning.textContent =
                    existingAttachmentCount > 0
                        ? '현재 첨부파일과 새 파일을 합쳐 최대 ' + maxFileCount + '개까지 가능합니다.'
                        : '첨부파일은 최대 ' + maxFileCount + '개까지 선택할 수 있습니다.';
                return false;
            }

            if (oversizeFile) {
                attachmentWarning.textContent =
                    '첨부파일 1개당 최대 용량은 ' + formatBytes(maxSingleFileSize) + '를 초과할 수 없습니다.';
                return false;
            }

            if (totalSize > maxTotalSize) {
                attachmentWarning.textContent =
                    '첨부파일 총 용량은 ' + formatBytes(maxTotalSize) + '를 초과할 수 없습니다.';
                return false;
            }

            attachmentWarning.textContent = '';
            return true;
        }

        function updateSubmitButton() {
            const valid = validateFiles();

            if (submitButton) {
                submitButton.disabled = !valid;
            }
        }

        function getTotalSize() {
            return existingAttachmentTotalSize
                + selectedFiles.reduce((sum, file) => sum + file.size, 0);
        }

        function isImageFile(file) {
            return file.type && file.type.startsWith('image/');
        }

        function formatBytes(bytes) {
            if (bytes < 1024) return bytes + ' B';
            if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
            if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
            return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
        }

        render();
    }

    return {
        init: init
    };
})();