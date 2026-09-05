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

export function showDeviceConnectGuideModal() {
  return new Promise((resolve) => {
    const container = $("modal-container");
    if (!container) {
      resolve(false);
      return;
    }

    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop";

    backdrop.innerHTML = `
      <div class="modal-card guide-modal-card">
        <div class="modal-header">
          <span class="modal-icon">📱</span>
          <h4>设备连接与接入指南</h4>
        </div>
        <div class="modal-body guide-modal-body">
          <div class="guide-section">
            <h5 class="guide-section-title">🌟 连接设备后可解锁的操作</h5>
            <div class="guide-feature-list">
              <div class="guide-feature-item">
                <strong>📥 读取工作流</strong>
                <span>直接拉取手机/桌面设备上已存储的工作流及版本号</span>
              </div>
              <div class="guide-feature-item">
                <strong>💾 保存工作流</strong>
                <span>向设备端原子保存最新配置，带并发版本冲突检测</span>
              </div>
              <div class="guide-feature-item">
                <strong>🛡️ 设备端校验</strong>
                <span>使用宿主环境真实 Kotlin 引擎进行结构和约束校验</span>
              </div>
              <div class="guide-feature-item">
                <strong>▶️ 远程调试运行</strong>
                <span>一键触发设备执行工作流，控制台实时接收执行日志与事件</span>
              </div>
            </div>
          </div>

          <div class="guide-section">
            <h5 class="guide-section-title">🚀 如何在应用中启动 Bridge 服务</h5>
            <p style="margin: 0 0 6px 0; font-size: 12px; color: #94a3b8;">
              在 Kotlin Multiplatform / Android / Compose Desktop 项目中引入 <code>workflow-editor-bridge</code> 模块：
            </p>
            <div class="guide-code-block">// build.gradle.kts (commonMain)
implementation("com.xiaoyv.workflow:workflow-editor-bridge:&lt;version&gt;")</div>
            <p style="margin: 8px 0 6px 0; font-size: 12px; color: #94a3b8;">
              在应用初始化或调试菜单中启动嵌入式服务：
            </p>
            <div class="guide-code-block">val server = startEditorBridge(
    host = "0.0.0.0", // 允许局域网访问
    port = 8080,
    service = EditorWorkflowService(repository = ...),
    runController = EditorEngineRunController(runtime = ...)
)</div>
            <p style="margin: 8px 0 0 0; font-size: 12px; color: #94a3b8;">
              启动后，在顶部输入设备 IP（如 <code>http://192.168.1.100:8080</code>），点击「连接」即可开启双向实时调试！
            </p>
          </div>
        </div>
        <div class="modal-footer" style="justify-content: space-between;">
          <a href="https://github.com/xiaoyvyv/workflow-cmp" target="_blank" rel="noopener noreferrer" class="guide-link-btn">
            <span>⭐️</span> GitHub 仓库 (xiaoyvyv/workflow-cmp)
          </a>
          <button type="button" class="modal-btn modal-btn-confirm" id="guide-close-btn">我知道了</button>
        </div>
      </div>
    `;

    container.appendChild(backdrop);

    const cleanup = () => {
      document.removeEventListener("keydown", handleKeyDown);
      backdrop.remove();
      resolve(true);
    };

    const handleKeyDown = (e) => {
      if (e.key === "Escape" || e.key === "Enter") {
        e.preventDefault();
        cleanup();
      }
    };

    document.addEventListener("keydown", handleKeyDown);

    backdrop.querySelector("#guide-close-btn")?.addEventListener("click", () => {
      cleanup();
    });

    backdrop.querySelector("#guide-close-btn")?.focus();
  });
}
