import { state, specForNode } from "../state.js";
import { $, escapeHtml } from "../utils.js";

export function renderPorts(ports, direction) {
  if (!ports || ports.length === 0) return "";
  return `
    <div class="ports-column ${direction}">
      ${ports
        .map((port) => {
          const handle = `<span class="port-handle"></span>`;
          const portName = port.label || port.id || (direction === "in" ? "输入" : "输出");
          const kindTip = port.kind && port.kind !== "control" ? ` · 类型: ${port.kind}` : "";
          const fullTitle = `${portName}${kindTip}`;
          const content =
            direction === "in"
              ? `${handle}<span>${escapeHtml(portName)}</span>`
              : `<span>${escapeHtml(portName)}</span>${handle}`;
          return `
            <div
              class="port ${direction}"
              data-port="${escapeHtml(port.id)}"
              data-dir="${direction}"
              title="${escapeHtml(fullTitle)}"
            >
              ${content}
            </div>
          `;
        })
        .join("")}
    </div>
  `;
}

export function renderNodes() {
  const nodesContainer = $("nodes");
  if (!nodesContainer) return;

  nodesContainer.innerHTML = state.workflow.nodes
    .map((node) => {
      const spec = specForNode(node);
      const inputPorts = spec?.inputPorts ?? [];
      const outputPorts = spec?.outputPorts ?? [];

      const isStart = node.type === "flow.start" || state.workflow.entryNodeId === node.id;
      const isEnd = node.type === "flow.end";
      const isErrorNode = state.workflow.globalErrorNodeId === node.id;
      const isConfigError = state.errorNodeIds.has(node.id);

      const badgesHtml = [
        isStart ? `<span class="node-badge-tag node-badge-entry" title="流程入口节点">入口</span>` : "",
        isEnd ? `<span class="node-badge-tag node-badge-exit" title="流程出口节点">出口</span>` : "",
        isErrorNode ? `<span class="node-badge-tag node-badge-error" title="全局错误节点">全局错误</span>` : "",
        isConfigError ? `<span class="node-badge-tag node-badge-warn" title="配置异常">异常</span>` : "",
      ]
        .filter(Boolean)
        .join("");

      const isSelected = state.selectedNodeId === node.id;
      const isError = isConfigError;
      const nodeClasses = ["node"];
      if (isStart) nodeClasses.push("node-entry");
      if (isEnd) nodeClasses.push("node-exit");
      if (isSelected) nodeClasses.push("selected");
      if (isError) nodeClasses.push("node-error");

      const baseTitle = node.label || spec?.editor?.title || node.type;
      const rawType = node.type || "";
      const descText =
        node.description ||
        spec?.editor?.description ||
        (isStart ? "工作流的唯一入口节点。" : isEnd ? "结束当前工作流执行。" : "");

      if (!node.layout || typeof node.layout.x !== "number" || typeof node.layout.y !== "number") {
        node.layout = { x: 80, y: 80 };
      }

      return `
        <article
          id="node-${escapeHtml(node.id)}"
          class="${nodeClasses.join(" ")}"
          data-id="${escapeHtml(node.id)}"
          style="left:${node.layout.x}px;top:${node.layout.y}px"
        >
          <header class="node-header">
            <div class="node-header-row node-title-row">
              <span class="node-title" title="${escapeHtml(baseTitle)}">${escapeHtml(baseTitle)}</span>
              ${badgesHtml ? `<div class="node-badges">${badgesHtml}</div>` : ""}
            </div>
            ${rawType ? `
              <div class="node-header-row node-id-row">
                <span class="node-type-tag" title="${escapeHtml(rawType)}">${escapeHtml(rawType)}</span>
              </div>
            ` : ""}
            ${descText ? `
              <div class="node-header-row node-desc-row">
                <span class="node-desc-text" title="${escapeHtml(descText)}">${escapeHtml(descText)}</span>
              </div>
            ` : ""}
          </header>
          <div class="ports">
            ${renderPorts(inputPorts, "in")}
            ${renderPorts(outputPorts, "out")}
          </div>
        </article>
      `;
    })
    .join("");
}
