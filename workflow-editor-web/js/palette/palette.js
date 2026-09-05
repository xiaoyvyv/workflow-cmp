import { EditorConfig } from "../config.js";
import { state, nodeById, specForNode } from "../state.js";
import { $, escapeHtml, generateUUID } from "../utils.js";
import { canConnect } from "../canvas/edges.js";
import { showToast } from "../ui/toast.js";

export function compatibleInputPort(spec, source) {
  if (!source) return spec.inputPorts?.[0];
  const sourceSpec = specForNode(nodeById(source.nodeId));
  const output = sourceSpec?.outputPorts?.find(
    (port) => port.id === source.portId,
  );
  const targetKind = output?.kind || "control";
  return spec.inputPorts?.find(
    (port) => (port.kind || "control") === targetKind && (port.maxConnections === undefined || port.maxConnections > 0),
  );
}

export function categoryForSpec(spec) {
  if (spec?.type === "flow.start" || spec?.type === "flow.end") {
    return "flow_start_end";
  }
  if (spec?.category && String(spec.category).trim()) {
    return String(spec.category).trim();
  }
  if (spec?.editor?.category && String(spec.editor.category).trim()) {
    return String(spec.editor.category).trim();
  }
  if (spec?.type && spec.type.includes(".")) {
    const prefix = spec.type.split(".")[0].trim();
    if (prefix) return prefix;
  }
  return spec?.type?.trim() || "other";
}

export function categoryLabel(category, specsInGroup) {
  if (category === "flow_start_end") {
    return "流程起止";
  }
  const normCat = String(category || "").trim().toLowerCase();
  const meta = state.manifest.categories?.find?.(
    (item) => typeof item === "object" && (item.id?.toLowerCase() === normCat || item.name?.toLowerCase() === normCat),
  );
  if (meta?.label) return meta.label;
  if (meta?.title) return meta.title;
  if (meta?.name) return meta.name;

  if (normCat === "loop") return "循环控制";
  if (normCat === "flow") return "通用流程";

  if (category && category !== "other") {
    return category;
  }

  if (specsInGroup && specsInGroup.length > 0) {
    const firstType = specsInGroup[0].type || "";
    if (firstType.includes(".")) {
      const p = firstType.split(".")[0];
      if (p === "loop") return "循环控制";
      if (p === "flow") return "通用流程";
      return p;
    }
  }

  return category || "other";
}

export function matchingPaletteSpecs() {
  const searchEl = $("search");
  const query = searchEl ? searchEl.value.trim().toLowerCase() : "";
  return state.manifest.nodeTypes.filter((spec) => {
    const category = categoryForSpec(spec);
    const catLabel = categoryLabel(category);
    const matchesQuery =
      !query ||
      spec.type.toLowerCase().includes(query) ||
      (spec.editor?.title || "").toLowerCase().includes(query) ||
      (spec.editor?.description || "").toLowerCase().includes(query) ||
      catLabel.toLowerCase().includes(query) ||
      category.toLowerCase().includes(query);
    const isCreatable = spec.type === "flow.start" || spec.type === "flow.end" || spec.editor?.creatable !== false;
    return (
      isCreatable &&
      matchesQuery &&
      (!state.pendingLink || compatibleInputPort(spec, state.pendingLink))
    );
  });
}

export function groupedPaletteSpecs() {
  const groups = new Map();
  matchingPaletteSpecs().forEach((spec) => {
    const category = categoryForSpec(spec);
    const specs = groups.get(category) ?? [];
    specs.push(spec);
    groups.set(category, specs);
  });
  return [...groups.entries()]
    .map(([category, specs]) => ({
      category,
      label: categoryLabel(category, specs),
      specs: specs.sort((left, right) => {
        if (category === "flow_start_end") {
          if (left.type === "flow.start") return -1;
          if (right.type === "flow.start") return 1;
        }
        return (left.editor?.title || left.type).localeCompare(right.editor?.title || right.type, "zh-CN");
      }),
    }))
    .sort((left, right) => {
      if (left.category === "flow_start_end") return -1;
      if (right.category === "flow_start_end") return 1;
      return left.label.localeCompare(right.label, "zh-CN");
    });
}

// Track collapsed categories (so categories are expanded by default)
const collapsedCategories = new Set();

export function toggleCategory(category) {
  if (collapsedCategories.has(category)) {
    collapsedCategories.delete(category);
  } else {
    collapsedCategories.add(category);
  }
  renderPalette();
}

export function expandAllCategories() {
  collapsedCategories.clear();
  renderPalette();
}

export function collapseAllCategories() {
  const groups = groupedPaletteSpecs();
  groups.forEach((g) => collapsedCategories.add(g.category));
  renderPalette();
}

export function renderPalette() {
  const hintEl = $("palette-hint");
  const paletteEl = $("palette");
  const searchInput = $("search");
  if (!paletteEl) return;

  if (!state.manifest?.nodeTypes || state.manifest.nodeTypes.length === 0) {
    if (hintEl) hintEl.innerHTML = "";
    paletteEl.innerHTML = `
      <div class="empty-palette-hint">
        <p style="margin-bottom: 8px;">尚未获取到节点定义</p>
        <p style="font-size: 12px; color: #94a3b8; line-height: 1.5;">请点击上方“连接设备”或“导入 Manifest”加载可用节点。</p>
      </div>
    `;
    return;
  }

  const query = searchInput ? searchInput.value.trim() : "";
  const isSearching = Boolean(query);

  if (searchInput) {
    searchInput.classList.toggle("has-value", isSearching);
  }

  const matchingSpecs = matchingPaletteSpecs();
  const totalMatching = matchingSpecs.length;

  if (hintEl) {
    if (state.pendingLink) {
      const sourceNode = nodeById(state.pendingLink.nodeId);
      const sourceSpec = specForNode(sourceNode);
      const output = sourceSpec?.outputPorts?.find((p) => p.id === state.pendingLink.portId);
      const nodeTitle = sourceNode?.label || sourceSpec?.editor?.title || sourceNode?.type || state.pendingLink.nodeId;
      const portLabel = output?.label || state.pendingLink.portId;

      hintEl.innerHTML = `
        <div class="pending-link-banner">
          <span>从 <strong>${escapeHtml(nodeTitle)}</strong> 的 <code>${escapeHtml(portLabel)}</code> 端口拖出连线：</span>
          <span style="font-size: 11px; opacity: 0.85;">点击下方节点可直接新建并完成连线</span>
          <button id="cancel-link-btn" type="button" class="cancel-link-btn">✕ 取消连线（显示全部）</button>
        </div>
      `;
      $("cancel-link-btn")?.addEventListener("click", () => {
        state.pendingLink = null;
        renderPalette();
      });
    } else if (isSearching) {
      hintEl.innerHTML = `
        <div class="filter-active-banner">
          <span>🔍 正在筛选：“<strong>${escapeHtml(query)}</strong>” (${totalMatching} 个结果)</span>
          <button id="hint-clear-search" type="button" class="filter-clear-link">清空</button>
        </div>
      `;
      $("hint-clear-search")?.addEventListener("click", () => {
        if (searchInput) {
          searchInput.value = "";
          searchInput.focus();
        }
        renderPalette();
      });
    } else {
      hintEl.innerHTML = `
        <div class="palette-quick-actions">
          <span class="palette-total-count">共 ${totalMatching} 个节点</span>
          <div class="palette-toggle-links">
            <button type="button" id="expand-all-cats-btn" class="quick-link-btn">全部展开</button>
            <span class="quick-link-divider">|</span>
            <button type="button" id="collapse-all-cats-btn" class="quick-link-btn">全部收起</button>
          </div>
        </div>
      `;
      $("expand-all-cats-btn")?.addEventListener("click", expandAllCategories);
      $("collapse-all-cats-btn")?.addEventListener("click", collapseAllCategories);
    }
  }

  const groups = groupedPaletteSpecs();

  if (groups.length === 0) {
    paletteEl.innerHTML = `
      <div class="empty-palette-hint">
        <p>暂无匹配的节点${state.pendingLink ? "（已根据连线端口类型过滤）" : isSearching ? `（关键词: “${escapeHtml(query)}”）` : ""}</p>
        ${state.pendingLink ? `<button id="clear-pending-link-btn" type="button" class="action-btn-small" style="margin-top: 8px;">显示全部节点（取消连线）</button>` : ""}
        ${isSearching ? `<button id="clear-search-empty-btn" type="button" class="action-btn-small" style="margin-top: 8px;">清空筛选条件</button>` : ""}
      </div>
    `;
    $("clear-pending-link-btn")?.addEventListener("click", () => {
      state.pendingLink = null;
      renderPalette();
    });
    $("clear-search-empty-btn")?.addEventListener("click", () => {
      if (searchInput) {
        searchInput.value = "";
        searchInput.focus();
      }
      renderPalette();
    });
    return;
  }

  paletteEl.innerHTML = groups
    .map(({ category, label, specs }) => {
      // By default expanded unless present in collapsedCategories
      const isExpanded = isSearching || state.pendingLink || !collapsedCategories.has(category);
      const items = specs
        .map(
          (spec) => {
            const isStart = spec.type === "flow.start";
            const isEnd = spec.type === "flow.end";
            const itemClasses = ["palette-item", "palette-node-card"];
            if (isStart) itemClasses.push("palette-item-start");
            if (isEnd) itemClasses.push("palette-item-end");

            const specialBadge = isStart
              ? `<span class="palette-badge palette-badge-entry">入口</span>`
              : isEnd
              ? `<span class="palette-badge palette-badge-exit">出口</span>`
              : "";

            return `
              <div class="${itemClasses.join(" ")}" data-type="${escapeHtml(spec.type)}">
                <div class="palette-item-row palette-item-title-row">
                  <span class="palette-item-title" title="${escapeHtml(spec.editor?.title || spec.type)}">${escapeHtml(spec.editor?.title || spec.type)}</span>
                  ${specialBadge}
                </div>
                <div class="palette-item-row palette-item-id-row">
                  <span class="palette-type-tag" title="${escapeHtml(spec.type)}">${escapeHtml(spec.type)}</span>
                </div>
                <div class="palette-item-row palette-item-desc-row">
                  <span class="palette-item-description" title="${escapeHtml(spec.editor?.description || spec.type)}">${escapeHtml(spec.editor?.description || spec.type)}</span>
                </div>
              </div>
            `;
          },
        )
        .join("");
      return `
        <section class="palette-group ${isExpanded ? "group-expanded" : "group-collapsed"}">
          <div
            class="palette-group-toggle"
            data-category="${escapeHtml(category)}"
            role="button"
            tabindex="0"
          >
            <div class="group-header-left">
              <span class="group-chevron">${isExpanded ? "▼" : "▶"}</span>
              <span class="group-folder-icon">${isExpanded ? "📂" : "📁"}</span>
              <span class="group-title">${escapeHtml(label)}</span>
            </div>
            <span class="palette-group-count">${specs.length}</span>
          </div>
          <div class="palette-group-items ${isExpanded ? "expanded" : ""}">
            ${items}
          </div>
        </section>
      `;
    })
    .join("");
}

export function nextNodePosition(sourceNode = null) {
  if (sourceNode) {
    return {
      x: (sourceNode.layout?.x ?? EditorConfig.defaultNodePosition.x) + EditorConfig.nodeWidth + 60,
      y: (sourceNode.layout?.y ?? EditorConfig.defaultNodePosition.y) + 20,
    };
  }
  if (!state.workflow.nodes || state.workflow.nodes.length === 0) {
    return { ...EditorConfig.defaultNodePosition };
  }
  const count = state.workflow.nodes.length;
  const lastNode = state.workflow.nodes[count - 1];
  const stepX = 40;
  const stepY = 30;
  const cycle = count % 10;
  let nextX = (lastNode.layout?.x ?? EditorConfig.defaultNodePosition.x) + stepX;
  let nextY = (lastNode.layout?.y ?? EditorConfig.defaultNodePosition.y) + stepY;

  if (nextX > 1400 || nextY > 900) {
    nextX = EditorConfig.defaultNodePosition.x + (cycle * 25);
    nextY = EditorConfig.defaultNodePosition.y + (cycle * 25);
  }
  return { x: nextX, y: nextY };
}

export function createNode(spec, layout = null) {
  const id = generateUUID();
  const position = layout ?? nextNodePosition();
  const fieldDefaults = Object.fromEntries(
    spec.editor.fields
      .filter(
        (field) =>
          field.defaultValue !== undefined && field.defaultValue !== null,
      )
      .map((field) => [field.key, field.defaultValue]),
  );
  state.workflow.nodes.push({
    id,
    type: spec.type,
    nodeVersion: spec.latestVersion,
    label: spec.editor.title,
    config: {
      ...fieldDefaults,
      ...structuredClone(spec.editor.defaultConfig ?? {}),
    },
    layout: { ...position },
  });
  if (spec.type === "flow.start" && !state.workflow.entryNodeId) {
    state.workflow.entryNodeId = id;
  }
  state.selectedNodeId = id;
  return nodeById(id);
}

export function addNodeFromPalette(type, renderCallback) {
  const spec = state.manifest.nodeTypes.find((item) => item.type === type);
  if (!spec) return;

  // 入口节点仅支持一个，已存在时聚焦并提示，无需重复创建
  if (spec.type === "flow.start") {
    const existingStart = state.workflow.nodes?.find(
      (n) => n.type === "flow.start" || n.id === state.workflow.entryNodeId,
    );
    if (existingStart) {
      showToast("工作流仅支持一个入口节点", "warning");
      state.selectedNodeId = existingStart.id;
      if (renderCallback) renderCallback();
      const el = document.getElementById(`node-${existingStart.id}`);
      el?.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" });
      return;
    }
  }

  const sourceNode = state.pendingLink ? nodeById(state.pendingLink.nodeId) : null;
  const position = nextNodePosition(sourceNode);
  const node = createNode(spec, position);
  if (state.pendingLink) {
    const input = compatibleInputPort(spec, state.pendingLink);
    if (
      input &&
      canConnect(state.pendingLink, { nodeId: node.id, portId: input.id })
    ) {
      state.workflow.edges.push({
        id: generateUUID(),
        source: state.pendingLink,
        target: { nodeId: node.id, portId: input.id },
        kind: input.kind,
      });
    }
    state.pendingLink = null;
  }
  if (renderCallback) renderCallback();
}

export function initializePaletteEvents(renderCallback) {
  const searchInput = $("search");
  searchInput?.addEventListener("input", renderPalette);
  searchInput?.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && searchInput.value) {
      e.stopPropagation();
      searchInput.value = "";
      renderPalette();
    }
  });

  $("search-clear-btn")?.addEventListener("click", () => {
    if (searchInput) {
      searchInput.value = "";
      searchInput.focus();
    }
    renderPalette();
  });

  document.addEventListener("click", (event) => {
    const categoryButton = event.target.closest("[data-category]");
    if (categoryButton) {
      const category = categoryButton.dataset.category;
      toggleCategory(category);
      return;
    }
    const paletteButton = event.target.closest("[data-type]");
    if (paletteButton) {
      addNodeFromPalette(paletteButton.dataset.type, renderCallback);
      return;
    }
  });
}
