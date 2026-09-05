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
                <span class="import-choice-desc">选择本地 .json 工作流</span>
              </div>
            </button>
            <button type="button" class="import-choice-btn" id="import-choice-clip">
              <span class="import-choice-icon">📋</span>
              <div class="import-choice-text">
                <span class="import-choice-title">从剪贴板读取</span>
                <span class="import-choice-desc">一键读取系统剪贴板</span>
              </div>
            </button>
          </div>
          <div class="import-divider">
            <span>或直接在下方粘贴 JSON</span>
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

    backdrop.querySelector("#import-choice-clip")?.addEventListener("click", async () => {
      const textarea = backdrop.querySelector("#import-json-textarea");
      try {
        let text = "";
        if (navigator.clipboard && typeof navigator.clipboard.readText === "function") {
          text = await navigator.clipboard.readText();
        }
        const cleaned = cleanJsonText(text);
        if (cleaned) {
          try {
            const parsed = JSON.parse(cleaned);
            if (parsed && typeof parsed === "object" && Array.isArray(parsed.nodes)) {
              cleanup();
              if (onImportText) onImportText(cleaned);
              resolve(true);
              return;
            }
          } catch {
            // Not valid JSON directly, put in textarea
          }
          if (textarea) {
            textarea.value = cleaned;
            textarea.focus();
          }
          return;
        }
        if (textarea) {
          textarea.placeholder = "剪贴板为空或未授权，请在此处按 Cmd/Ctrl + V 粘贴...";
          textarea.focus();
        }
      } catch (err) {
        // Firefox by default blocks clipboard.readText() on general web pages
        showToast("火狐/浏览器限制直接读取剪贴板，请在输入框按 Cmd/Ctrl+V 粘贴", "info", 3000);
        if (textarea) {
          textarea.placeholder = "浏览器安全策略限制直接读取剪贴板，请在此处按快捷键粘贴 (Cmd/Ctrl + V)...";
          textarea.focus();
        }
      }
    });

    const textarea = backdrop.querySelector("#import-json-textarea");

    // 监听文本框粘贴与输入：若粘贴了合法工作流 JSON，立即自动执行导入并关闭弹窗
    textarea?.addEventListener("paste", (e) => {
      const pasted = e.clipboardData?.getData("text/plain") || e.clipboardData?.getData("text");
      const cleaned = cleanJsonText(pasted || textarea.value);
      if (cleaned && cleaned.startsWith("{") && cleaned.endsWith("}")) {
        try {
          const parsed = JSON.parse(cleaned);
          if (parsed && typeof parsed === "object" && Array.isArray(parsed.nodes)) {
            e.preventDefault();
            cleanup();
            if (onImportText) onImportText(cleaned);
            resolve(true);
            return;
          }
        } catch {
          // Not complete JSON yet, let normal paste proceed
        }
      }
      setTimeout(() => {
        const val = cleanJsonText(textarea.value);
        if (val && val.startsWith("{") && val.endsWith("}")) {
          try {
            const parsed = JSON.parse(val);
            if (parsed && typeof parsed === "object" && Array.isArray(parsed.nodes)) {
              cleanup();
              if (onImportText) onImportText(val);
              resolve(true);
            }
          } catch {
            // ignore
          }
        }
      }, 20);
    });

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

