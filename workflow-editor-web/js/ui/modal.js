import { $, escapeHtml, cleanJsonText } from "../utils.js";
import { showToast } from "./toast.js";

export function showModalDialog({
  title = "提示",
  message = "",
  type = "info",
  confirmText = "确定",
  cancelText = null,
}) {
  return new Promise((resolve) => {
    const container = $("modal-container");
    if (!container) {
      resolve(true);
      return;
    }

    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop";

    const icon =
      type === "success" ? "✅" : type === "error" ? "❌" : type === "warning" ? "⚠️" : "ℹ️";

    const cancelBtnHtml = cancelText
      ? `<button type="button" class="modal-btn modal-btn-cancel" id="modal-cancel-btn">${escapeHtml(cancelText)}</button>`
      : "";

    backdrop.innerHTML = `
      <div class="modal-card">
        <div class="modal-header">
          <span class="modal-icon">${icon}</span>
          <h4>${escapeHtml(title)}</h4>
        </div>
        <div class="modal-body">${escapeHtml(message)}</div>
        <div class="modal-footer">
          ${cancelBtnHtml}
          <button type="button" class="modal-btn modal-btn-confirm" id="modal-confirm-btn">${escapeHtml(confirmText)}</button>
        </div>
      </div>
    `;

    container.appendChild(backdrop);

    const cleanup = (result) => {
      document.removeEventListener("keydown", handleKeyDown);
      backdrop.remove();
      resolve(result);
    };

    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        e.preventDefault();
        cleanup(false);
      } else if (e.key === "Enter") {
        e.preventDefault();
        cleanup(true);
      }
    };

    document.addEventListener("keydown", handleKeyDown);

    backdrop.querySelector("#modal-confirm-btn")?.addEventListener("click", () => cleanup(true));
    backdrop.querySelector("#modal-cancel-btn")?.addEventListener("click", () => cleanup(false));
    backdrop.querySelector("#modal-confirm-btn")?.focus();
  });
}

export function showImportWorkflowDialog({ onImportFile, onImportText }) {
  return new Promise((resolve) => {
    const container = $("modal-container");
    if (!container) {
      resolve(false);
      return;
    }

    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop";

    backdrop.innerHTML = `
      <div class="modal-card import-modal-card">
        <div class="modal-header">
          <span class="modal-icon">📥</span>
          <h4>导入工作流</h4>
        </div>
        <div class="modal-body import-modal-body">
          <div class="import-options-grid">
            <button type="button" class="import-choice-btn" id="import-choice-file">
              <span class="import-choice-icon">📁</span>
              <div class="import-choice-text">
                <span class="import-choice-title">从文件选择</span>
                <span class="import-choice-desc">选择本地 .json 工作流文件</span>
              </div>
            </button>
          </div>
          <div class="import-divider">
            <span>或在下方粘贴工作流 JSON 后点击导入</span>
          </div>
          <textarea id="import-json-textarea" class="import-json-textarea" placeholder="在此粘贴工作流 JSON 内容..."></textarea>
        </div>
        <div class="modal-footer">
          <button type="button" class="modal-btn modal-btn-cancel" id="import-cancel-btn">取消</button>
          <button type="button" class="modal-btn modal-btn-confirm" id="import-confirm-text-btn">确定导入</button>
        </div>
      </div>
    `;

    container.appendChild(backdrop);

    const cleanup = () => {
      document.removeEventListener("keydown", handleKeyDown);
      backdrop.remove();
    };

    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        e.preventDefault();
        cleanup();
        resolve(false);
      }
    };

    document.addEventListener("keydown", handleKeyDown);

    backdrop.querySelector("#import-cancel-btn")?.addEventListener("click", () => {
      cleanup();
      resolve(false);
    });

    backdrop.querySelector("#import-choice-file")?.addEventListener("click", () => {
      cleanup();
      if (onImportFile) onImportFile();
      resolve(true);
    });

    const textarea = backdrop.querySelector("#import-json-textarea");

    backdrop.querySelector("#import-confirm-text-btn")?.addEventListener("click", () => {
      const text = cleanJsonText(textarea?.value);
      if (!text) {
        textarea?.focus();
        return;
      }
      cleanup();
      if (onImportText) onImportText(text);
      resolve(true);
    });

    textarea?.focus();
  });
}

