import { EditorConfig } from "./config.js";
import { showEvent } from "./console/console.js";

export function newWorkflow() {
  const startId = crypto.randomUUID();
  const endId = crypto.randomUUID();
  return {
    formatVersion: EditorConfig.workflowFormatVersion,
    id: crypto.randomUUID(),
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

const builtinFallbackSpecs = [
  {
    type: "flow.start",
    editor: {
      title: "开始",
      description: "工作流的唯一入口节点。",
      fields: [],
    },
    inputPorts: [],
    outputPorts: [{ id: "next", label: "下一步", kind: "control", maxConnections: 1 }],
  },
  {
    type: "flow.end",
    editor: {
      title: "结束",
      description: "结束当前工作流执行。",
      fields: [],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [],
  },
  {
    type: "flow.switch",
    editor: {
      title: "多路分支",
      description: "根据匹配值与分支映射表分发到 matched 或 default 端口。",
      fields: [],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [
      { id: "matched", label: "匹配", kind: "control", maxConnections: 1 },
      { id: "default", label: "默认", kind: "control", maxConnections: 1 },
    ],
  },
  {
    type: "flow.delay",
    editor: {
      title: "延时等待",
      description: "暂停当前分支指定的时间（毫秒）。",
      fields: [],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [{ id: "next", label: "下一步", kind: "control", maxConnections: 1 }],
  },
  {
    type: "flow.log",
    editor: {
      title: "记录日志",
      description: "在工作流执行期间输出日志消息与附加数据。",
      fields: [],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [{ id: "next", label: "下一步", kind: "control", maxConnections: 1 }],
  },
  {
    type: "loop.repeat",
    editor: {
      title: "次数循环 (Repeat)",
      description: "按指定次数重复执行循环体分支流程。",
      fields: [
        { key: "count", label: "循环次数", kind: "number", defaultValue: 5, required: true, order: 0 },
        { key: "maxIterations", label: "最大安全迭代次数", kind: "number", defaultValue: 10000, required: false, order: 1 },
      ],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [
      { id: "body", label: "循环体 (body)", kind: "control", maxConnections: 1 },
      { id: "completed", label: "完成 (completed)", kind: "control", maxConnections: 1 },
      { id: "failure", label: "失败 (failure)", kind: "control", maxConnections: 1 },
    ],
  },
  {
    type: "loop.for_each",
    editor: {
      title: "遍历循环 (ForEach)",
      description: "遍历数组或列表中的每一个元素并执行循环体分支。",
      fields: [
        { key: "items", label: "待遍历的数组/列表", kind: "json", required: true, order: 0 },
        { key: "maxIterations", label: "最大安全迭代次数", kind: "number", defaultValue: 10000, required: false, order: 1 },
      ],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [
      { id: "body", label: "循环体 (body)", kind: "control", maxConnections: 1 },
      { id: "completed", label: "完成 (completed)", kind: "control", maxConnections: 1 },
      { id: "failure", label: "失败 (failure)", kind: "control", maxConnections: 1 },
    ],
  },
  {
    type: "loop.while",
    editor: {
      title: "条件循环 (While)",
      description: "当条件表达式为真时持续重复执行循环体分支。",
      fields: [
        { key: "condition", label: "继续循环条件表达式", kind: "template-text", required: true, order: 0 },
        { key: "maxIterations", label: "最大安全迭代次数", kind: "number", defaultValue: 10000, required: false, order: 1 },
      ],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [
      { id: "body", label: "循环体 (body)", kind: "control", maxConnections: 1 },
      { id: "completed", label: "完成 (completed)", kind: "control", maxConnections: 1 },
      { id: "failure", label: "失败 (failure)", kind: "control", maxConnections: 1 },
    ],
  },
  {
    type: "loop.next",
    editor: {
      title: "循环步进 (Next)",
      description: "标记当前循环轮次完成，进入下一轮迭代。",
      fields: [
        { key: "loopId", label: "关联循环节点 ID", kind: "template-text", required: false, placeholder: "可选：多层嵌套循环时指定循环 ID", order: 0 },
      ],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [],
  },
  {
    type: "loop.continue",
    editor: {
      title: "跳过本轮 (Continue)",
      description: "跳过当前循环的后续步骤，直接进入下一轮迭代。",
      fields: [
        { key: "loopId", label: "关联循环节点 ID", kind: "template-text", required: false, placeholder: "可选：多层嵌套循环时指定循环 ID", order: 0 },
      ],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [],
  },
  {
    type: "loop.break",
    editor: {
      title: "中断循环 (Break)",
      description: "立即中断并跳出循环，流向 completed 端口。",
      fields: [
        { key: "loopId", label: "关联循环节点 ID", kind: "template-text", required: false, placeholder: "可选：多层嵌套循环时指定循环 ID", order: 0 },
      ],
    },
    inputPorts: [{ id: "in", label: "输入", kind: "control", maxConnections: 2147483647 }],
    outputPorts: [],
  },
];

export const state = {
  manifest: { nodeTypes: [...builtinFallbackSpecs], categories: [], capabilities: [] },
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

const STORAGE_KEY = "workflow_editor_draft_v1";
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
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
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
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return false;
    const data = JSON.parse(raw);
    if (data && data.workflow && Array.isArray(data.workflow.nodes) && data.workflow.nodes.length > 0) {
      state.workflow = data.workflow;
      if (typeof data.revision === "number") state.revision = data.revision;
      if (typeof data.zoom === "number") state.zoom = data.zoom;
      if (typeof data.panX === "number") state.panX = data.panX;
      if (typeof data.panY === "number") state.panY = data.panY;
      if (data.layoutDirection) state.layoutDirection = data.layoutDirection;
      if (data.selectedNodeId && state.workflow.nodes.some((n) => n.id === data.selectedNodeId)) {
        state.selectedNodeId = data.selectedNodeId;
      }
      if (data.selectedEdgeId && state.workflow.edges && state.workflow.edges.some((e) => e.id === data.selectedEdgeId)) {
        state.selectedEdgeId = data.selectedEdgeId;
      }
      return true;
    }
  } catch (e) {
    console.warn("Failed to restore draft from localStorage:", e);
  }
  return false;
}

export function clearDraft() {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch (e) {
    console.warn("Failed to clear draft from localStorage:", e);
  }
}

export function nodeById(id) {
  return state.workflow.nodes.find((node) => node.id === id);
}

export function specForNode(node) {
  if (!node) return null;
  const fromManifest = state.manifest?.nodeTypes?.find((spec) => spec.type === node.type);
  if (fromManifest) return fromManifest;
  return builtinFallbackSpecs.find((spec) => spec.type === node.type) || null;
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
        type: "editor.capabilities.auto_completed",
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
