import { EditorConfig } from "../config.js";
import { $, escapeHtml } from "../utils.js";
import { showToast } from "../ui/toast.js";

let eventCount = 0;
let logCount = 0;
let consoleExpandedHeight = 220;

export function appendLog(message, level = "info") {
  logCount++;
  const badge = $("log-count-badge");
  if (badge) badge.textContent = String(logCount);

  const logsContainer = $("logs");
  if (!logsContainer) return;
  const now = new Date();
  const timeStr = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}:${String(now.getSeconds()).padStart(2, "0")}`;

  if (logsContainer.children.length === 1 && logsContainer.children[0].textContent.includes("等待连接")) {
    logsContainer.innerHTML = "";
  }

  const line = document.createElement("div");
  line.className = `log-line log-${level}`;
  line.innerHTML = `<span class="log-time">${timeStr}</span><span>${escapeHtml(message)}</span>`;
  logsContainer.appendChild(line);

  const pane = $("pane-logs");
  if (pane) pane.scrollTop = pane.scrollHeight;
}

export function showEvent(value, getNodeTitle = null) {
  eventCount++;
  const badge = $("event-count-badge");
  if (badge) badge.textContent = String(eventCount);

  const eventsEl = $("events");
  if (eventsEl) {
    if (eventsEl.textContent.includes("尚未连接设备")) {
      eventsEl.textContent = "";
    }
    const raw = typeof value === "object" ? JSON.stringify(value, null, 2) : String(value);
    const now = new Date();
    const timeStr = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}:${String(now.getSeconds()).padStart(2, "0")}`;
    const entry = `[${timeStr}] ${raw}`;
    eventsEl.textContent = `${entry}\n\n${eventsEl.textContent}`.slice(
      0,
      EditorConfig.maximumEventCharacters,
    );
  }

  if (typeof value === "object" && value !== null) {
    const type = value.type || "";
    if (type === EditorConfig.eventType.bridgeConnected) {
      appendLog("🔗 WebSocket 桥接事件连接已建立", "success");
    } else if (type === EditorConfig.eventType.bridgeError) {
      appendLog(`⚠️ 桥接事件异常: ${value.message || ""}`, "error");
    } else if (type === EditorConfig.eventType.workflowSaved) {
      appendLog(`💾 工作流已保存至设备 (版本: ${value.revision})`, "success");
    } else if (type === "engine.workflow.started") {
      appendLog(`🚀 工作流开始运行 (WorkflowId: ${value.workflowId || ""})`, "info");
    } else if (type === "engine.node.started") {
      const title = getNodeTitle ? getNodeTitle(value.nodeId) : value.nodeId;
      appendLog(`▶️ 节点开始执行: [${title}] (${value.nodeId})`, "info");
    } else if (type === "engine.node.completed") {
      const title = getNodeTitle ? getNodeTitle(value.nodeId) : value.nodeId;
      appendLog(`✅ 节点执行完成: [${title}] -> 输出端口: ${value.outputPortId || "next"}`, "success");
    } else if (type === "engine.side_effect.requested") {
      appendLog(`⚡ 节点请求副作用: ${value.nodeId} (${JSON.stringify(value.sideEffect || {})})`, "warn");
    } else if (type === "engine.workflow.completed") {
      appendLog(`🎉 工作流执行完毕 (状态: ${value.status || "success"}, 耗时: ${value.durationMs || 0}ms, 步骤: ${value.stepCount || 0})`, "success");
    } else if (type === "engine.workflow.failed") {
      appendLog(`❌ 工作流执行失败: [${value.errorCode || "error"}] ${value.message || ""} (节点: ${value.nodeId || "unknown"})`, "error");
    } else if (type === "editor.capabilities.auto_completed") {
      appendLog(`⚡ 自动推导补全工作流所需权限: ${(value.capabilities || []).join(", ")}`, "info");
    } else if (value.message) {
      appendLog(`${value.message}`, "info");
    }
  }
}

export function initializeConsoleAndResizers() {
  // 1. Console Tabs & Toggle
  const consolePanel = $("console-panel");
  const toggleBtn = $("console-toggle-btn");
  const toggleText = $("console-toggle-text");
  const copyBtn = $("console-copy-btn");
  const clearBtn = $("console-clear-btn");
  const tabs = document.querySelectorAll(".console-tab");
  const panes = document.querySelectorAll(".console-tab-pane");

  const setConsoleExpanded = (expanded) => {
    if (!consolePanel) return;
    if (expanded) {
      consolePanel.classList.remove("collapsed");
      consolePanel.style.height = `${consoleExpandedHeight}px`;
      if (toggleText) toggleText.textContent = "▼ 收起控制台";
    } else {
      consolePanel.classList.add("collapsed");
      consolePanel.style.height = "";
      if (toggleText) toggleText.textContent = "▲ 展开控制台";
    }
  };

  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      const targetTab = tab.dataset.tab;
      tabs.forEach((t) => t.classList.toggle("active", t === tab));
      panes.forEach((pane) => {
        pane.classList.toggle("active", pane.id === `pane-${targetTab}`);
      });
      if (consolePanel.classList.contains("collapsed")) {
        setConsoleExpanded(true);
      }
    });
  });

  toggleBtn?.addEventListener("click", () => {
    const isCollapsed = consolePanel.classList.contains("collapsed");
    setConsoleExpanded(isCollapsed);
  });

  copyBtn?.addEventListener("click", async () => {
    const activePane = document.querySelector(".console-tab-pane.active");
    const content = activePane?.innerText?.trim();
    if (!content) {
      showToast("当前控制台没有可复制的内容", "info", 1500);
      return;
    }
    try {
      await navigator.clipboard.writeText(content);
      showToast("已复制当前控制台内容", "success", 1500);
    } catch {
      showToast("复制失败，请选中文本后使用 Ctrl/Cmd + C", "error", 2500);
    }
  });

  clearBtn?.addEventListener("click", () => {
    const activeTab = document.querySelector(".console-tab.active")?.dataset.tab;
    if (activeTab === "events") {
      $("events").textContent = "";
      eventCount = 0;
      $("event-count-badge").textContent = "0";
    } else {
      $("logs").innerHTML = '<div class="log-line log-info"><span class="log-time">--:--:--</span> 控制台日志已清空</div>';
      logCount = 0;
      $("log-count-badge").textContent = "0";
    }
    showToast("控制台已清空", "info", 1500);
  });

  // 2. Resizers
  const leftPanel = $("panel-palette");
  const rightPanel = $("panel-inspector");
  const resizerLeft = $("resizer-left");
  const resizerRight = $("resizer-right");
  const resizerBottom = $("resizer-bottom");

  if (resizerLeft && leftPanel) {
    resizerLeft.addEventListener("pointerdown", (e) => {
      e.preventDefault();
      resizerLeft.classList.add("resizing");
      const startX = e.clientX;
      const startWidth = leftPanel.offsetWidth;
      const onPointerMove = (moveEvent) => {
        const delta = moveEvent.clientX - startX;
        const newWidth = Math.min(480, Math.max(170, startWidth + delta));
        leftPanel.style.width = `${newWidth}px`;
      };
      const onPointerUp = () => {
        resizerLeft.classList.remove("resizing");
        window.removeEventListener("pointermove", onPointerMove);
        window.removeEventListener("pointerup", onPointerUp);
      };
      window.addEventListener("pointermove", onPointerMove);
      window.addEventListener("pointerup", onPointerUp);
    });
  }

  if (resizerRight && rightPanel) {
    resizerRight.addEventListener("pointerdown", (e) => {
      e.preventDefault();
      resizerRight.classList.add("resizing");
      const startX = e.clientX;
      const startWidth = rightPanel.offsetWidth;
      const onPointerMove = (moveEvent) => {
        const delta = startX - moveEvent.clientX;
        const newWidth = Math.min(560, Math.max(220, startWidth + delta));
        rightPanel.style.width = `${newWidth}px`;
      };
      const onPointerUp = () => {
        resizerRight.classList.remove("resizing");
        window.removeEventListener("pointermove", onPointerMove);
        window.removeEventListener("pointerup", onPointerUp);
      };
      window.addEventListener("pointermove", onPointerMove);
      window.addEventListener("pointerup", onPointerUp);
    });
  }

  if (resizerBottom && consolePanel) {
    resizerBottom.addEventListener("pointerdown", (e) => {
      e.preventDefault();
      if (consolePanel.classList.contains("collapsed")) {
        setConsoleExpanded(true);
      }
      resizerBottom.classList.add("resizing");
      const startY = e.clientY;
      const startHeight = consolePanel.offsetHeight;
      const onPointerMove = (moveEvent) => {
        const delta = startY - moveEvent.clientY;
        const newHeight = Math.min(window.innerHeight * 0.75, Math.max(80, startHeight + delta));
        consoleExpandedHeight = newHeight;
        consolePanel.style.height = `${newHeight}px`;
      };
      const onPointerUp = () => {
        resizerBottom.classList.remove("resizing");
        window.removeEventListener("pointermove", onPointerMove);
        window.removeEventListener("pointerup", onPointerUp);
      };
      window.addEventListener("pointermove", onPointerMove);
      window.addEventListener("pointerup", onPointerUp);
    });
  }
}
