import { EditorConfig } from "../config.js";
import { state, getNodeTitle, ensureWorkflowCapabilities } from "../state.js";
import { $, cleanJsonText } from "../utils.js";
import { showToast } from "../ui/toast.js";
import { showModalDialog, showImportWorkflowDialog } from "../ui/modal.js";
import { showEvent, appendLog, openConsole, clearAllConsoleLogs } from "../console/console.js";
import { inspectorIsValid } from "../inspector/inspector.js";
import { executeAutoLayout } from "../canvas/layout.js";
import { zoomFit } from "../canvas/interaction.js";

export function baseUrl() {
  let configured = $("device")?.value?.trim()?.replace(/\/+$/, "") || "";
  if (configured && !configured.startsWith("http://") && !configured.startsWith("https://")) {
    configured = `http://${configured}`;
  }
  if (configured) return configured;
  if (typeof window !== "undefined" && window.location.protocol.startsWith("http")) {
    return window.location.origin;
  }
  return EditorConfig.defaultDeviceUrl;
}

export function updateConnectionUI(status, deviceName = "") {
  const dotEl = $("device-status-dot");
  const connectBtn = $("connect");

  if (dotEl) {
    dotEl.className = `device-status-dot status-${status}`;
    dotEl.title =
      status === "connected"
        ? `已连接: ${deviceName || "WorkflowCMP 设备"}`
        : status === "connecting"
        ? "正在连接设备..."
        : status === "error"
        ? "连接失败"
        : "设备未连接";
  }

  if (connectBtn) {
    connectBtn.classList.toggle("is-connected", status === "connected");
    if (status === "connected") {
      connectBtn.textContent = "已连接";
      connectBtn.title = "设备已连接 (点击可重新连接)";
    } else if (status === "connecting") {
      connectBtn.textContent = "连接中";
      connectBtn.title = "正在连接中...";
    } else {
      connectBtn.textContent = "连接";
      connectBtn.title = "连接设备";
    }
  }
}

export function initializeDeviceAddress() {
  const deviceEl = $("device");
  if (deviceEl) {
    if (typeof window !== "undefined" && window.location.protocol.startsWith("http")) {
      deviceEl.value = window.location.origin;
    } else if (!deviceEl.value.trim()) {
      deviceEl.value = EditorConfig.defaultDeviceUrl;
    }
  }
}

export function applyWorkflowData(parsed, renderCallback) {
  if (!parsed || typeof parsed !== "object" || !Array.isArray(parsed.nodes)) {
    throw new Error("工作流格式不正确，缺少 nodes 节点列表");
  }
  state.workflow = parsed;
  if (!Array.isArray(state.workflow.edges)) {
    state.workflow.edges = [];
  }
  state.revision = 0;
  state.selectedNodeId = null;
  state.selectedEdgeId = null;
  state.errorNodeIds.clear();

  // 导入工作流时清空控制台历史日志与事件
  clearAllConsoleLogs();

  // 导入后始终按当前选择的布局类型重新排版，避免保留其它环境的旧坐标。
  executeAutoLayout(state.workflow, state.layoutDirection || "dag-lr");

  if (renderCallback) renderCallback();
  setTimeout(zoomFit, 30);
  showToast(`已成功导入工作流: ${parsed.name || parsed.id || ""}`, "success");
}

export function authHeaders() {
  const token = $("token")?.value;
  return token
    ? {
        [EditorConfig.api.authorizationHeader]:
          `${EditorConfig.api.bearerPrefix}${token}`,
      }
    : {};
}

export function connectEvents(renderCallback = null) {
  state.eventSocket?.close();
  const base = baseUrl();
  if (!base) return;
  const token = $("token")?.value;
  const eventQuery = token
    ? `?${EditorConfig.api.eventAccessTokenQuery}=${encodeURIComponent(token)}`
    : "";
  const eventUrl = `${base.replace(/^http/, "ws")}${EditorConfig.api.events}${eventQuery}`;
  state.eventSocket = new WebSocket(eventUrl);
  state.eventSocket.onopen = () => {
    showToast("设备事件连接已建立", "success");
    showEvent({ type: EditorConfig.eventType.bridgeConnected }, getNodeTitle, renderCallback);
  };
  state.eventSocket.onmessage = (event) => {
    try {
      showEvent(JSON.parse(event.data), getNodeTitle, renderCallback);
    } catch {
      showEvent({
        type: EditorConfig.eventType.bridgeMessage,
        message: event.data,
      }, getNodeTitle, renderCallback);
    }
  };
  state.eventSocket.onerror = () => {
    showToast("设备事件连接失败", "error");
    showEvent({
      type: EditorConfig.eventType.bridgeError,
      message: "事件连接失败",
    }, getNodeTitle, renderCallback);
  };
}

export function validateWorkflowLocal(workflow = state.workflow) {
  const issues = [];
  const nodes = workflow?.nodes || [];

  const startNodes = nodes.filter((n) => n.type === "flow.start");
  if (startNodes.length === 0) {
    issues.push({
      code: "missing_start_node",
      message: "工作流必须包含一个开始节点 (flow.start)",
      level: "error",
    });
  } else if (startNodes.length > 1) {
    startNodes.slice(1).forEach((node) => {
      issues.push({
        code: "multiple_start_nodes",
        message: `工作流只能包含一个开始节点 (flow.start)，当前存在 ${startNodes.length} 个`,
        level: "error",
        nodeId: node.id,
      });
    });
  } else {
    if (workflow.entryNodeId !== startNodes[0].id) {
      workflow.entryNodeId = startNodes[0].id;
    }
  }

  const endNodes = nodes.filter((n) => n.type === "flow.end");
  if (endNodes.length === 0) {
    issues.push({
      code: "missing_end_node",
      message: "工作流建议包含至少一个结束节点 (flow.end)",
      level: "warning",
    });
  }

  return {
    isValid: issues.filter((i) => i.level === "error").length === 0,
    issues,
  };
}

export async function validateWorkflowRemote(renderCallback) {
  if (!inspectorIsValid()) {
    return {
      isValid: false,
      issues: [{ message: "部分字段配置不符合约束规则", nodeId: state.selectedNodeId }],
    };
  }

  const localValidation = validateWorkflowLocal(state.workflow);
  ensureWorkflowCapabilities();

  let remoteValidation = { isValid: true, issues: [] };
  if (baseUrl()) {
    const response = await fetch(
      `${baseUrl()}${EditorConfig.api.validateWorkflow}`,
      {
        method: "POST",
        headers: { ...authHeaders(), "content-type": "application/json" },
        body: JSON.stringify({ workflow: state.workflow }),
      },
    );
    if (!response.ok) {
      throw Error((await response.text()) || response.statusText);
    }
    const payload = await response.json();
    remoteValidation = payload.validation || { isValid: true, issues: [] };
  }

  const mergedIssues = [...localValidation.issues, ...(remoteValidation.issues || [])];
  const isValid = localValidation.isValid && remoteValidation.isValid;

  const validation = {
    isValid,
    issues: mergedIssues,
  };

  state.errorNodeIds.clear();
  if (!validation.isValid && validation.issues) {
    validation.issues.forEach((issue) => {
      if (issue.nodeId) {
        state.errorNodeIds.add(issue.nodeId);
      }
    });
    const firstError = validation.issues.find((issue) => issue.nodeId);
    if (firstError && firstError.nodeId) {
      state.selectedNodeId = firstError.nodeId;
      const el = document.getElementById(`node-${state.selectedNodeId}`);
      el?.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" });
    }
  }
  if (renderCallback) renderCallback();
  return validation;
}

export async function saveWorkflowRemote(renderCallback) {
  if (!inspectorIsValid()) return false;

  const localValidation = validateWorkflowLocal(state.workflow);
  if (!localValidation.isValid) {
    state.errorNodeIds.clear();
    localValidation.issues.forEach((issue) => {
      if (issue.nodeId) state.errorNodeIds.add(issue.nodeId);
    });
    const firstError = localValidation.issues.find((i) => i.nodeId);
    if (firstError?.nodeId) {
      state.selectedNodeId = firstError.nodeId;
    }
    if (renderCallback) renderCallback();
    const issuesText = localValidation.issues.map((i) => `• ${i.message}`).join("\n");
    showModalDialog({
      title: "工作流校验未通过，无法保存",
      message: `${issuesText}\n\n请修改结构后再保存。`,
      type: "warning",
    });
    return false;
  }

  ensureWorkflowCapabilities();
  const response = await fetch(
    `${baseUrl()}${EditorConfig.api.workflows}/${encodeURIComponent(state.workflow.id)}`,
    {
      method: "PUT",
      headers: { ...authHeaders(), "content-type": "application/json" },
      body: JSON.stringify({ baseRevision: state.revision, workflow: state.workflow }),
    },
  );
  if (response.status === EditorConfig.httpConflict) {
    const current = await response.json();
    const remoteRev = current.current?.revision ?? "未知";
    const confirmed = await showModalDialog({
      title: "版本冲突",
      message: `设备端当前版本为 ${remoteRev}，本地版本为 ${state.revision}。\n\n是否强制覆盖推送到设备端？`,
      type: "warning",
      confirmText: "覆盖推送",
      cancelText: "取消",
    });
    if (confirmed) {
      const forceResponse = await fetch(
        `${baseUrl()}${EditorConfig.api.workflows}/${encodeURIComponent(state.workflow.id)}`,
        {
          method: "PUT",
          headers: { ...authHeaders(), "content-type": "application/json" },
          body: JSON.stringify({
            baseRevision: current.current?.revision ?? 0,
            workflow: state.workflow,
          }),
        },
      );
      if (!forceResponse.ok) {
        throw Error((await forceResponse.text()) || forceResponse.statusText);
      }
      const document = await forceResponse.json();
      state.workflow = document.workflow;
      state.revision = document.revision;
      showEvent({ type: EditorConfig.eventType.workflowSaved, revision: state.revision }, getNodeTitle);
      if (renderCallback) renderCallback();
      return true;
    }
    return false;
  }
  if (!response.ok) {
    throw Error((await response.text()) || response.statusText);
  }
  const document = await response.json();
  state.workflow = document.workflow;
  state.revision = document.revision;
  showEvent({ type: EditorConfig.eventType.workflowSaved, revision: state.revision }, getNodeTitle);
  if (renderCallback) renderCallback();
  return true;
}

export async function performConnect(renderCallback, isAuto = false) {
  const targetUrl = baseUrl();
  if (!targetUrl) {
    if (!isAuto) showToast("请填写设备地址，例如 http://localhost:8080", "warning");
    return;
  }
  const connectBtn = $("connect");
  updateConnectionUI("connecting");
  if (connectBtn) connectBtn.disabled = true;

  try {
    // 1. 获取设备信息
    let deviceName = "";
    try {
      const deviceRes = await fetch(`${targetUrl}${EditorConfig.api.device}`, {
        headers: authHeaders(),
      });
      if (deviceRes.ok) {
        const deviceData = await deviceRes.json();
        deviceName = deviceData.name || "";
      }
    } catch {
      // ignore optional device descriptor failure
    }

    // 2. 获取 Manifest 节点类型元数据
    const response = await fetch(`${targetUrl}${EditorConfig.api.manifest}`, {
      headers: authHeaders(),
    });
    if (!response.ok) throw Error(`HTTP ${response.status}: ${response.statusText}`);
    const payload = await response.json();
    state.manifest = payload.manifest ?? payload;

    // 3. 建立 WebSocket 实时事件通道
    connectEvents(renderCallback);

    // 4. 更新连接状态 UI 并记录日志
    updateConnectionUI("connected", deviceName);
    if (renderCallback) renderCallback();
    const count = state.manifest?.nodeTypes?.length || 0;
    showToast(`已成功连接设备${deviceName ? ` (${deviceName})` : ""}`, "success");
    appendLog(`🔗 成功连接设备: ${targetUrl}${deviceName ? ` [${deviceName}]` : ""}，共加载 ${count} 个节点定义`, "success");
  } catch (error) {
    updateConnectionUI("error");
    appendLog(`❌ 连接设备失败: ${error.message}`, "error");
    if (!isAuto) {
      showModalDialog({
        title: "连接设备失败",
        message: `无法连接设备：${error.message}\n请检查设备端服务是否已启动，且 PC 与手机在同一 Wi-Fi 局域网内。`,
        type: "error",
      });
    }
  } finally {
    if (connectBtn) connectBtn.disabled = false;
  }
}

export function initializeToolbarActions(renderCallback) {
  const fileInput = $("file");

  $("connect")?.addEventListener("click", () => {
    performConnect(renderCallback, false);
  });

  $("device")?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      performConnect(renderCallback, false);
    }
  });

  $("token")?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      performConnect(renderCallback, false);
    }
  });

  $("load")?.addEventListener("click", async () => {
    try {
      const response = await fetch(
        `${baseUrl()}${EditorConfig.api.workflows}/${encodeURIComponent(state.workflow.id)}`,
        { headers: authHeaders() },
      );
      if (response.status === EditorConfig.httpNotFound) {
        showToast("设备上尚未保存此工作流", "warning");
        return;
      }
      if (!response.ok) throw Error(response.statusText);
      const document = await response.json();
      state.workflow = document.workflow;
      state.revision = document.revision;
      state.selectedNodeId = null;
      state.errorNodeIds.clear();
      clearAllConsoleLogs();
      executeAutoLayout(state.workflow, state.layoutDirection || "dag-lr");
      if (renderCallback) renderCallback();
      setTimeout(zoomFit, 30);
      showToast(`工作流已读取 (版本: ${state.revision})`, "success");
    } catch (error) {
      showModalDialog({
        title: "读取失败",
        message: error.message,
        type: "error",
      });
    }
  });

  $("save")?.addEventListener("click", async () => {
    try {
      const saved = await saveWorkflowRemote(renderCallback);
      if (saved) {
        showToast("工作流已成功保存至设备", "success");
      }
    } catch (error) {
      showModalDialog({
        title: "保存失败",
        message: error.message,
        type: "error",
      });
    }
  });

  $("validate")?.addEventListener("click", async () => {
    try {
      const validation = await validateWorkflowRemote(renderCallback);
      if (validation.isValid) {
        showModalDialog({
          title: "设备端校验结果",
          message: "工作流结构与约束校验全部通过，无发现问题。",
          type: "success",
        });
      } else {
        const issues = (validation.issues || []).map((issue) => `• ${issue.message}`).join("\n");
        showModalDialog({
          title: "设备端校验未通过",
          message: issues || "未知校验异常",
          type: "warning",
        });
      }
    } catch (error) {
      showModalDialog({
        title: "校验请求失败",
        message: error.message,
        type: "error",
      });
    }
  });

  $("run")?.addEventListener("click", async () => {
    if (!baseUrl()) {
      showToast("请先填写设备地址并连接设备", "warning");
      return;
    }
    try {
      ensureWorkflowCapabilities();
      const validation = await validateWorkflowRemote(renderCallback);
      if (!validation.isValid) {
        const issues = (validation.issues || [])
          .map((issue) => `• ${issue.message}`)
          .join("\n");
        showModalDialog({
          title: "工作流校验未通过，无法运行",
          message: issues || "配置存在错误",
          type: "error",
        });
        return;
      }

      const saved = await saveWorkflowRemote(renderCallback);
      if (!saved) return;

      const response = await fetch(
        `${baseUrl()}${EditorConfig.api.workflows}/${encodeURIComponent(state.workflow.id)}/runs`,
        {
          method: "POST",
          headers: { ...authHeaders(), "content-type": "application/json" },
          body: JSON.stringify({ revision: state.revision }),
        },
      );
      if (!response.ok) {
        const errorText = (await response.text()) || `HTTP ${response.status} ${response.statusText}`;
        openConsole("logs");
        appendLog(`❌ 推送执行失败: ${errorText}`, "error");
        throw Error(errorText);
      }
      showToast("已成功向设备发起工作流执行请求", "success");
      showEvent(await response.json(), getNodeTitle, renderCallback);
    } catch (error) {
      openConsole("logs");
      appendLog(`❌ 运行请求异常: ${error.message}`, "error");
      showModalDialog({
        title: "运行失败",
        message: error.message,
        type: "error",
      });
    }
  });

  $("manifest")?.addEventListener("click", () => {
    if (!fileInput) return;
    fileInput.dataset.kind = "manifest";
    fileInput.click();
  });

  $("workflow")?.addEventListener("click", () => {
    showImportWorkflowDialog({
      onImportFile: () => {
        if (!fileInput) return;
        fileInput.dataset.kind = "workflow";
        fileInput.click();
      },
      onImportText: (text) => {
        try {
          const parsed = JSON.parse(text);
          applyWorkflowData(parsed, renderCallback);
        } catch (err) {
          showModalDialog({
            title: "工作流解析失败",
            message: `剪贴板或输入的文本不是有效的工作流 JSON：\n${err.message}`,
            type: "error",
          });
        }
      },
    });
  });

  fileInput?.addEventListener("change", async () => {
    if (!fileInput.files || fileInput.files.length === 0) return;
    try {
      const text = await fileInput.files[0].text();
      const parsed = JSON.parse(text);
      if (fileInput.dataset.kind === "manifest") {
        state.manifest = parsed;
        showToast("已成功导入 Manifest", "success");
        if (renderCallback) renderCallback();
      } else {
        applyWorkflowData(parsed, renderCallback);
      }
    } catch (err) {
      showModalDialog({
        title: "导入失败",
        message: `无法解析所选文件：\n${err.message}`,
        type: "error",
      });
    } finally {
      fileInput.value = "";
    }
  });

  $("export")?.addEventListener("click", () => {
    const localValidation = validateWorkflowLocal(state.workflow);
    if (!localValidation.isValid) {
      const issuesText = localValidation.issues.map((i) => `• ${i.message}`).join("\n");
      showToast("提示：当前工作流结构存在未满足的起止节点约束", "warning");
    }
    const anchor = document.createElement("a");
    anchor.href = URL.createObjectURL(
      new Blob([JSON.stringify(state.workflow, null, 2)], { type: "application/json" }),
    );
    anchor.download = "workflow.json";
    anchor.click();
    URL.revokeObjectURL(anchor.href);
  });

  // 支持在画布上直接使用快捷键 Cmd+V / Ctrl+V 快速导入工作流
  document.addEventListener("paste", async (event) => {
    const activeEl = document.activeElement;
    const isEditingInput = activeEl && ["input", "textarea"].includes(activeEl.tagName?.toLowerCase());
    if (isEditingInput) return;

    const raw = event.clipboardData?.getData("text/plain") || event.clipboardData?.getData("text");
    const text = cleanJsonText(raw);
    if (!text) return;

    // 仅在文本看起来像 JSON 对象时尝试解析
    if (text.startsWith("{") && text.endsWith("}")) {
      try {
        const parsed = JSON.parse(text);
        if (parsed && typeof parsed === "object" && Array.isArray(parsed.nodes)) {
          event.preventDefault();
          applyWorkflowData(parsed, renderCallback);
          showToast(`已通过快捷键快速导入工作流（${parsed.nodes.length} 个节点）`, "success");
        } else if (parsed && typeof parsed === "object") {
          showToast("剪贴板内容不是有效的工作流 JSON（缺少 nodes 列表）", "warning");
        }
      } catch (err) {
        showToast("剪贴板 JSON 格式解析失败", "warning");
      }
    }
  });
}

export async function tryAutoConnect(renderCallback) {
  await performConnect(renderCallback, true);
}
