import { EditorConfig } from "../config.js";
import { state } from "../state.js";
import { $, escapeHtml } from "../utils.js";
import { showToast } from "../ui/toast.js";

let eventCount = 0;
let logCount = 0;
let consoleExpandedHeight = 220;
let runStartTime = 0;
let runStepCount = 0;

function formatSideEffect(effect) {
  if (!effect) return "";
  if (typeof effect === "string") return effect;
  if (typeof effect === "object") {
    return effect.message || effect.title || JSON.stringify(effect);
  }
  return String(effect);
}

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

export function showEvent(value, getNodeTitle = null, renderCallback = null) {
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
    const types = EditorConfig.eventType;
    if (type === types.bridgeConnected) {
      appendLog("🔗 WebSocket 桥接事件连接已建立", "success");
    } else if (type === types.bridgeError) {
      appendLog(`⚠️ 桥接事件异常: ${value.message || ""}`, "error");
    } else if (type === types.workflowSaved) {
      appendLog(`💾 工作流已保存至设备 (版本: ${value.revision})`, "success");
    } else if (type === types.runStarted || type === "engine.workflow.started") {
      runStartTime = value.timestampMillis || Date.now();
      runStepCount = 0;
      const wfId = value.workflowId || value.message || value.runId || "";
      appendLog(`🚀 启动工作流 [${wfId}]`, "info");
    } else if (type === types.nodeStarted || type === "engine.node.started") {
      const nodeId = value.nodeId || "";
      const title = getNodeTitle ? getNodeTitle(nodeId) : "";
      const displayNode = title && title !== nodeId ? `[${title}] (${nodeId})` : nodeId;
      appendLog(`▶️ 准备执行节点: ${displayNode}`, "info");
    } else if (type === types.nodeCompleted || type === "engine.node.completed") {
      runStepCount++;
      const nodeId = value.nodeId || "";
      const title = getNodeTitle ? getNodeTitle(nodeId) : "";
      const displayNode = title && title !== nodeId ? `${title}` : nodeId;
      const portId = value.outputPortId || (value.message && !value.message.startsWith("{") ? value.message : "next");
      const outputData = value.output !== undefined && value.output !== null && typeof value.output === "object" && Object.keys(value.output).length > 0
        ? ` → 输出: ${JSON.stringify(value.output)}`
        : "";
      appendLog(`✅ 节点 [${displayNode}] 执行完成 (出口: ${portId})${outputData}`, "success");
    } else if (type === types.sideEffectRequested || type === "engine.side_effect.requested") {
      const nodeId = value.nodeId || "";
      const title = getNodeTitle ? getNodeTitle(nodeId) : "";
      const displayNode = title && title !== nodeId ? `${title}` : nodeId;
      const effectDesc = formatSideEffect(value.sideEffect || value.effect || value.message);
      appendLog(`⚡ 触发系统动作 [${displayNode}]: ${effectDesc}`, "warn");
    } else if (type === types.runCompleted || type === "engine.workflow.completed") {
      const duration = value.durationMs ?? Math.max(0, Date.now() - runStartTime);
      const steps = value.stepCount ?? runStepCount;
      const status = value.status || "COMPLETED";
      appendLog(`🏁 工作流运行结束 (状态: ${status}, 共 ${steps} 步, 耗时: ${duration}ms)`, "success");
    } else if (type === types.runFailed || type === "engine.workflow.failed") {
      let rawMsg = value.message || value.error?.message || value.errorCode || "未知执行异常";
      if (typeof value.details === "object" && value.details && Object.keys(value.details).length > 0) {
        rawMsg += `\n(详情: ${JSON.stringify(value.details, null, 2)})`;
      }

      // 提取目标出错节点 ID
      let targetNodeId = value.nodeId || value.error?.nodeId || "";
      if (!targetNodeId && typeof rawMsg === "string") {
        const traceMatch = rawMsg.match(/📍\s*节点标识\s*:\s*\[([^\]]+)\]/);
        if (traceMatch && traceMatch[1]) {
          targetNodeId = traceMatch[1].trim();
        } else {
          const bracketMatch = rawMsg.match(/\[([a-zA-Z0-9_\-\.]+)\]/);
          if (bracketMatch && bracketMatch[1] && state.workflow?.nodes?.some((n) => n.id === bracketMatch[1])) {
            targetNodeId = bracketMatch[1].trim();
          }
        }
      }

      // 如果匹配到了对应节点，自动选中并单次高亮提示
      if (targetNodeId && state.workflow?.nodes?.some((n) => n.id === targetNodeId)) {
        state.selectedNodeId = targetNodeId;
        state.errorNodeIds.add(targetNodeId);
        if (renderCallback) renderCallback();

        setTimeout(() => {
          const nodeEl = $(`node-${targetNodeId}`);
          if (nodeEl) {
            nodeEl.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" });
            nodeEl.classList.remove("node-error-flash");
            void nodeEl.offsetWidth; // 触发 reflow 重启动画
            nodeEl.classList.add("node-error-flash");
            setTimeout(() => nodeEl.classList.remove("node-error-flash"), 2000);
          }
        }, 60);
      }

      // 运行发生异常时，自动展开并切换到运行日志控制台
      openConsole("logs");

      // 控制台日志与 Toast 提示（与 core 模块 buildFormattedTraceLog 格式保持一致）
      if (typeof rawMsg === "string" && (rawMsg.includes("[WORKFLOW ERROR TRACE]") || rawMsg.includes("========"))) {
        appendLog(rawMsg, "error");
        const reasonLine = rawMsg
          .split("\n")
          .find((l) => l.includes("错误原因") || l.includes("排查建议"))
          ?.replace(/^.*?[：:]\s*/, "") || "工作流执行失败，请查看控制台溯源报告";
        showToast(`执行失败: ${reasonLine}`, "error", 4500);
      } else {
        const title = (targetNodeId && getNodeTitle) ? getNodeTitle(targetNodeId) : "";
        const displayNode = title && title !== targetNodeId ? `[${title}] (${targetNodeId})` : (targetNodeId ? `[${targetNodeId}]` : "");
        const nodePrefix = displayNode ? `${displayNode} ` : "";
        appendLog(`❌ 执行发生异常: ${nodePrefix}${rawMsg}`, "error");
        showToast(`执行发生异常: ${rawMsg}`, "error", 4000);
      }
    } else if (type === types.capabilitiesAutoCompleted || type === "editor.capabilities.auto_completed") {
      appendLog(`⚡ 自动推导补全工作流所需权限: ${(value.capabilities || []).join(", ")}`, "info");
    } else if (value.message) {
      appendLog(`${value.message}`, "info");
    }
  }
}

export function openConsole(targetTab = "logs") {
  const consolePanel = $("console-panel");
  const toggleText = $("console-toggle-text");
  if (consolePanel) {
    consolePanel.classList.remove("collapsed");
    consolePanel.style.height = `${consoleExpandedHeight}px`;
    if (toggleText) toggleText.textContent = "▼ 收起控制台";
  }
  if (targetTab) {
    const tabs = document.querySelectorAll(".console-tab");
    const panes = document.querySelectorAll(".console-tab-pane");
    tabs.forEach((t) => t.classList.toggle("active", t.dataset.tab === targetTab));
    panes.forEach((pane) => {
      pane.classList.toggle("active", pane.id === `pane-${targetTab}`);
    });
  }
}

export function clearAllConsoleLogs() {
  const eventsEl = $("events");
  if (eventsEl) {
    eventsEl.textContent = "";
  }
  eventCount = 0;
  const eventBadge = $("event-count-badge");
  if (eventBadge) eventBadge.textContent = "0";

  const logsEl = $("logs");
  if (logsEl) {
    logsEl.innerHTML = "";
  }
  logCount = 0;
  const logBadge = $("log-count-badge");
  if (logBadge) logBadge.textContent = "0";
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
