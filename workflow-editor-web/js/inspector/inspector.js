import { EditorConfig } from "../config.js";
import { state, nodeById, specForNode, getAllCapabilities, autoSyncRequiredCapabilities, saveDraft } from "../state.js";
import { $, escapeHtml, generateUUID } from "../utils.js";
import { showToast } from "../ui/toast.js";
import { showEvent } from "../console/console.js";
import { renderNodes } from "../canvas/nodes.js";
import { drawEdges } from "../canvas/edges.js";
import { deleteNode } from "../canvas/interaction.js";

export function fieldConditionMatches(node, condition) {
  return (
    !condition ||
    JSON.stringify(node.config[condition.key]) ===
      JSON.stringify(condition.equals)
  );
}

export function fieldConstraintAttributes(validation = {}) {
  const values = {
    min: validation.minimum,
    max: validation.maximum,
    pattern: validation.pattern,
    minlength: validation.minLength,
    maxlength: validation.maxLength,
  };
  return Object.entries(values)
    .filter(([, value]) => value !== undefined && value !== null)
    .map(([name, value]) => `${name}="${escapeHtml(value)}"`)
    .join(" ");
}

export function renderField(node, field) {
  const spec = specForNode(node);
  const isRequired = field.required || Boolean(
    spec?.requiredConfigKeys &&
    (Array.isArray(spec.requiredConfigKeys)
      ? spec.requiredConfigKeys.includes(field.key)
      : spec.requiredConfigKeys.has?.(field.key))
  );
  const value = node.config[field.key] ?? "";
  const disabled =
    field.readOnly || !fieldConditionMatches(node, field.enabledWhen)
      ? "disabled"
      : "";
  const constraints = fieldConstraintAttributes(field.validation);
  const required = isRequired ? "required" : "";
  const requiredStar = isRequired ? '<span class="required-star" title="必填项">*</span>' : "";
  const placeholder = field.placeholder
    ? `placeholder="${escapeHtml(field.placeholder)}"`
    : "";
  const key = escapeHtml(field.key);
  const label = `<span class="field-label">${escapeHtml(field.label)}${requiredStar}</span>`;

  if (field.kind === "boolean") {
    return `
      <label>
        ${label}
        <input type="checkbox" data-key="${key}" data-kind="boolean" ${value ? "checked" : ""} ${disabled}>
      </label>
    `;
  }
  if (["json", "string-list", "key-value-list"].includes(field.kind)) {
    const jsonValue = escapeHtml(JSON.stringify(value, null, 2));
    return `
      <label>
        ${label}
        <textarea
          data-key="${key}"
          data-kind="json"
          ${disabled} ${required} ${constraints} ${placeholder}
        >${jsonValue}</textarea>
      </label>
    `;
  }
  if (field.kind === "textarea") {
    return `
      <label>
        ${label}
        <textarea
          data-key="${key}"
          data-kind="text"
          ${disabled} ${required} ${constraints} ${placeholder}
        >${escapeHtml(value)}</textarea>
      </label>
    `;
  }
  if (field.kind === "select") {
    const options = (field.options ?? [])
      .map((option) => {
        const optionValue =
          typeof option.value === "string"
            ? option.value
            : JSON.stringify(option.value);
        const isSelected =
          JSON.stringify(option.value) === JSON.stringify(value)
            ? "selected"
            : "";
        return `<option value="${escapeHtml(optionValue)}" ${isSelected}>${escapeHtml(option.label)}</option>`;
      })
      .join("");
    return `
      <label>
        ${label}
        <select data-key="${key}" data-kind="select" ${disabled}>${options}</select>
      </label>
    `;
  }
  if (field.key === "loopId") {
    const loopTypes = ["loop.repeat", "loop.for_each", "loop.forEach", "loop.while"];
    const loopNodes = (state.workflow.nodes || []).filter((n) => loopTypes.includes(n.type));
    const loopOptions = loopNodes
      .map((n) => `<option value="${escapeHtml(n.id)}">${escapeHtml(n.label || n.type)} (${escapeHtml(n.id)})</option>`)
      .join("");
    return `
      <label>
        ${label}
        <input
          type="text"
          data-key="${key}"
          data-kind="${escapeHtml(field.kind)}"
          value="${escapeHtml(value)}"
          list="loop-nodes-datalist"
          ${disabled} ${required} ${constraints} ${placeholder}
        >
        <datalist id="loop-nodes-datalist">
          ${loopOptions}
        </datalist>
        <span class="hint" style="font-size: 11px; margin-top: 4px; display: block; opacity: 0.75;">💡 留空时画布将自动反向推导所属的上游循环节点并绘制回环虚线</span>
      </label>
    `;
  }

  const type =
    field.kind === "number"
      ? "number"
      : field.kind === "secret-reference"
        ? "password"
        : "text";
  return `
    <label>
      ${label}
      <input
        type="${type}"
        data-key="${key}"
        data-kind="${escapeHtml(field.kind)}"
        value="${escapeHtml(value)}"
        ${disabled} ${required} ${constraints} ${placeholder}
      >
    </label>
  `;
}

export function renderWorkflowSettings() {
  const titleEl = $("title");
  const nodeActions = $("node-actions");
  const inspectorEl = $("inspector");

  if (titleEl) titleEl.textContent = "工作流全局配置";
  if (nodeActions) nodeActions.style.display = "none";

  const allCapabilities = getAllCapabilities();
  const currentCaps = new Set(state.workflow.requiredCapabilities || []);

  const capsCheckboxes = allCapabilities
    .map((cap) => {
      const checked = currentCaps.has(cap.id) ? "checked" : "";
      const displayTitle = cap.label ? `${cap.label} (${cap.id})` : cap.id;
      const tooltip = cap.description ? `title="${escapeHtml(cap.description)}"` : "";
      return `
        <label class="capability-checkbox-label" ${tooltip}>
          <input type="checkbox" data-capability="${escapeHtml(cap.id)}" ${checked}>
          <span>${escapeHtml(displayTitle)}</span>
        </label>
      `;
    })
    .join("");

  const html = `
    <div class="workflow-settings-form">
      <label>
        工作流名称
        <input id="wf-name" type="text" value="${escapeHtml(state.workflow.name || "")}" placeholder="工作流名称">
      </label>
      <label>
        工作流描述
        <textarea id="wf-description" placeholder="工作流用途与说明">${escapeHtml(state.workflow.description || "")}</textarea>
      </label>
      <label class="capability-checkbox-label" style="margin: 8px 0 14px 0 !important;">
        <input id="wf-enabled" type="checkbox" ${state.workflow.enabled !== false ? "checked" : ""}>
        <span>启用此工作流 (Enabled)</span>
      </label>
      <fieldset class="capabilities-fieldset">
        <legend>权限声明 (Required Capabilities)</legend>
        <button id="wf-auto-caps" type="button" class="action-btn-small">⚡ 根据节点自动推导所需权限</button>
        <div class="capabilities-list">
          ${capsCheckboxes || "<p class='hint'>暂无可用权限</p>"}
        </div>
      </fieldset>
    </div>
  `;
  if (inspectorEl) inspectorEl.innerHTML = html;
}

export function generateUniqueEdgeId(preferredId, currentEdge, allEdges) {
  let baseId = (preferredId || "").trim();
  if (!baseId) {
    baseId = `edge_${generateUUID().slice(0, 8)}`;
  }

  const otherEdges = (allEdges || []).filter((e) => e !== currentEdge);
  const exists = (id) => otherEdges.some((e) => e.id === id);

  if (!exists(baseId)) {
    return { id: baseId, corrected: false };
  }

  let candidate = baseId;
  let counter = 1;
  while (exists(candidate)) {
    candidate = `${baseId}_${counter}`;
    counter++;
  }
  return { id: candidate, corrected: true };
}

export function renderEdgeInspector(edge) {
  const titleEl = $("title");
  const nodeActions = $("node-actions");
  const errorBtn = $("set-error");
  const deleteBtn = $("delete-node");
  const inspectorEl = $("inspector");

  if (titleEl) titleEl.textContent = `连线配置 (${edge.id})`;
  if (nodeActions) nodeActions.style.display = "flex";
  if (errorBtn) errorBtn.style.display = "none";
  if (deleteBtn) {
    deleteBtn.disabled = false;
    deleteBtn.textContent = "删除此连线";
  }

  const sourceNode = nodeById(edge.source?.nodeId);
  const sourceSpec = specForNode(sourceNode);
  const sourcePort = sourceSpec?.outputPorts?.find((p) => p.id === edge.source?.portId);
  const sourceName = sourceNode?.label || sourceSpec?.editor?.title || edge.source?.nodeId || "未知节点";
  const sourcePortLabel = sourcePort?.label || edge.source?.portId || "未知出口";
  const sourceText = `${sourceName} [${sourcePortLabel}]`;

  const targetNode = nodeById(edge.target?.nodeId);
  const targetSpec = specForNode(targetNode);
  const targetPort = targetSpec?.inputPorts?.find((p) => p.id === edge.target?.portId);
  const targetName = targetNode?.label || targetSpec?.editor?.title || edge.target?.nodeId || "未知节点";
  const targetPortLabel = targetPort?.label || edge.target?.portId || "未知入口";
  const targetText = `${targetName} [${targetPortLabel}]`;

  const html = `
    <div class="edge-inspector-form">
      <button id="switch-to-wf-settings" type="button" class="action-btn-small" style="margin-bottom: 12px; background: #334863; border-color: #476288;">⚙ 切换到工作流全局配置与权限</button>

      <div class="edge-base-props" style="margin-bottom: 14px; padding-bottom: 12px; border-bottom: 1px solid #28374d;">
        <label>
          <span class="field-label">连线唯一标识 (Edge ID) <span class="required-star">*</span></span>
          <div style="display: flex; gap: 6px; align-items: center; width: 100%; margin-top: 4px;">
            <input
              id="edge-prop-id"
              type="text"
              value="${escapeHtml(edge.id)}"
              placeholder="请输入连线唯一ID"
              spellcheck="false"
              style="flex: 1; min-width: 0; font-family: ui-monospace, SFMono-Regular, monospace; font-size: 12px;"
            >
            <button id="save-edge-id" type="button" class="btn-primary" style="padding: 7px 14px; font-size: 12px; font-weight: 600; white-space: nowrap; height: 35px; border-radius: 6px; cursor: pointer;">保存</button>
          </div>
          <span class="hint" style="font-size: 11px; margin-top: 4px; display: block; opacity: 0.75;">💡 点击保存或按回车键校验唯一性，重复时自动纠正并提示</span>
        </label>
      </div>

      <div class="edge-detail-card" style="background: rgba(15, 23, 42, 0.45); border: 1px solid #24354a; border-radius: 6px; padding: 10px 12px; font-size: 12px; display: flex; flex-direction: column; gap: 8px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="opacity: 0.7;">连线类型</span>
          <span style="color: #60a5fa; font-weight: 600;">${escapeHtml(edge.kind || "control")}</span>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed rgba(255,255,255,0.08); padding-top: 6px;">
          <span style="opacity: 0.7;">源端 (Source)</span>
          <span style="color: #93c5fd; text-align: right; max-width: 60%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${escapeHtml(sourceText)}">${escapeHtml(sourceText)}</span>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed rgba(255,255,255,0.08); padding-top: 6px;">
          <span style="opacity: 0.7;">目标端 (Target)</span>
          <span style="color: #34d399; text-align: right; max-width: 60%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${escapeHtml(targetText)}">${escapeHtml(targetText)}</span>
        </div>
      </div>
    </div>
  `;

  if (inspectorEl) inspectorEl.innerHTML = html;
}

export function renderInspector() {
  const titleEl = $("title");
  const nodeActions = $("node-actions");
  const errorBtn = $("set-error");
  const deleteBtn = $("delete-node");
  const inspectorEl = $("inspector");

  if (state.selectedEdgeId) {
    const edge = (state.workflow.edges || []).find((e) => e.id === state.selectedEdgeId);
    if (edge) {
      renderEdgeInspector(edge);
      return;
    }
    state.selectedEdgeId = null;
  }

  const node = nodeById(state.selectedNodeId);
  if (!node) {
    renderWorkflowSettings();
    return;
  }
  const spec = specForNode(node);
  if (titleEl) titleEl.textContent = `${node.label || spec?.editor?.title || node.type} (${node.type})`;
  if (nodeActions) nodeActions.style.display = "flex";
  if (errorBtn) errorBtn.style.display = "";

  const isGlobalError = state.workflow.globalErrorNodeId === node.id;

  if (errorBtn) {
    errorBtn.disabled = false;
    errorBtn.textContent = isGlobalError ? "✓ 已设为全局错误节点 (点击取消)" : "设为全局错误节点";
    errorBtn.classList.toggle("btn-active", isGlobalError);
  }
  if (deleteBtn) {
    deleteBtn.disabled = false;
    deleteBtn.textContent = "删除此节点";
  }

  const basePropsHtml = `
    <div class="node-base-props" style="margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #28374d;">
      <label>
        节点标题
        <input id="node-prop-label" type="text" value="${escapeHtml(node.label || spec?.editor?.title || node.type)}" placeholder="节点标题">
      </label>
      <label style="margin-top: 8px;">
        节点描述
        <textarea id="node-prop-desc" placeholder="节点功能描述" style="min-height: 52px;">${escapeHtml(node.description || spec?.editor?.description || "")}</textarea>
      </label>
      <label style="margin-top: 8px; opacity: 0.85;">
        节点类型标识 (Type ID)
        <input type="text" value="${escapeHtml(node.type)}" readonly disabled style="font-family: ui-monospace, SFMono-Regular, monospace; font-size: 11.5px; background: rgba(0,0,0,0.25);">
      </label>
    </div>
  `;

  if (!spec) {
    if (inspectorEl) {
      inspectorEl.innerHTML =
        `<button id="switch-to-wf-settings" type="button" class="action-btn-small" style="margin-bottom: 10px; background: #334863; border-color: #476288;">⚙ 切换到工作流全局配置与权限</button>` +
        basePropsHtml +
        "<p class='hint'>此节点暂无参数配置定义</p>";
    }
    return;
  }

  const fields = spec.editor.fields
    .filter(
      (field) =>
        !field.editorHidden && fieldConditionMatches(node, field.visibleWhen),
    )
    .sort((left, right) => left.order - right.order);

  const requiredCaps = spec.requiredCapabilities || [];
  const capsBadge =
    requiredCaps.length > 0
      ? `<div class="node-required-caps"><strong>节点所需权限：</strong> ${requiredCaps.map((c) => `<code>${escapeHtml(c)}</code>`).join(" ")}</div>`
      : "";

  if (inspectorEl) {
    inspectorEl.innerHTML =
      `<button id="switch-to-wf-settings" type="button" class="action-btn-small" style="margin-bottom: 10px; background: #334863; border-color: #476288;">⚙ 切换到工作流全局配置与权限</button>` +
      basePropsHtml +
      capsBadge +
      (fields.map((field) => renderField(node, field)).join("") ||
        "<p class='hint'>此节点无其他高级参数配置</p>");
  }
}

export function inspectorIsValid() {
  const inspectorEl = $("inspector");
  if (!inspectorEl) return true;
  const invalid = [
    ...inspectorEl.querySelectorAll("input, textarea, select"),
  ].find((input) => !input.disabled && !input.checkValidity());
  if (!invalid) return true;
  invalid.focus();
  const fieldLabel = invalid.closest("label")?.textContent?.replace(/\*/g, "").trim();
  if (state.selectedNodeId) {
    state.errorNodeIds.add(state.selectedNodeId);
    renderNodes();
  }
  showToast(
    `配置“${fieldLabel ?? invalid.dataset.key}”不符合字段约束：${invalid.validationMessage}`,
    "warning",
    4000,
  );
  return false;
}

export function initializeInspectorEvents(renderCallback) {
  const inspectorEl = $("inspector");
  if (!inspectorEl) return;

  inspectorEl.addEventListener("input", (event) => {
    const input = event.target;
    if (input.id === "wf-name") {
      state.workflow.name = input.value;
      saveDraft();
      return;
    }
    if (input.id === "wf-description") {
      state.workflow.description = input.value;
      saveDraft();
      return;
    }
    if (input.id === "node-prop-label") {
      const node = nodeById(state.selectedNodeId);
      if (node) {
        node.label = input.value;
        const titleEl = $("title");
        if (titleEl) titleEl.textContent = `${node.label || node.type} (${node.type})`;
        renderNodes();
        drawEdges();
        saveDraft();
      }
      return;
    }
    if (input.id === "node-prop-desc") {
      const node = nodeById(state.selectedNodeId);
      if (node) {
        node.description = input.value;
        renderNodes();
        saveDraft();
      }
      return;
    }

    const key = input.dataset.key;
    if (!key) return;
    const node = nodeById(state.selectedNodeId);
    if (!node) return;
    let value = input.type === "checkbox" ? input.checked : input.value;
    try {
      if (input.dataset.kind === "number" && value !== "") value = Number(value);
      if (input.dataset.kind === "json") {
        try {
          value = JSON.parse(value);
        } catch {
          node.config[key] = value;
          saveDraft();
          return;
        }
      }
    } catch {
      return;
    }
    node.config[key] = value;
    saveDraft();
    if (state.selectedNodeId && state.errorNodeIds.has(state.selectedNodeId)) {
      state.errorNodeIds.delete(state.selectedNodeId);
      renderNodes();
    }

    const spec = specForNode(node);
    if (spec) {
      const visibleFields = spec.editor.fields
        .filter((f) => !f.editorHidden && fieldConditionMatches(node, f.visibleWhen))
        .sort((a, b) => a.order - b.order);

      const currentDomKeys = [...inspectorEl.querySelectorAll("[data-key]")].map(
        (el) => el.dataset.key,
      );
      const newKeys = visibleFields.map((f) => f.key);
      const keysChanged =
        currentDomKeys.length !== newKeys.length ||
        currentDomKeys.some((k, i) => k !== newKeys[i]);

      if (keysChanged) {
        const activeKey = input.dataset.key;
        const selStart = typeof input.selectionStart === "number" ? input.selectionStart : null;
        const selEnd = typeof input.selectionEnd === "number" ? input.selectionEnd : null;

        renderInspector();

        const restoredInput = inspectorEl.querySelector(`[data-key="${activeKey}"]`);
        if (restoredInput) {
          restoredInput.focus();
          if (selStart !== null && selEnd !== null && typeof restoredInput.setSelectionRange === "function") {
            restoredInput.setSelectionRange(selStart, selEnd);
          }
        }
      } else {
        visibleFields.forEach((field) => {
          const fieldEl = inspectorEl.querySelector(`[data-key="${field.key}"]`);
          if (fieldEl) {
            fieldEl.disabled = field.readOnly || !fieldConditionMatches(node, field.enabledWhen);
          }
        });
      }
    }
  });

  function saveEdgeIdFromInput() {
    const input = $("edge-prop-id");
    if (!input) return;
    const edge = (state.workflow.edges || []).find((e) => e.id === state.selectedEdgeId);
    if (!edge) return;
    const rawVal = input.value.trim();
    if (rawVal === edge.id) {
      showToast("连线 ID 未发生变动", "info", 1500);
      return;
    }

    const result = generateUniqueEdgeId(rawVal, edge, state.workflow.edges || []);
    edge.id = result.id;
    state.selectedEdgeId = result.id;
    input.value = result.id;

    if (result.corrected) {
      if (!rawVal) {
        showToast(`连线 ID 不能为空，已自动重置为 “${result.id}”`, "warning", 3500);
      } else {
        showToast(`连线 ID “${rawVal}” 已存在，已自动纠正为 “${result.id}”`, "warning", 3500);
      }
    } else {
      showToast(`连线 ID 已修改为 “${result.id}”`, "success", 2000);
    }

    const titleEl = $("title");
    if (titleEl) titleEl.textContent = `连线配置 (${result.id})`;
    drawEdges();
    saveDraft(true);
  }

  inspectorEl.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && event.target.id === "edge-prop-id") {
      event.preventDefault();
      saveEdgeIdFromInput();
      event.target.blur();
    }
  });

  inspectorEl.addEventListener("change", (event) => {
    const target = event.target;
    if (target.id === "wf-enabled") {
      state.workflow.enabled = target.checked;
      saveDraft();
    } else if (target.dataset.capability) {
      const cap = target.dataset.capability;
      const caps = new Set(state.workflow.requiredCapabilities || []);
      if (target.checked) {
        caps.add(cap);
      } else {
        caps.delete(cap);
      }
      state.workflow.requiredCapabilities = [...caps].sort();
      saveDraft();
    }
  });

  inspectorEl.addEventListener("click", (event) => {
    if (event.target.id === "save-edge-id") {
      event.preventDefault();
      saveEdgeIdFromInput();
    } else if (event.target.id === "wf-auto-caps") {
      event.preventDefault();
      autoSyncRequiredCapabilities(() => {
        renderInspector();
        saveDraft();
      });
    } else if (event.target.id === "switch-to-wf-settings") {
      event.preventDefault();
      state.selectedNodeId = null;
      state.selectedEdgeId = null;
      if (renderCallback) renderCallback();
      else {
        renderInspector();
        saveDraft();
      }
    }
  });

  $("set-error")?.addEventListener("click", () => {
    if (state.selectedNodeId) {
      state.workflow.globalErrorNodeId =
        state.workflow.globalErrorNodeId === state.selectedNodeId ? null : state.selectedNodeId;
      showEvent({
        type: EditorConfig.eventType.globalErrorChanged,
        nodeId: state.workflow.globalErrorNodeId,
      });
      renderInspector();
      if (renderCallback) renderCallback();
      else saveDraft(true);
    }
  });

  $("delete-node")?.addEventListener("click", () => {
    if (state.selectedEdgeId) {
      const edgeId = state.selectedEdgeId;
      state.workflow.edges = (state.workflow.edges || []).filter((e) => e.id !== edgeId);
      state.selectedEdgeId = null;
      renderInspector();
      drawEdges();
      if (renderCallback) renderCallback();
      else saveDraft(true);
      showToast("已删除连线", "info", 1200);
      return;
    }
    if (state.selectedNodeId) {
      deleteNode(state.selectedNodeId, renderCallback);
    }
  });
}
