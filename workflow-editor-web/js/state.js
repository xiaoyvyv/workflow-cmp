import { EditorConfig } from "./config.js";
import { showEvent } from "./console/console.js";
import { generateUUID } from "./utils.js";

export function newWorkflow() {
  const startId = generateUUID();
  const endId = generateUUID();
  return {
    formatVersion: EditorConfig.workflowFormatVersion,
    id: generateUUID(),
    name: "未命名工作流",
    description: "",
    enabled: true,
    entryNodeId: startId,
    globalErrorNodeId: null,
    requiredCapabilities: [],
    nodes: [
      {
        id: startId,
        type: "flow.start",
        nodeVersion: 1,
        label: "开始",
        config: {},
        layout: { x: 80, y: 140 },
      },
      {
        id: endId,
        type: "flow.end",
        nodeVersion: 1,
        label: "结束",
        config: {},
        layout: { x: 460, y: 140 },
      },
    ],
    edges: [],
  };
}

export const state = {
  manifest: { nodeTypes: [], categories: [], capabilities: [] },
  workflow: newWorkflow(),
  selectedNodeId: null,
  selectedEdgeId: null,
  dragging: null,
  linking: null,
  pendingLink: null,
  revision: 0,
  eventSocket: null,
  expandedPaletteCategories: new Set(),
  errorNodeIds: new Set(),
  zoom: 1.0,
  panX: 0,
  panY: 0,
  layoutDirection: "dag-lr",
};

const getDraftStorageKey = () => EditorConfig.storage?.draftKey || "workflow_editor_draft_v1";
let saveDraftTimer = null;

export function saveDraft(immediate = false) {
  const doSave = () => {
    try {
      if (!state.workflow) return;
      const data = {
        workflow: state.workflow,
        selectedNodeId: state.selectedNodeId,
        selectedEdgeId: state.selectedEdgeId,
        revision: state.revision,
        zoom: state.zoom ?? 1.0,
        panX: state.panX ?? 0,
        panY: state.panY ?? 0,
        layoutDirection: state.layoutDirection,
        savedAt: Date.now(),
      };
      localStorage.setItem(getDraftStorageKey(), JSON.stringify(data));
    } catch (e) {
      console.warn("Failed to save draft to localStorage:", e);
    }
  };

  if (saveDraftTimer) clearTimeout(saveDraftTimer);
  if (immediate) {
    doSave();
  } else {
    saveDraftTimer = setTimeout(doSave, 100);
  }
}

export function restoreDraft() {
  try {
    const raw = localStorage.getItem(getDraftStorageKey());
    if (!raw) return false;
    const data = JSON.parse(raw);
    if (data && data.workflow && Array.isArray(data.workflow.nodes) && data.workflow.nodes.length > 0) {
      state.workflow = data.workflow;
      if (typeof data.revision === "number") state.revision = data.revision;
      if (typeof data.zoom === "number") state.zoom = data.zoom;
      if (typeof data.panX === "number") state.panX = data.panX;
      if (typeof data.panY === "number") state.panY = data.panY;
      if (data.layoutDirection) state.layoutDirection = data.layoutDirection;
      state.selectedNodeId = data.selectedNodeId || null;
      state.selectedEdgeId = data.selectedEdgeId || null;
      return true;
    }
  } catch (e) {
    console.warn("Failed to restore draft from localStorage:", e);
  }
  return false;
}

export function clearDraft() {
  try {
    localStorage.removeItem(getDraftStorageKey());
  } catch (e) {
    console.warn("Failed to clear draft from localStorage:", e);
  }
}

export function nodeById(id) {
  return state.workflow.nodes.find((node) => node.id === id);
}

export function specForNode(node) {
  if (!node) return null;
  return state.manifest?.nodeTypes?.find((spec) => spec.type === node.type) || null;
}

export function getNodeTitle(nodeId) {
  const node = nodeById(nodeId);
  if (!node) return nodeId;
  const spec = specForNode(node);
  return node.label || spec?.editor?.title || node.type || nodeId;
}

export function getAllCapabilities() {
  const capMap = new Map();
  // 1. Read capabilities specification from API manifest
  if (Array.isArray(state.manifest.capabilities)) {
    state.manifest.capabilities.forEach((item) => {
      if (typeof item === "object" && item && item.id) {
        capMap.set(item.id, {
          id: item.id,
          label: item.label || item.id,
          description: item.description || "",
        });
      } else if (typeof item === "string") {
        capMap.set(item, { id: item, label: item, description: "" });
      }
    });
  }

  // 2. Also ensure any capabilities declared by registered nodes are included
  state.manifest.nodeTypes?.forEach((node) => {
    node.requiredCapabilities?.forEach((capId) => {
      if (!capMap.has(capId)) {
        capMap.set(capId, { id: capId, label: capId, description: "" });
      }
    });
  });

  return [...capMap.values()].sort((a, b) => a.id.localeCompare(b.id));
}

export function ensureWorkflowCapabilities(notify = false, onUpdated = null) {
  const caps = new Set(state.workflow.requiredCapabilities || []);
  let added = false;
  state.workflow.nodes.forEach((node) => {
    const spec = specForNode(node);
    spec?.requiredCapabilities?.forEach((cap) => {
      if (!caps.has(cap)) {
        caps.add(cap);
        added = true;
      }
    });
  });
  if (added || !state.workflow.requiredCapabilities) {
    state.workflow.requiredCapabilities = [...caps].sort();
    if (notify) {
      showEvent({
        type: EditorConfig.eventType.capabilitiesAutoCompleted,
        capabilities: state.workflow.requiredCapabilities,
      }, getNodeTitle);
    }
    if (onUpdated) onUpdated();
  }
  return state.workflow.requiredCapabilities;
}

export function autoSyncRequiredCapabilities(onUpdated = null) {
  ensureWorkflowCapabilities(true, onUpdated);
}
