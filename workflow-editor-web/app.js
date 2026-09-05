const $ = (id) => document.getElementById(id);
const fileInput = $("file");

const EditorWeb = {
  workflowFormatVersion: 1,
  defaultNodePosition: { x: 360, y: 160 },
  nodeWidth: 190,
  portVerticalOffset: 45,
  edgeControlOffset: 60,
  maximumEventCharacters: 6000,
  httpNotFound: 404,
  httpConflict: 409,
  api: {
    manifest: "/api/v1/manifest",
    workflows: "/api/v1/workflows",
    validateWorkflow: "/api/v1/workflows/validate",
    events: "/api/v1/events",
    authorizationHeader: "Authorization",
    bearerPrefix: "Bearer ",
    eventAccessTokenQuery: "access_token",
  },
  eventType: {
    entryChanged: "editor.entry.changed",
    globalErrorChanged: "editor.global_error.changed",
    connectionRejected: "editor.connection.rejected",
    bridgeConnected: "bridge.connected",
    bridgeMessage: "bridge.message",
    bridgeError: "bridge.error",
    workflowSaved: "workflow.saved",
  },
  categoryLabel: {
    flow: "流程控制",
    control: "条件与分支",
    loop: "循环",
    data: "数据处理",
    date: "日期时间",
    html: "HTML 解析",
    http: "网络请求",
    storage: "存储",
    object: "对象处理",
    json: "JSON",
    codec: "编解码",
    crypto: "加密",
    text: "文本处理",
    array: "数组处理",
    math: "数学计算",
    action: "系统操作",
    url: "URL 处理",
    csv: "CSV",
    xml: "XML / RSS",
    bilibili: "哔哩哔哩",
    other: "其他节点",
  },
};

let manifest = { nodeTypes: [] };
let workflow = newWorkflow();
let selectedNodeId = null;
let dragging = null;
let linking = null;
let pendingLink = null;
let revision = 0;
let eventSocket = null;
const expandedPaletteCategories = new Set();

function newWorkflow() {
  return {
    formatVersion: EditorWeb.workflowFormatVersion,
    id: crypto.randomUUID(),
    name: "未命名工作流",
    entryNodeId: "",
    nodes: [],
    edges: [],
  };
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function nodeById(id) {
  return workflow.nodes.find((node) => node.id === id);
}
function specForNode(node) {
  return manifest.nodeTypes.find((spec) => spec.type === node?.type);
}
function fieldConditionMatches(node, condition) {
  return (
    !condition ||
    JSON.stringify(node.config[condition.key]) ===
      JSON.stringify(condition.equals)
  );
}

function compatibleInputPort(spec, source) {
  const sourceSpec = specForNode(nodeById(source.nodeId));
  const output = sourceSpec?.outputPorts.find(
    (port) => port.id === source.portId,
  );
  return spec.inputPorts.find(
    (port) => port.kind === output?.kind && port.maxConnections > 0,
  );
}

function matchingPaletteSpecs() {
  const query = $("search").value.trim().toLowerCase();
  return manifest.nodeTypes.filter((spec) => {
    const matchesQuery =
      !query ||
      spec.type.toLowerCase().includes(query) ||
      spec.editor.title.toLowerCase().includes(query) ||
      (spec.editor.description ?? "").toLowerCase().includes(query) ||
      categoryLabel(spec.category).toLowerCase().includes(query);
    return (
      spec.editor.creatable !== false &&
      matchesQuery &&
      (!pendingLink || compatibleInputPort(spec, pendingLink))
    );
  });
}

function categoryLabel(category) {
  return EditorWeb.categoryLabel[category] ?? category;
}

function groupedPaletteSpecs() {
  const groups = new Map();
  matchingPaletteSpecs().forEach((spec) => {
    const category = spec.category || "other";
    const specs = groups.get(category) ?? [];
    specs.push(spec);
    groups.set(category, specs);
  });
  return [...groups.entries()]
    .map(([category, specs]) => ({
      category,
      specs: specs.sort((left, right) =>
        left.editor.title.localeCompare(right.editor.title, "zh-CN"),
      ),
    }))
    .sort((left, right) =>
      categoryLabel(left.category).localeCompare(categoryLabel(right.category), "zh-CN"),
    );
}

function render() {
  renderPalette();
  renderNodes();
  drawEdges();
  renderInspector();
}

function renderPalette() {
  $("palette-hint").textContent = pendingLink
    ? "选择一个兼容节点以完成连线"
    : "";
  const isSearching = Boolean($("search").value.trim());
  const groups = groupedPaletteSpecs();
  $("palette").innerHTML = groups
    .map(({ category, specs }) => {
      const isExpanded = isSearching || expandedPaletteCategories.has(category);
      const items = specs
        .map(
          (spec) => `
            <button class="palette-item" data-type="${escapeHtml(spec.type)}">
              <span class="palette-item-title">${escapeHtml(spec.editor.title)}</span>
              <span class="palette-item-description">${escapeHtml(spec.editor.description || spec.type)}</span>
            </button>
          `,
        )
        .join("");
      return `
        <section class="palette-group">
          <button
            aria-expanded="${isExpanded}"
            class="palette-group-toggle"
            data-category="${escapeHtml(category)}"
          >
            <span>${isExpanded ? "▾" : "▸"} ${escapeHtml(categoryLabel(category))}</span>
            <span class="palette-group-count">${specs.length}</span>
          </button>
          <div class="palette-group-items ${isExpanded ? "expanded" : ""}">
            ${items}
          </div>
        </section>
      `;
    })
    .join("");
}

function renderNodes() {
  $("nodes").innerHTML = workflow.nodes
    .map((node) => {
      const spec = specForNode(node);
      const inputPorts = spec?.inputPorts ?? [];
      const outputPorts = spec?.outputPorts ?? [];
      const badges = [
        workflow.entryNodeId === node.id ? "入口" : "",
        workflow.globalErrorNodeId === node.id ? "错误" : "",
      ]
        .filter(Boolean)
        .join(" · ");
      const ports = `
        <div class="ports">
          <span>${renderPorts(inputPorts, "in")}</span>
          <span>${renderPorts(outputPorts, "out")}</span>
        </div>
      `;
      const selectionClass = selectedNodeId === node.id ? "selected" : "";
      const title = escapeHtml(node.label || spec?.editor.title || node.type);
      return `
        <article
          class="node ${selectionClass}"
          data-id="${escapeHtml(node.id)}"
          style="left:${node.layout.x}px;top:${node.layout.y}px"
        >
          <header>${title}<span class="node-badges">${escapeHtml(badges)}</span></header>
          ${ports}
        </article>
      `;
    })
    .join("");
}

function renderPorts(ports, direction) {
  return ports
    .map((port) => {
      const arrow = direction === "in" ? "◀" : "▶";
      const label =
        direction === "in"
          ? `${arrow} ${port.label}`
          : `${port.label} ${arrow}`;
      return `<span class="port ${direction}" data-port="${escapeHtml(port.id)}">${escapeHtml(label)}</span>`;
    })
    .join("<br>");
}

function drawEdges() {
  $("edges").innerHTML = workflow.edges
    .map((edge) => {
      const source = nodeById(edge.source.nodeId);
      const target = nodeById(edge.target.nodeId);
      if (!source || !target) return "";
      const x1 = source.layout.x + EditorWeb.nodeWidth;
      const y1 = source.layout.y + EditorWeb.portVerticalOffset;
      const x2 = target.layout.x;
      const y2 = target.layout.y + EditorWeb.portVerticalOffset;
      const controlOffset = EditorWeb.edgeControlOffset;
      const path = [
        `M${x1},${y1}`,
        `C${x1 + controlOffset},${y1}`,
        `${x2 - controlOffset},${y2}`,
        `${x2},${y2}`,
      ].join(" ");
      return `<path class="edge" d="${path}"/>`;
    })
    .join("");
}

function fieldConstraintAttributes(validation = {}) {
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

function renderInspector() {
  const node = nodeById(selectedNodeId);
  const spec = specForNode(node);
  $("title").textContent = node ? node.label || spec.editor.title : "配置";
  $("set-entry").disabled = !node;
  $("set-error").disabled = !node;
  if (!node || !spec) {
    $("inspector").textContent = "选择一个节点";
    return;
  }
  const fields = spec.editor.fields
    .filter(
      (field) =>
        !field.editorHidden && fieldConditionMatches(node, field.visibleWhen),
    )
    .sort((left, right) => left.order - right.order);
  $("inspector").innerHTML =
    fields.map((field) => renderField(node, field)).join("") ||
    "此节点无配置项";
}

function renderField(node, field) {
  const value = node.config[field.key] ?? "";
  const disabled =
    field.readOnly || !fieldConditionMatches(node, field.enabledWhen)
      ? "disabled"
      : "";
  const constraints = fieldConstraintAttributes(field.validation);
  const required = field.required ? "required" : "";
  const placeholder = field.placeholder
    ? `placeholder="${escapeHtml(field.placeholder)}"`
    : "";
  const key = escapeHtml(field.key);
  const label = escapeHtml(field.label);
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

function createNode(spec, layout = EditorWeb.defaultNodePosition) {
  const id = crypto.randomUUID();
  const fieldDefaults = Object.fromEntries(
    spec.editor.fields
      .filter(
        (field) =>
          field.defaultValue !== undefined && field.defaultValue !== null,
      )
      .map((field) => [field.key, field.defaultValue]),
  );
  workflow.nodes.push({
    id,
    type: spec.type,
    nodeVersion: spec.latestVersion,
    label: spec.editor.title,
    config: {
      ...fieldDefaults,
      ...structuredClone(spec.editor.defaultConfig ?? {}),
    },
    layout: { ...layout },
  });
  selectedNodeId = id;
  return nodeById(id);
}

function addNodeFromPalette(type) {
  const spec = manifest.nodeTypes.find((item) => item.type === type);
  if (!spec) return;
  const node = createNode(spec);
  if (pendingLink) {
    const input = compatibleInputPort(spec, pendingLink);
    if (
      input &&
      canConnect(pendingLink, { nodeId: node.id, portId: input.id })
    ) {
      workflow.edges.push({
        id: crypto.randomUUID(),
        source: pendingLink,
        target: { nodeId: node.id, portId: input.id },
        kind: input.kind,
      });
    }
    pendingLink = null;
  }
  render();
}

function canConnect(source, target) {
  const sourceSpec = specForNode(nodeById(source.nodeId));
  const targetSpec = specForNode(nodeById(target.nodeId));
  const output = sourceSpec?.outputPorts.find(
    (port) => port.id === source.portId,
  );
  const input = targetSpec?.inputPorts.find(
    (port) => port.id === target.portId,
  );
  if (
    !output ||
    !input ||
    source.nodeId === target.nodeId ||
    output.kind !== input.kind
  )
    return false;
  const outputCount = workflow.edges.filter(
    (edge) =>
      edge.source.nodeId === source.nodeId &&
      edge.source.portId === source.portId,
  ).length;
  const inputCount = workflow.edges.filter(
    (edge) =>
      edge.target.nodeId === target.nodeId &&
      edge.target.portId === target.portId,
  ).length;
  return (
    outputCount < output.maxConnections && inputCount < input.maxConnections
  );
}

document.addEventListener("click", (event) => {
  const categoryButton = event.target.closest("[data-category]");
  if (categoryButton) {
    const category = categoryButton.dataset.category;
    if (expandedPaletteCategories.has(category)) {
      expandedPaletteCategories.delete(category);
    } else {
      expandedPaletteCategories.add(category);
    }
    renderPalette();
    return;
  }
  const paletteButton = event.target.closest("[data-type]");
  if (paletteButton) {
    addNodeFromPalette(paletteButton.dataset.type);
    return;
  }
  const nodeElement = event.target.closest(".node");
  if (nodeElement && !event.target.closest(".port")) {
    selectedNodeId = nodeElement.dataset.id;
    render();
  }
});
$("search").addEventListener("input", renderPalette);
$("inspector").addEventListener("input", (event) => {
  const input = event.target;
  if (!input.dataset.key) return;
  const node = nodeById(selectedNodeId);
  let value = input.type === "checkbox" ? input.checked : input.value;
  try {
    if (input.dataset.kind === "number") value = Number(value);
    if (input.dataset.kind === "json") value = JSON.parse(value);
  } catch {
    return;
  }
  node.config[input.dataset.key] = value;
  renderInspector();
});
$("set-entry").onclick = () => {
  if (selectedNodeId) {
    workflow.entryNodeId = selectedNodeId;
    showEvent({
      type: EditorWeb.eventType.entryChanged,
      nodeId: selectedNodeId,
    });
    render();
  }
};
$("set-error").onclick = () => {
  if (selectedNodeId) {
    workflow.globalErrorNodeId =
      workflow.globalErrorNodeId === selectedNodeId ? null : selectedNodeId;
    showEvent({
      type: EditorWeb.eventType.globalErrorChanged,
      nodeId: workflow.globalErrorNodeId,
    });
    render();
  }
};

$("nodes").addEventListener("pointerdown", (event) => {
  const output = event.target.closest(".out");
  if (output) {
    event.preventDefault();
    linking = {
      nodeId: output.closest(".node").dataset.id,
      portId: output.dataset.port,
    };
    return;
  }
  if (event.target.closest(".port")) return;
  const element = event.target.closest(".node");
  if (!element) return;
  const node = nodeById(element.dataset.id);
  dragging = {
    id: node.id,
    x: event.clientX - node.layout.x,
    y: event.clientY - node.layout.y,
  };
  element.setPointerCapture(event.pointerId);
});
$("nodes").addEventListener("pointermove", (event) => {
  if (!dragging) return;
  const node = nodeById(dragging.id);
  node.layout.x = Math.max(0, event.clientX - dragging.x);
  node.layout.y = Math.max(0, event.clientY - dragging.y);
  renderNodes();
  drawEdges();
});
document.addEventListener("pointerup", (event) => {
  if (linking) {
    const input = document
      .elementFromPoint(event.clientX, event.clientY)
      ?.closest(".in");
    if (input) {
      const target = {
        nodeId: input.closest(".node").dataset.id,
        portId: input.dataset.port,
      };
      if (canConnect(linking, target)) {
        const kind = specForNode(nodeById(linking.nodeId)).outputPorts.find(
          (port) => port.id === linking.portId,
        ).kind;
        workflow.edges.push({
          id: crypto.randomUUID(),
          source: linking,
          target,
          kind,
        });
      } else
        showEvent({
          type: EditorWeb.eventType.connectionRejected,
          message: "端口方向、类型或连接数量不兼容",
        });
    } else pendingLink = linking;
    linking = null;
    render();
  }
  dragging = null;
});
document.addEventListener("keydown", (event) => {
  const editing = /INPUT|TEXTAREA|SELECT/.test(
    document.activeElement?.tagName ?? "",
  );
  if (
    (event.key !== "Delete" && event.key !== "Backspace") ||
    !selectedNodeId ||
    editing
  )
    return;
  const id = selectedNodeId;
  workflow.nodes = workflow.nodes.filter((node) => node.id !== id);
  workflow.edges = workflow.edges.filter(
    (edge) => edge.source.nodeId !== id && edge.target.nodeId !== id,
  );
  if (workflow.entryNodeId === id) workflow.entryNodeId = "";
  if (workflow.globalErrorNodeId === id) workflow.globalErrorNodeId = null;
  selectedNodeId = null;
  render();
});

function inspectorIsValid() {
  const invalid = [
    ...$("inspector").querySelectorAll("input, textarea, select"),
  ].find((input) => !input.disabled && !input.checkValidity());
  if (!invalid) return true;
  invalid.focus();
  const fieldLabel = invalid.closest("label")?.textContent?.trim();
  alert(
    `配置“${fieldLabel ?? invalid.dataset.key}”不符合字段约束：${invalid.validationMessage}`,
  );
  return false;
}
function baseUrl() {
  const configured = $("device").value.replace(/\/$/, "");
  if (configured) return configured;
  return window.location.protocol === "file:" ? "" : window.location.origin;
}
function initializeDeviceAddress() {
  if (window.location.protocol !== "file:") {
    $("device").value = window.location.origin;
  }
}
function authHeaders() {
  const token = $("token").value;
  return token
    ? {
        [EditorWeb.api.authorizationHeader]:
          `${EditorWeb.api.bearerPrefix}${token}`,
      }
    : {};
}
function showEvent(value) {
  $("events").textContent =
    `${JSON.stringify(value)}\n${$("events").textContent}`.slice(
      0,
      EditorWeb.maximumEventCharacters,
    );
}
function connectEvents() {
  eventSocket?.close();
  const base = baseUrl();
  if (!base) return;
  const token = $("token").value;
  const eventQuery = token
    ? `?${EditorWeb.api.eventAccessTokenQuery}=${encodeURIComponent(token)}`
    : "";
  const eventUrl = `${base.replace(/^http/, "ws")}${EditorWeb.api.events}${eventQuery}`;
  eventSocket = new WebSocket(eventUrl);
  eventSocket.onopen = () =>
    showEvent({ type: EditorWeb.eventType.bridgeConnected });
  eventSocket.onmessage = (event) => {
    try {
      showEvent(JSON.parse(event.data));
    } catch {
      showEvent({
        type: EditorWeb.eventType.bridgeMessage,
        message: event.data,
      });
    }
  };
  eventSocket.onerror = () =>
    showEvent({
      type: EditorWeb.eventType.bridgeError,
      message: "事件连接失败",
    });
}

$("connect").onclick = async () => {
  if (!baseUrl()) {
    alert("请填写设备地址，例如 http://127.0.0.1:8080");
    return;
  }
  try {
    const response = await fetch(`${baseUrl()}${EditorWeb.api.manifest}`, {
      headers: authHeaders(),
    });
    if (!response.ok) throw Error(response.statusText);
    const payload = await response.json();
    manifest = payload.manifest ?? payload;
    connectEvents();
    render();
  } catch (error) {
    alert(`无法连接设备：${error.message}`);
  }
};
initializeDeviceAddress();
$("load").onclick = async () => {
  try {
    const response = await fetch(
      `${baseUrl()}${EditorWeb.api.workflows}/${encodeURIComponent(workflow.id)}`,
      { headers: authHeaders() },
    );
    if (response.status === EditorWeb.httpNotFound)
      return alert("设备上尚未保存此工作流");
    if (!response.ok) throw Error(response.statusText);
    const document = await response.json();
    workflow = document.workflow;
    revision = document.revision;
    selectedNodeId = null;
    render();
  } catch (error) {
    alert(`读取失败：${error.message}`);
  }
};
$("save").onclick = async () => {
  if (!inspectorIsValid()) return;
  try {
    const response = await fetch(
      `${baseUrl()}${EditorWeb.api.workflows}/${encodeURIComponent(workflow.id)}`,
      {
        method: "PUT",
        headers: { ...authHeaders(), "content-type": "application/json" },
        body: JSON.stringify({ baseRevision: revision, workflow }),
      },
    );
    if (response.status === EditorWeb.httpConflict) {
      const current = await response.json();
      return alert(
        `版本冲突，设备当前版本：${current.current?.revision ?? "未知"}`,
      );
    }
    if (!response.ok)
      throw Error((await response.text()) || response.statusText);
    const document = await response.json();
    workflow = document.workflow;
    revision = document.revision;
    showEvent({ type: EditorWeb.eventType.workflowSaved, revision });
    render();
  } catch (error) {
    alert(`保存失败：${error.message}`);
  }
};
$("validate").onclick = async () => {
  if (!inspectorIsValid()) return;
  try {
    const response = await fetch(
      `${baseUrl()}${EditorWeb.api.validateWorkflow}`,
      {
        method: "POST",
        headers: { ...authHeaders(), "content-type": "application/json" },
        body: JSON.stringify({ workflow }),
      },
    );
    const payload = await response.json();
    alert(
      payload.validation.isValid
        ? "设备校验通过"
        : payload.validation.issues.map((issue) => issue.message).join("\n"),
    );
  } catch (error) {
    alert(`校验失败：${error.message}`);
  }
};
$("run").onclick = async () => {
  try {
    const response = await fetch(
      `${baseUrl()}${EditorWeb.api.workflows}/${encodeURIComponent(workflow.id)}/runs`,
      {
        method: "POST",
        headers: { ...authHeaders(), "content-type": "application/json" },
        body: JSON.stringify({ revision }),
      },
    );
    if (!response.ok)
      throw Error((await response.text()) || response.statusText);
    showEvent(await response.json());
  } catch (error) {
    alert(`运行失败：${error.message}`);
  }
};
$("manifest").onclick = () => {
  fileInput.dataset.kind = "manifest";
  fileInput.click();
};
$("workflow").onclick = () => {
  fileInput.dataset.kind = "workflow";
  fileInput.click();
};
fileInput.onchange = async () => {
  const parsed = JSON.parse(await fileInput.files[0].text());
  if (fileInput.dataset.kind === "manifest") manifest = parsed;
  else {
    workflow = parsed;
    revision = 0;
    selectedNodeId = null;
  }
  render();
};
$("export").onclick = () => {
  const anchor = document.createElement("a");
  anchor.href = URL.createObjectURL(
    new Blob([JSON.stringify(workflow, null, 2)], { type: "application/json" }),
  );
  anchor.download = "workflow.json";
  anchor.click();
  URL.revokeObjectURL(anchor.href);
};

render();
