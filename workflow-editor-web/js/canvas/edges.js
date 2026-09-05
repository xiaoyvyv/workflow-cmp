import { EditorConfig } from "../config.js";
import { state, nodeById, specForNode } from "../state.js";
import { $, escapeHtml, createBezierPath } from "../utils.js";
import { getNodeDimensions } from "./layout.js";

export function getBezierPoint(x1, y1, x2, y2, t) {
  const dx = Math.max(30, Math.abs(x2 - x1) * 0.5);
  const controlOffset = Math.max(EditorConfig.edgeControlOffset, dx);
  const cx1 = x1 + controlOffset;
  const cy1 = y1;
  const cx2 = x2 - controlOffset;
  const cy2 = y2;

  const mt = 1 - t;
  const mt2 = mt * mt;
  const mt3 = mt2 * mt;
  const t2 = t * t;
  const t3 = t2 * t;

  const x = mt3 * x1 + 3 * mt2 * t * cx1 + 3 * mt * t2 * cx2 + t3 * x2;
  const y = mt3 * y1 + 3 * mt2 * t * cy1 + 3 * mt * t2 * cy2 + t3 * y2;
  return { x, y };
}

export function getPortPosition(nodeId, portId, direction) {
  const node = nodeById(nodeId);
  if (!node) return { x: 0, y: 0 };
  const nodeEl = document.getElementById(`node-${nodeId}`);
  const lx = Number.isFinite(node.layout?.x) ? node.layout.x : 80;
  const ly = Number.isFinite(node.layout?.y) ? node.layout.y : 80;
  if (!nodeEl) {
    return {
      x: direction === "out" ? lx + (EditorConfig.nodeWidth || 240) : lx,
      y: ly + EditorConfig.portVerticalOffset,
    };
  }
  const portEl = nodeEl.querySelector(`.port.${direction}[data-port="${portId}"]`);
  if (!portEl) {
    return {
      x: direction === "out" ? lx + nodeEl.offsetWidth : lx,
      y: ly + EditorConfig.portVerticalOffset,
    };
  }
  const handleEl = portEl.querySelector(".port-handle") || portEl;
  const nodeRect = nodeEl.getBoundingClientRect();
  const handleRect = handleEl.getBoundingClientRect();
  const zoom = state.zoom || 1.0;
  const relX = (handleRect.left + handleRect.width / 2 - nodeRect.left) / zoom;
  const relY = (handleRect.top + handleRect.height / 2 - nodeRect.top) / zoom;
  const x = lx + relX;
  const y = ly + relY;
  return { x, y };
}

export function canConnect(source, target) {
  if (!source || !target || source.nodeId === target.nodeId) return false;
  const sourceSpec = specForNode(nodeById(source.nodeId));
  const targetSpec = specForNode(nodeById(target.nodeId));
  const output = sourceSpec?.outputPorts?.find(
    (port) => port.id === source.portId,
  );
  const input = targetSpec?.inputPorts?.find(
    (port) => port.id === target.portId,
  );
  if (!output || !input) return false;

  const sourceKind = output.kind || "control";
  const targetKind = input.kind || "control";
  if (sourceKind !== targetKind) return false;

  const outputCount = state.workflow.edges.filter(
    (edge) =>
      edge.source.nodeId === source.nodeId &&
      edge.source.portId === source.portId,
  ).length;
  const inputCount = state.workflow.edges.filter(
    (edge) =>
      edge.target.nodeId === target.nodeId &&
      edge.target.portId === target.portId,
  ).length;

  const maxOut = output.maxConnections ?? 2147483647;
  // flow.end 是工作流的控制流汇聚点。即使当前画布加载的是早期 Manifest，
  // 也必须允许多个分支汇入其 in 端口；最新设备端 Spec 同样声明该端口无限连接。
  const targetIsFlowEndInput =
    nodeById(target.nodeId)?.type === "flow.end" && target.portId === "in";
  const maxIn =
    targetIsFlowEndInput
      ? Number.POSITIVE_INFINITY
      : input.maxConnections ?? 1;
  return outputCount < maxOut && inputCount < maxIn;
}

export function highlightCompatiblePorts(source, active) {
  document.querySelectorAll(".port").forEach((portEl) => {
    portEl.classList.remove("port-compatible", "port-hover");
  });
  if (!active || !source) return;
  const sourceSpec = specForNode(nodeById(source.nodeId));
  const sourcePort = sourceSpec?.outputPorts?.find((p) => p.id === source.portId);
  if (!sourcePort) return;

  state.workflow.nodes.forEach((node) => {
    if (node.id === source.nodeId) return;
    const spec = specForNode(node);
    spec?.inputPorts?.forEach((inPort) => {
      if (canConnect(source, { nodeId: node.id, portId: inPort.id })) {
        const nodeEl = document.getElementById(`node-${node.id}`);
        const portEl = nodeEl?.querySelector(`.port.in[data-port="${inPort.id}"]`);
        if (portEl) {
          portEl.classList.add("port-compatible");
        }
      }
    });
  });
}

export function getCubicBezierPoint(x1, y1, cx1, cy1, cx2, cy2, x2, y2, t) {
  const mt = 1 - t;
  const mt2 = mt * mt;
  const mt3 = mt2 * mt;
  const t2 = t * t;
  const t3 = t2 * t;

  const x = mt3 * x1 + 3 * mt2 * t * cx1 + 3 * mt * t2 * cx2 + t3 * x2;
  const y = mt3 * y1 + 3 * mt2 * t * cy1 + 3 * mt * t2 * cy2 + t3 * y2;
  return { x, y };
}

export function getLoopHeaderNode(node) {
  if (!node) return null;
  const loopTypes = ["loop.repeat", "loop.for_each", "loop.forEach", "loop.while"];

  // 1. 优先使用节点配置中的显式 loopId
  const explicitLoopId = node.config?.loopId;
  if (explicitLoopId) {
    const target = nodeById(explicitLoopId);
    if (target && loopTypes.includes(target.type)) {
      return target;
    }
  }

  // 2. 沿入边向上反向追溯 (BFS) 寻找所属的外层循环节点
  const visited = new Set();
  const queue = [node.id];
  visited.add(node.id);

  while (queue.length > 0) {
    const currentId = queue.shift();
    const incomingEdges = (state.workflow.edges || []).filter((e) => e.target?.nodeId === currentId);
    for (const edge of incomingEdges) {
      const srcNode = nodeById(edge.source?.nodeId);
      if (!srcNode) continue;
      if (loopTypes.includes(srcNode.type)) {
        return srcNode;
      }
      if (!visited.has(srcNode.id)) {
        visited.add(srcNode.id);
        queue.push(srcNode.id);
      }
    }
  }

  // 3. 兜底策略：如果当前画布中只有一个循环节点，则自动绑定该循环节点
  const allLoopNodes = (state.workflow.nodes || []).filter((n) => loopTypes.includes(n.type));
  if (allLoopNodes.length === 1) {
    return allLoopNodes[0];
  }

  return null;
}

export function drawEdges() {
  const edgesContainer = $("edges");
  if (!edgesContainer) return;

  const defsSvg = `
    <defs>
      <marker id="loopback-arrow" viewBox="0 0 10 10" refX="7" refY="5" markerWidth="6" markerHeight="6" orient="auto">
        <path d="M 0 1.5 L 8 5 L 0 8.5 z" fill="#a855f7"/>
      </marker>
      <marker id="loopback-arrow-hover" viewBox="0 0 10 10" refX="7" refY="5" markerWidth="6" markerHeight="6" orient="auto">
        <path d="M 0 1.5 L 8 5 L 0 8.5 z" fill="#c084fc"/>
      </marker>
    </defs>
  `;

  const edgePaths = state.workflow.edges
    .map((edge) => {
      const p1 = getPortPosition(edge.source.nodeId, edge.source.portId, "out");
      const p2 = getPortPosition(edge.target.nodeId, edge.target.portId, "in");
      const path = createBezierPath(p1.x, p1.y, p2.x, p2.y);
      const isSelected = state.selectedEdgeId === edge.id;

      // Calculate source & target port labels for badge
      const sourceNode = nodeById(edge.source.nodeId);
      const sourceSpec = specForNode(sourceNode);
      const sourcePort = sourceSpec?.outputPorts?.find((p) => p.id === edge.source.portId);
      const targetNode = nodeById(edge.target.nodeId);
      const targetSpec = specForNode(targetNode);
      const targetPort = targetSpec?.inputPorts?.find((p) => p.id === edge.target.portId);

      const sourceLabel = sourcePort?.label || edge.source.portId;
      let badgeText = sourceLabel || "";
      if (targetPort && targetPort.id !== "in" && targetPort.label && targetPort.label !== "输入") {
        badgeText = `${sourceLabel} → ${targetPort.label}`;
      }

      let badgeWidth = 32;
      let textWidth = 0;
      if (badgeText) {
        for (let i = 0; i < badgeText.length; i++) {
          const code = badgeText.charCodeAt(i);
          textWidth += code > 255 ? 12 : 6.8;
        }
        badgeWidth = Math.max(30, Math.ceil(textWidth + 14));
      }
      const badgeHeight = 20;
      const halfW = badgeWidth / 2;
      const halfH = badgeHeight / 2;

      // 获取源节点与目标节点几何边界
      const srcDim = getNodeDimensions(sourceNode);
      const tgtDim = getNodeDimensions(targetNode);

      const srcLeft = Number.isFinite(sourceNode?.layout?.x) ? sourceNode.layout.x : 80;
      const srcTop = Number.isFinite(sourceNode?.layout?.y) ? sourceNode.layout.y : 80;
      const srcRight = srcLeft + srcDim.width;
      const srcBottom = srcTop + srcDim.height;

      const tgtLeft = Number.isFinite(targetNode?.layout?.x) ? targetNode.layout.x : 80;
      const tgtTop = Number.isFinite(targetNode?.layout?.y) ? targetNode.layout.y : 80;
      const tgtRight = tgtLeft + tgtDim.width;
      const tgtBottom = tgtTop + tgtDim.height;

      // 严格沿贝塞尔连线路径采样点，确保 Badge 始终位于连线上，并优先选择未被卡片遮挡的位置
      const candidateTs = [0.5, 0.55, 0.45, 0.6, 0.4, 0.65, 0.35, 0.7, 0.3, 0.75, 0.25, 0.8, 0.2];
      let bestPoint = getBezierPoint(p1.x, p1.y, p2.x, p2.y, 0.5);

      for (const t of candidateTs) {
        const pt = getBezierPoint(p1.x, p1.y, p2.x, p2.y, t);
        const bLeft = pt.x - halfW;
        const bRight = pt.x + halfW;
        const bTop = pt.y - halfH;
        const bBottom = pt.y + halfH;

        const overlapsSrc = !(bRight < srcLeft || bLeft > srcRight || bBottom < srcTop || bTop > srcBottom);
        const overlapsTgt = !(bRight < tgtLeft || bLeft > tgtRight || bBottom < tgtTop || bTop > tgtBottom);

        if (!overlapsSrc && !overlapsTgt) {
          bestPoint = pt;
          break;
        }
      }

      const xMid = bestPoint.x;
      const yMid = bestPoint.y;

      const badgeSvg = badgeText
        ? `
          <g class="edge-badge" transform="translate(${Math.round(xMid)}, ${Math.round(yMid)})" data-edge-id="${escapeHtml(edge.id)}">
            <rect x="-${halfW}" y="-${halfH}" width="${badgeWidth}" height="${badgeHeight}" rx="${halfH}" class="edge-badge-bg"/>
            <text x="0" y="0" text-anchor="middle" dominant-baseline="central" alignment-baseline="central" class="edge-badge-text">${escapeHtml(badgeText)}</text>
          </g>
        `
        : "";

      return `
        <g class="edge-group ${isSelected ? "edge-selected" : ""}" data-edge-id="${escapeHtml(edge.id)}">
          <path class="edge-hitbox" data-edge-id="${escapeHtml(edge.id)}" d="${path}"/>
          <path class="edge" data-edge-id="${escapeHtml(edge.id)}" d="${path}">
            <title>双击变红删除连线</title>
          </path>
          ${badgeSvg}
        </g>
      `;
    })
    .join("");

  // 渲染 loop.next / loop.continue 的虚拟回环虚线
  const loopControlTypes = ["loop.next", "loop.continue"];
  const loopbackPaths = (state.workflow.nodes || [])
    .filter((node) => loopControlTypes.includes(node.type))
    .map((node) => {
      const targetLoop = getLoopHeaderNode(node);
      if (!targetLoop) return "";

      const fromDim = getNodeDimensions(node);
      const fromX = Number.isFinite(node.layout?.x) ? node.layout.x : 80;
      const fromY = Number.isFinite(node.layout?.y) ? node.layout.y : 80;

      // 起点：从 loop.next 卡片右侧边缘引出
      const p1 = {
        x: fromX + fromDim.width,
        y: fromY + Math.min(36, fromDim.height / 2),
      };

      // 终点：指向目标循环节点的输入端口
      const p2 = getPortPosition(targetLoop.id, "in", "in");

      // 计算优雅的回环贝塞尔曲线
      const dx = Math.max(60, Math.abs(p1.x - p2.x) * 0.35);
      const cx1 = p1.x + dx;
      const cy1 = p1.y;
      const cx2 = p2.x - dx;
      const cy2 = p2.y;
      const path = `M ${p1.x} ${p1.y} C ${cx1} ${cy1}, ${cx2} ${cy2}, ${p2.x} ${p2.y}`;

      const isRelatedSelected =
        state.selectedNodeId === node.id || state.selectedNodeId === targetLoop.id;

      const badgeText = node.type === "loop.continue" ? "跳过本轮 ↺" : "循环下一轮 ↺";
      let textWidth = 0;
      for (let i = 0; i < badgeText.length; i++) {
        const code = badgeText.charCodeAt(i);
        textWidth += code > 255 ? 12 : 6.8;
      }
      const badgeWidth = Math.max(48, Math.ceil(textWidth + 16));
      const badgeHeight = 22;
      const halfW = badgeWidth / 2;
      const halfH = badgeHeight / 2;

      // 取曲线中点作为 Badge 坐标
      const midPoint = getCubicBezierPoint(p1.x, p1.y, cx1, cy1, cx2, cy2, p2.x, p2.y, 0.5);

      const tooltip = `循环回环：${node.label || node.type} ➔ ${targetLoop.label || targetLoop.type}`;

      return `
        <g class="edge-group edge-group-loopback ${isRelatedSelected ? "edge-selected" : ""}" data-edge-id="virtual-loopback-${escapeHtml(node.id)}" data-loop-return-id="${escapeHtml(node.id)}">
          <path class="edge-hitbox" data-edge-id="virtual-loopback-${escapeHtml(node.id)}" data-loop-return-id="${escapeHtml(node.id)}" d="${path}"/>
          <path class="edge edge-loopback" data-edge-id="virtual-loopback-${escapeHtml(node.id)}" data-loop-return-id="${escapeHtml(node.id)}" d="${path}" marker-end="url(#loopback-arrow)">
            <title>${escapeHtml(tooltip)}</title>
          </path>
          <g class="edge-badge edge-badge-loopback" transform="translate(${Math.round(midPoint.x)}, ${Math.round(midPoint.y)})" data-edge-id="virtual-loopback-${escapeHtml(node.id)}" data-loop-return-id="${escapeHtml(node.id)}">
            <rect x="-${halfW}" y="-${halfH}" width="${badgeWidth}" height="${badgeHeight}" rx="${halfH}" class="edge-badge-bg edge-badge-loopback-bg"/>
            <text x="0" y="0" text-anchor="middle" dominant-baseline="central" alignment-baseline="central" class="edge-badge-text edge-badge-loopback-text">${escapeHtml(badgeText)}</text>
          </g>
        </g>
      `;
    })
    .join("");

  let draggingPath = "";
  if (state.linking) {
    const p1 =
      state.linking.direction === "out"
        ? getPortPosition(state.linking.nodeId, state.linking.portId, "out")
        : { x: state.linking.currentX, y: state.linking.currentY };
    const p2 =
      state.linking.direction === "out"
        ? { x: state.linking.currentX, y: state.linking.currentY }
        : getPortPosition(state.linking.nodeId, state.linking.portId, "in");
    const path = createBezierPath(p1.x, p1.y, p2.x, p2.y);
    draggingPath = `<path class="edge dragging-edge" d="${path}"/>`;
  }

  edgesContainer.innerHTML = defsSvg + edgePaths + loopbackPaths + draggingPath;
}
