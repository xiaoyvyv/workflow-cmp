import { EditorConfig } from "../config.js";
import { specForNode } from "../state.js";

/**
 * 布局算法注册表，支持横向、纵向、紧凑及自定义算法。
 */
export const layoutAlgorithms = new Map();

const DEFAULT_LAYOUT_ID = "dag-lr";
const COMPACT_LAYOUT_ID = "dag-compact";

/**
 * 注册布局算法。
 * @param {string} id 算法标识
 * @param {string} name 算法展示名称
 * @param {Function} layoutFn 布局执行函数 (workflow, options) => void
 */
export function registerLayoutAlgorithm(id, name, layoutFn) {
  layoutAlgorithms.set(id, { id, name, layout: layoutFn });
}

/**
 * 获取节点的真实渲染尺寸或根据内容精确估算尺寸。
 * @param {Object} node 节点数据
 * @returns {{width: number, height: number}}
 */
export function getNodeDimensions(node) {
  if (!node) return { width: 240, height: 140 };

  const nodeEl = document.getElementById(`node-${node.id}`);
  if (nodeEl && nodeEl.offsetWidth > 0 && nodeEl.offsetHeight > 0) {
    return {
      width: nodeEl.offsetWidth,
      height: nodeEl.offsetHeight,
    };
  }

  // 动态估算高度（考虑标题、类型标签、描述文本及端口数量）
  const spec = specForNode(node);
  const inputPortsCount = spec?.inputPorts?.length || 0;
  const outputPortsCount = spec?.outputPorts?.length || 0;
  const maxPorts = Math.max(inputPortsCount, outputPortsCount, 1);

  const hasType = Boolean(node.type);
  const hasDesc = Boolean(
    node.description ||
      spec?.editor?.description ||
      node.type === "flow.start" ||
      node.type === "flow.end",
  );

  let height = 48; // 基础边框和标题行
  if (hasType) height += 22; // 类型标签行
  if (hasDesc) height += 28; // 描述行
  height += maxPorts * 32 + 14; // 端口区域高度

  return {
    width: EditorConfig.nodeWidth || 240,
    height: Math.max(130, Math.ceil(height)),
  };
}

/**
 * 获取端口在卡片内部的相对垂直偏移（供微调连线走线对齐）
 */
function getPortRelativeOffset(node, portId, direction) {
  const spec = specForNode(node);
  const ports = direction === "out" ? spec?.outputPorts : spec?.inputPorts;
  if (!ports || ports.length === 0) {
    return 0.5; // 居中
  }
  const idx = ports.findIndex((p) => p.id === portId);
  if (idx === -1) return 0.5;
  if (ports.length === 1) return 0.5;
  return (idx + 0.5) / ports.length;
}

/**
 * 获取输出端口的自然排布序号（用于优先让上部端口流向靠上的分支，下部端口流向靠下的分支，减少交叉）
 */
function getPortOrderIndex(node, portId) {
  const spec = specForNode(node);
  const ports = spec?.outputPorts || [];
  const idx = ports.findIndex((p) => p.id === portId);
  return idx >= 0 ? idx : 0;
}

/**
 * 消除有向图中的环，构建纯正的 DAG
 */
function buildAcyclicDag(nodes, edges) {
  const nodeMap = new Map(nodes.map((n) => [n.id, n]));
  const adj = new Map();
  nodes.forEach((n) => adj.set(n.id, []));

  edges.forEach((e) => {
    const sId = e.source?.nodeId;
    const tId = e.target?.nodeId;
    if (sId && tId && nodeMap.has(sId) && nodeMap.has(tId) && sId !== tId) {
      adj.get(sId).push({
        targetId: tId,
        edge: e,
      });
    }
  });

  const visited = new Set();
  const visiting = new Set();
  const forwardEdges = [];
  const dagOut = new Map();
  const dagIn = new Map();

  nodes.forEach((n) => {
    dagOut.set(n.id, []);
    dagIn.set(n.id, []);
  });

  function dfs(u) {
    visited.add(u);
    visiting.add(u);

    for (const { targetId: v, edge } of adj.get(u)) {
      if (visiting.has(v)) {
        // 发现后向边/环路边，跳过
        continue;
      }
      forwardEdges.push(edge);
      dagOut.get(u).push({ targetId: v, edge });
      dagIn.get(v).push({ sourceId: u, edge });

      if (!visited.has(v)) {
        dfs(v);
      }
    }

    visiting.delete(u);
  }

  nodes.forEach((n) => {
    if (!visited.has(n.id)) {
      dfs(n.id);
    }
  });

  return { nodeMap, forwardEdges, dagOut, dagIn };
}

/**
 * 拓扑分层 (Rank Assignment)
 * 采用最长路径与紧凑化沉降 (Sink-Pulling) 确保连线紧凑自然
 */
function computeRanks(workflow, nodes, dagOut, dagIn) {
  const ranks = new Map();
  const entryId = workflow.entryNodeId;
  const rootIds = [];

  if (entryId && nodes.some((n) => n.id === entryId)) {
    rootIds.push(entryId);
  }
  nodes.forEach((n) => {
    if (dagIn.get(n.id).length === 0 && !rootIds.includes(n.id)) {
      rootIds.push(n.id);
    }
  });
  if (rootIds.length === 0 && nodes.length > 0) {
    rootIds.push(nodes[0].id);
  }

  rootIds.forEach((id) => ranks.set(id, 0));

  const inDegree = new Map();
  nodes.forEach((n) => inDegree.set(n.id, dagIn.get(n.id).length));
  const queue = [...rootIds];
  const processed = new Set();

  while (queue.length > 0) {
    const u = queue.shift();
    processed.add(u);
    const currRank = ranks.get(u) || 0;

    for (const { targetId: v } of dagOut.get(u)) {
      const targetRank = Math.max(ranks.get(v) || 0, currRank + 1);
      ranks.set(v, targetRank);

      const remainingIn = inDegree.get(v) - 1;
      inDegree.set(v, remainingIn);
      if (remainingIn <= 0 && !processed.has(v)) {
        queue.push(v);
      }
    }
  }

  // 补齐所有未覆盖节点
  nodes.forEach((n) => {
    if (!ranks.has(n.id)) {
      ranks.set(n.id, 0);
      queue.push(n.id);
    }
  });
  while (queue.length > 0) {
    const u = queue.shift();
    const currRank = ranks.get(u) || 0;
    for (const { targetId: v } of dagOut.get(u)) {
      const targetRank = Math.max(ranks.get(v) || 0, currRank + 1);
      if (!ranks.has(v) || ranks.get(v) < targetRank) {
        ranks.set(v, targetRank);
        queue.push(v);
      }
    }
  }

  // 循环节点结构拓扑聚合
  clusterLoopRanks(workflow, nodes, ranks, dagOut, dagIn);

  return ranks;
}

const LOOP_HEADER_TYPES = ["loop.repeat", "loop.for_each", "loop.forEach", "loop.while"];
const LOOP_CONTROL_TYPES = ["loop.next", "loop.continue", "loop.break"];

/**
 * 循环结构拓扑对齐：
 * 仅对齐循环内部控制终端节点（loop.next/continue），保持主流程（completed）自然畅通
 */
function clusterLoopRanks(workflow, nodes, ranks, dagOut, dagIn) {
  const nodeMap = new Map(nodes.map((n) => [n.id, n]));
  const loopHeaders = nodes.filter((n) => LOOP_HEADER_TYPES.includes(n.type));
  if (loopHeaders.length === 0) return;

  loopHeaders.forEach((loopNode) => {
    const loopId = loopNode.id;
    const bodyOutEdges = (dagOut.get(loopId) || []).filter(
      (entry) => entry.edge?.source?.portId === "body",
    );

    const bodyNodeIds = new Set();
    const queue = bodyOutEdges.map((e) => e.targetId);
    queue.forEach((id) => bodyNodeIds.add(id));

    while (queue.length > 0) {
      const currId = queue.shift();
      const currNode = nodeMap.get(currId);
      if (!currNode) continue;
      if (LOOP_CONTROL_TYPES.includes(currNode.type)) continue;

      for (const { targetId } of dagOut.get(currId) || []) {
        if (!bodyNodeIds.has(targetId) && targetId !== loopId) {
          bodyNodeIds.add(targetId);
          queue.push(targetId);
        }
      }
    }

    nodes.forEach((n) => {
      if (LOOP_CONTROL_TYPES.includes(n.type) && !bodyNodeIds.has(n.id)) {
        if (n.config?.loopId === loopId) {
          bodyNodeIds.add(n.id);
        }
      }
    });

    if (bodyNodeIds.size === 0) return;

    let maxBodyRank = ranks.get(loopId) || 0;
    bodyNodeIds.forEach((id) => {
      const r = ranks.get(id) || 0;
      if (r > maxBodyRank) {
        maxBodyRank = r;
      }
    });

    bodyNodeIds.forEach((id) => {
      const n = nodeMap.get(id);
      if (n && (n.type === "loop.next" || n.type === "loop.continue")) {
        ranks.set(id, Math.max(ranks.get(id) || 0, maxBodyRank));
      }
    });
  });
}

function getLoopBodyAnalysis(nodes, dagOut) {
  const nodeMap = new Map(nodes.map((n) => [n.id, n]));
  const isLoopBodyNode = new Set();
  const loopHeaders = nodes.filter((n) => LOOP_HEADER_TYPES.includes(n.type));

  loopHeaders.forEach((loopNode) => {
    const loopId = loopNode.id;
    const bodyOuts = (dagOut.get(loopId) || []).filter(
      (e) => e.edge?.source?.portId === "body",
    );
    const queue = bodyOuts.map((e) => e.targetId);
    queue.forEach((id) => isLoopBodyNode.add(id));

    while (queue.length > 0) {
      const currId = queue.shift();
      const curr = nodeMap.get(currId);
      if (!curr) continue;
      if (LOOP_CONTROL_TYPES.includes(curr.type)) continue;

      for (const { targetId } of dagOut.get(currId) || []) {
        if (!isLoopBodyNode.has(targetId) && targetId !== loopId) {
          isLoopBodyNode.add(targetId);
          queue.push(targetId);
        }
      }
    }

    nodes.forEach((n) => {
      if (LOOP_CONTROL_TYPES.includes(n.type) && n.config?.loopId === loopId) {
        isLoopBodyNode.add(n.id);
      }
    });
  });

  return { isLoopBodyNode };
}

/**
 * 跨层虚拟长边节点插入 (Virtual Dummy Nodes)
 * 将跨越 >1 层的长边分割为单元步进，使得长连线不穿透中间层的节点实体
 */
function insertDummyNodes(nodes, forwardEdges, ranks) {
  const allNodesMap = new Map(nodes.map((n) => [n.id, { ...n, isDummy: false }]));
  const augmentedEdges = [];
  let dummyIndex = 0;

  forwardEdges.forEach((edge) => {
    const sId = edge.source?.nodeId;
    const tId = edge.target?.nodeId;
    const sRank = ranks.get(sId) ?? 0;
    const tRank = ranks.get(tId) ?? 0;

    if (tRank - sRank > 1) {
      let prevId = sId;
      for (let r = sRank + 1; r < tRank; r++) {
        const dummyId = `__dummy_${dummyIndex++}_${sId}_${tId}`;
        const dummyNode = {
          id: dummyId,
          isDummy: true,
          sourceId: sId,
          targetId: tId,
          sourcePortId: edge.source?.portId,
          targetPortId: edge.target?.portId,
          layout: { x: 0, y: 0 },
        };
        allNodesMap.set(dummyId, dummyNode);
        ranks.set(dummyId, r);

        augmentedEdges.push({
          source: { nodeId: prevId, portId: prevId === sId ? edge.source?.portId : "out" },
          target: { nodeId: dummyId, portId: "in" },
          originalEdge: edge,
        });
        prevId = dummyId;
      }
      augmentedEdges.push({
        source: { nodeId: prevId, portId: "out" },
        target: { nodeId: tId, portId: edge.target?.portId },
        originalEdge: edge,
      });
    } else {
      augmentedEdges.push(edge);
    }
  });

  return { allNodesMap, augmentedEdges };
}

/**
 * 启发式两层交叉数计算
 */
function countCrossingsBetweenLayers(layer1, layer2, edgesMap) {
  const posMap2 = new Map(layer2.map((n, idx) => [n.id, idx]));
  const connectedEdges = [];

  layer1.forEach((n1, idx1) => {
    const outs = edgesMap.out.get(n1.id) || [];
    outs.forEach((n2Id) => {
      if (posMap2.has(n2Id)) {
        connectedEdges.push({
          u: idx1,
          v: posMap2.get(n2Id),
        });
      }
    });
  });

  let crossings = 0;
  for (let i = 0; i < connectedEdges.length; i++) {
    for (let j = i + 1; j < connectedEdges.length; j++) {
      const e1 = connectedEdges[i];
      const e2 = connectedEdges[j];
      if ((e1.u < e2.u && e1.v > e2.v) || (e1.u > e2.u && e1.v < e2.v)) {
        crossings++;
      }
    }
  }
  return crossings;
}

/**
 * 计算整个分层图的总连线交叉数
 */
function countTotalCrossings(layers, edgesMap) {
  let total = 0;
  for (let l = 0; l < layers.length - 1; l++) {
    total += countCrossingsBetweenLayers(layers[l], layers[l + 1], edgesMap);
  }
  return total;
}

/**
 * 多轮重心与端口加权启发式排序（Sugiyama Crossing Minimization）
 * 保持走向流分支有序，从源头消解交叉
 */
function orderLayersCrossingMinimized(layers, allNodesMap, augmentedEdges, maxPasses = 10) {
  const edgesMap = {
    out: new Map(),
    in: new Map(),
    edgeDetails: new Map(),
  };

  allNodesMap.forEach((_, id) => {
    edgesMap.out.set(id, []);
    edgesMap.in.set(id, []);
  });

  augmentedEdges.forEach((edge) => {
    const sId = edge.source?.nodeId;
    const tId = edge.target?.nodeId;
    if (sId && tId && edgesMap.out.has(sId) && edgesMap.in.has(tId)) {
      edgesMap.out.get(sId).push(tId);
      edgesMap.in.get(tId).push(sId);
      edgesMap.edgeDetails.set(`${sId}->${tId}`, edge);
    }
  });

  for (let l = 1; l < layers.length; l++) {
    const prevLayer = layers[l - 1];
    const prevPosMap = new Map(prevLayer.map((n, idx) => [n.id, idx]));

    layers[l].sort((a, b) => {
      const inA = edgesMap.in.get(a.id) || [];
      const inB = edgesMap.in.get(b.id) || [];

      function computeWeight(node, parentIds) {
        if (parentIds.length === 0) return 0;
        let sum = 0;
        parentIds.forEach((pId) => {
          const pPos = prevPosMap.get(pId) ?? 0;
          const edge = edgesMap.edgeDetails.get(`${pId}->${node.id}`);
          const pNode = allNodesMap.get(pId);
          const portOrder = pNode ? getPortOrderIndex(pNode, edge?.source?.portId) : 0;
          sum += pPos + portOrder * 0.15;
        });
        return sum / parentIds.length;
      }

      const wA = computeWeight(a, inA);
      const wB = computeWeight(b, inB);
      return wA - wB;
    });
  }

  let bestLayers = layers.map((l) => [...l]);
  let minCrossings = countTotalCrossings(bestLayers, edgesMap);

  for (let pass = 0; pass < maxPasses && minCrossings > 0; pass++) {
    for (let l = 1; l < layers.length; l++) {
      const prevLayer = layers[l - 1];
      const prevPosMap = new Map(prevLayer.map((n, idx) => [n.id, idx]));

      layers[l].sort((a, b) => {
        const inA = edgesMap.in.get(a.id) || [];
        const inB = edgesMap.in.get(b.id) || [];

        function calcBarycenter(node, pIds) {
          if (pIds.length === 0) return prevLayer.length / 2;
          let sum = 0;
          pIds.forEach((pId) => {
            const pPos = prevPosMap.get(pId) ?? (prevLayer.length / 2);
            const edge = edgesMap.edgeDetails.get(`${pId}->${node.id}`);
            const pNode = allNodesMap.get(pId);
            const portOrder = pNode ? getPortOrderIndex(pNode, edge?.source?.portId) : 0;
            sum += pPos + portOrder * 0.18;
          });
          return sum / pIds.length;
        }

        return calcBarycenter(a, inA) - calcBarycenter(b, inB);
      });
    }

    for (let l = layers.length - 2; l >= 0; l--) {
      const nextLayer = layers[l + 1];
      const nextPosMap = new Map(nextLayer.map((n, idx) => [n.id, idx]));

      layers[l].sort((a, b) => {
        const outA = edgesMap.out.get(a.id) || [];
        const outB = edgesMap.out.get(b.id) || [];

        function calcBarycenter(node, cIds) {
          if (cIds.length === 0) return nextLayer.length / 2;
          let sum = 0;
          cIds.forEach((cId) => {
            const cPos = nextPosMap.get(cId) ?? (nextLayer.length / 2);
            const edge = edgesMap.edgeDetails.get(`${node.id}->${cId}`);
            const pNode = allNodesMap.get(node.id);
            const portOrder = pNode ? getPortOrderIndex(pNode, edge?.source?.portId) : 0;
            sum += cPos - portOrder * 0.18;
          });
          return sum / cIds.length;
        }

        return calcBarycenter(a, outA) - calcBarycenter(b, outB);
      });
    }

    for (let l = 0; l < layers.length; l++) {
      const layer = layers[l];
      for (let i = 0; i < layer.length - 1; i++) {
        const crossingsBefore = countTotalCrossings(layers, edgesMap);
        const tmp = layer[i];
        layer[i] = layer[i + 1];
        layer[i + 1] = tmp;

        const crossingsAfter = countTotalCrossings(layers, edgesMap);
        if (crossingsAfter < crossingsBefore) {
          if (crossingsAfter < minCrossings) {
            minCrossings = crossingsAfter;
            bestLayers = layers.map((lay) => [...lay]);
          }
        } else {
          const revert = layer[i];
          layer[i] = layer[i + 1];
          layer[i + 1] = revert;
        }
      }
    }

    const currentCrossings = countTotalCrossings(layers, edgesMap);
    if (currentCrossings < minCrossings) {
      minCrossings = currentCrossings;
      bestLayers = layers.map((l) => [...l]);
    }
  }

  return { orderedLayers: bestLayers, edgesMap };
}

/**
 * 水平布局坐标计算：从左到右有向无相交流动布局 (Left-to-Right Flow Alignment)
 * 核心机制：
 * 1. 主流程对齐：主流程（从 Start、Loop completed 到 End）严格保持水平基准线；
 * 2. 循环内部逻辑法线偏移：循环体内部逻辑（body 分支）在垂直法线方向（向上）偏移一段清晰距离并行排布；
 * 3. 刚性防重叠与多次力学松弛平滑。
 */
export function layoutLeftToRight(workflow, options = {}) {
  const nodes = workflow.nodes || [];
  const edges = workflow.edges || [];
  if (nodes.length === 0) return;

  const startX = options.startX ?? 80;
  const startY = options.startY ?? 80;
  const gapX = options.gapX ?? 100;
  const gapY = options.gapY ?? 40;

  const { nodeMap, forwardEdges, dagOut, dagIn } = buildAcyclicDag(nodes, edges);
  const ranks = computeRanks(workflow, nodes, dagOut, dagIn);
  const { allNodesMap, augmentedEdges } = insertDummyNodes(nodes, forwardEdges, ranks);

  const maxRank = Math.max(0, ...ranks.values());
  const rawLayers = Array.from({ length: maxRank + 1 }, () => []);
  allNodesMap.forEach((n) => {
    const r = ranks.get(n.id) || 0;
    rawLayers[r].push(n);
  });
  const activeLayers = rawLayers.filter((layer) => layer.length > 0);

  const { orderedLayers, edgesMap } = orderLayersCrossingMinimized(
    activeLayers,
    allNodesMap,
    augmentedEdges,
    12,
  );

  const { isLoopBodyNode } = getLoopBodyAnalysis(nodes, dagOut);

  let currentX = startX;
  const layerXMap = new Map();
  orderedLayers.forEach((layer, layerIdx) => {
    layerXMap.set(layerIdx, currentX);
    const realNodesInLayer = layer.filter((n) => !n.isDummy);
    const maxW =
      realNodesInLayer.length > 0
        ? Math.max(...realNodesInLayer.map((n) => getNodeDimensions(n).width))
        : 80;
    currentX += maxW + gapX;
  });

  function getNodeHeight(n) {
    if (n.isDummy) return 20;
    return getNodeDimensions(n).height;
  }

  const layerTotalHeights = orderedLayers.map((layer) => {
    const sumH = layer.reduce((sum, n) => sum + getNodeHeight(n), 0);
    return sumH + Math.max(0, layer.length - 1) * gapY;
  });
  const maxTotalH = Math.max(...layerTotalHeights, 240);
  const centralY = startY + maxTotalH / 2;

  const layer0 = orderedLayers[0];
  let y0Cursor = centralY - layerTotalHeights[0] / 2;
  layer0.forEach((node) => {
    const h = getNodeHeight(node);
    node.layout = {
      x: layerXMap.get(0),
      y: y0Cursor,
    };
    y0Cursor += h + gapY;
  });

  for (let l = 1; l < orderedLayers.length; l++) {
    const layer = orderedLayers[l];

    layer.forEach((node) => {
      const parentIds = edgesMap.in.get(node.id) || [];
      const validParents = parentIds
        .map((pId) => allNodesMap.get(pId))
        .filter((p) => p && p.layout);

      let idealCenterY;
      if (validParents.length === 1) {
        const p = validParents[0];
        const pH = getNodeHeight(p);
        const edge = edgesMap.edgeDetails.get(`${p.id}->${node.id}`);
        const isFromLoop = LOOP_HEADER_TYPES.includes(p.type);
        const isBodyPort = edge?.source?.portId === "body";
        const isCompletedPort = edge?.source?.portId === "completed";
        const isFailurePort = edge?.source?.portId === "failure";

        const pOutEdges = edgesMap.out.get(p.id) || [];
        const isMultiBranch = pOutEdges.length > 1;

        if (isFromLoop && isBodyPort) {
          // 循环内部逻辑：垂直向上（法线正向）
          const normalOffset = pH + gapY + 20;
          idealCenterY = p.layout.y + pH / 2 - normalOffset;
        } else if (isFromLoop && isCompletedPort) {
          // 主流程分支 (completed)：沿主流程严格水平直线对齐
          idealCenterY = p.layout.y + pH / 2;
        } else if (isFromLoop && isFailurePort) {
          // 循环失败分支：垂直向下（法线反向）
          const normalOffset = pH + gapY + 20;
          idealCenterY = p.layout.y + pH / 2 + normalOffset;
        } else if (isMultiBranch) {
          // 多路分支（如 Condition / Switch）：沿源主流程中心向上下两个相反方向展开
          const portOrder = getPortOrderIndex(p, edge?.source?.portId);
          const totalOut = pOutEdges.length;
          const branchOffset = pH + gapY;
          if (totalOut === 2) {
            // 两个分支：分别向上、向下对立排布
            idealCenterY = portOrder === 0
              ? p.layout.y + pH / 2 - branchOffset * 0.75
              : p.layout.y + pH / 2 + branchOffset * 0.75;
          } else {
            const normalizedIdx = portOrder - (totalOut - 1) / 2;
            idealCenterY = p.layout.y + pH / 2 + normalizedIdx * branchOffset;
          }
        } else {
          const pRelY = p.isDummy
            ? 0.5
            : getPortRelativeOffset(p, edge?.source?.portId, "out");
          idealCenterY = p.layout.y + pH * pRelY;
        }
      } else if (validParents.length > 1) {
        idealCenterY =
          validParents.reduce((sum, p) => sum + (p.layout.y + getNodeHeight(p) / 2), 0) /
          validParents.length;
      } else {
        idealCenterY = centralY;
      }

      const h = getNodeHeight(node);
      node.layout = {
        x: layerXMap.get(l),
        y: idealCenterY - h / 2,
      };
    });

    // 刚性防重叠保证：自上向下扫描并推移
    for (let i = 1; i < layer.length; i++) {
      const prev = layer[i - 1];
      const prevH = getNodeHeight(prev);
      const curr = layer[i];
      const minY = prev.layout.y + prevH + gapY;
      if (curr.layout.y < minY) {
        curr.layout.y = minY;
      }
    }

    // 局部整体微调：向父节点群体中心拉回
    const allParentCenters = layer
      .flatMap((n) => edgesMap.in.get(n.id) || [])
      .map((pId) => allNodesMap.get(pId))
      .filter((p) => p && p.layout)
      .map((p) => p.layout.y + getNodeHeight(p) / 2);

    if (allParentCenters.length > 0) {
      const targetCenter =
        allParentCenters.reduce((sum, y) => sum + y, 0) / allParentCenters.length;
      const actualCenter =
        (layer[0].layout.y + layer[layer.length - 1].layout.y + getNodeHeight(layer[layer.length - 1])) / 2;
      const shift = targetCenter - actualCenter;
      layer.forEach((n) => (n.layout.y += shift));
    }
  }

  // 8. 反向与正向力学平滑
  for (let pass = 0; pass < 6; pass++) {
    for (let l = orderedLayers.length - 2; l >= 0; l--) {
      const layer = orderedLayers[l];
      layer.forEach((node) => {
        const childIds = edgesMap.out.get(node.id) || [];
        const validChildren = childIds
          .map((cId) => allNodesMap.get(cId))
          .filter((c) => c && c.layout);

        if (validChildren.length > 0) {
          const avgChildY =
            validChildren.reduce((sum, c) => sum + (c.layout.y + getNodeHeight(c) / 2), 0) /
            validChildren.length;
          const h = getNodeHeight(node);
          node.layout.y = avgChildY - h / 2;
        }
      });

      for (let i = 1; i < layer.length; i++) {
        const prev = layer[i - 1];
        const prevH = getNodeHeight(prev);
        const curr = layer[i];
        const minY = prev.layout.y + prevH + gapY;
        if (curr.layout.y < minY) {
          curr.layout.y = minY;
        }
      }
    }

    for (let l = 1; l < orderedLayers.length; l++) {
      const layer = orderedLayers[l];
      layer.forEach((node) => {
        const parentIds = edgesMap.in.get(node.id) || [];
        const validParents = parentIds
          .map((pId) => allNodesMap.get(pId))
          .filter((p) => p && p.layout);

        if (validParents.length > 0) {
          const avgParentY =
            validParents.reduce((sum, p) => sum + (p.layout.y + getNodeHeight(p) / 2), 0) /
            validParents.length;
          const h = getNodeHeight(node);
          node.layout.y = avgParentY - h / 2;
        }
      });

      for (let i = 1; i < layer.length; i++) {
        const prev = layer[i - 1];
        const prevH = getNodeHeight(prev);
        const curr = layer[i];
        const minY = prev.layout.y + prevH + gapY;
        if (curr.layout.y < minY) {
          curr.layout.y = minY;
        }
      }
    }
  }

  // 9. 将计算好的真实节点坐标写回 workflow.nodes
  nodes.forEach((node) => {
    const calculated = allNodesMap.get(node.id);
    if (calculated && calculated.layout) {
      node.layout = {
        x: Math.round(calculated.layout.x),
        y: Math.round(calculated.layout.y),
      };
    }
  });

  // 10. 全局标准化
  const minY = Math.min(...nodes.map((n) => n.layout?.y ?? startY));
  if (minY < startY) {
    const offsetY = startY - minY;
    nodes.forEach((n) => {
      if (n.layout) n.layout.y += offsetY;
    });
  }
  const minX = Math.min(...nodes.map((n) => n.layout?.x ?? startX));
  if (minX < startX) {
    const offsetX = startX - minX;
    nodes.forEach((n) => {
      if (n.layout) n.layout.x += offsetX;
    });
  }
}

/**
 * 垂直布局坐标计算：从上到下有向无相交流动布局 (Top-to-Bottom Flow Alignment)
 * 核心机制：
 * 1. 主流程垂直拉直：主流程（从 Start、Loop completed 到 End）严格保持垂直基准线；
 * 2. 循环内部逻辑法线偏移：循环体内部逻辑（body 分支）在水平法线方向（向右）偏移一段清晰距离并行排布；
 * 3. 刚性防重叠与多次力学松弛平滑。
 */
export function layoutTopToBottom(workflow, options = {}) {
  const nodes = workflow.nodes || [];
  const edges = workflow.edges || [];
  if (nodes.length === 0) return;

  const startX = options.startX ?? 80;
  const startY = options.startY ?? 80;
  const gapX = options.gapX ?? 60;
  const gapY = options.gapY ?? 90;

  const { nodeMap, forwardEdges, dagOut, dagIn } = buildAcyclicDag(nodes, edges);
  const ranks = computeRanks(workflow, nodes, dagOut, dagIn);
  const { allNodesMap, augmentedEdges } = insertDummyNodes(nodes, forwardEdges, ranks);

  const maxRank = Math.max(0, ...ranks.values());
  const rawLayers = Array.from({ length: maxRank + 1 }, () => []);
  allNodesMap.forEach((n) => {
    const r = ranks.get(n.id) || 0;
    rawLayers[r].push(n);
  });
  const activeLayers = rawLayers.filter((layer) => layer.length > 0);

  const { orderedLayers, edgesMap } = orderLayersCrossingMinimized(
    activeLayers,
    allNodesMap,
    augmentedEdges,
    12,
  );

  const { isLoopBodyNode } = getLoopBodyAnalysis(nodes, dagOut);

  let currentY = startY;
  const layerYMap = new Map();
  orderedLayers.forEach((layer, layerIdx) => {
    layerYMap.set(layerIdx, currentY);
    const realNodesInLayer = layer.filter((n) => !n.isDummy);
    const maxH =
      realNodesInLayer.length > 0
        ? Math.max(...realNodesInLayer.map((n) => getNodeDimensions(n).height))
        : 60;
    currentY += maxH + gapY;
  });

  function getNodeWidth(n) {
    if (n.isDummy) return 20;
    return getNodeDimensions(n).width;
  }

  const layerTotalWidths = orderedLayers.map((layer) => {
    const sumW = layer.reduce((sum, n) => sum + getNodeWidth(n), 0);
    return sumW + Math.max(0, layer.length - 1) * gapX;
  });
  const maxTotalW = Math.max(...layerTotalWidths, 240);
  const centralX = startX + maxTotalW / 2;

  const layer0 = orderedLayers[0];
  let x0Cursor = centralX - layerTotalWidths[0] / 2;
  layer0.forEach((node) => {
    const w = getNodeWidth(node);
    node.layout = {
      x: x0Cursor,
      y: layerYMap.get(0),
    };
    x0Cursor += w + gapX;
  });

  for (let l = 1; l < orderedLayers.length; l++) {
    const layer = orderedLayers[l];

    layer.forEach((node) => {
      const parentIds = edgesMap.in.get(node.id) || [];
      const validParents = parentIds
        .map((pId) => allNodesMap.get(pId))
        .filter((p) => p && p.layout);

      let idealCenterX;
      if (validParents.length === 1) {
        const p = validParents[0];
        const pW = getNodeWidth(p);
        const edge = edgesMap.edgeDetails.get(`${p.id}->${node.id}`);
        const isFromLoop = LOOP_HEADER_TYPES.includes(p.type);
        const isBodyPort = edge?.source?.portId === "body";
        const isCompletedPort = edge?.source?.portId === "completed";
        const isFailurePort = edge?.source?.portId === "failure";

        const pOutEdges = edgesMap.out.get(p.id) || [];
        const isMultiBranch = pOutEdges.length > 1;

        if (isFromLoop && isBodyPort) {
          // 循环内部逻辑：水平向右（法线正向）
          const normalOffset = pW + gapX + 30;
          idealCenterX = p.layout.x + pW / 2 + normalOffset;
        } else if (isFromLoop && isCompletedPort) {
          // 主流程分支 (completed)：沿主流程垂直直线对齐
          idealCenterX = p.layout.x + pW / 2;
        } else if (isFromLoop && isFailurePort) {
          // 失败分支：水平向左（法线反向）
          const normalOffset = pW + gapX + 30;
          idealCenterX = p.layout.x + pW / 2 - normalOffset;
        } else if (isMultiBranch) {
          // 多路分支（如 Condition / Switch）：向左右两个相反方向展开
          const portOrder = getPortOrderIndex(p, edge?.source?.portId);
          const totalOut = pOutEdges.length;
          const branchOffset = pW + gapX;
          if (totalOut === 2) {
            idealCenterX = portOrder === 0
              ? p.layout.x + pW / 2 + branchOffset * 0.75
              : p.layout.x + pW / 2 - branchOffset * 0.75;
          } else {
            const normalizedIdx = portOrder - (totalOut - 1) / 2;
            idealCenterX = p.layout.x + pW / 2 + normalizedIdx * branchOffset;
          }
        } else {
          idealCenterX = p.layout.x + pW / 2;
        }
      } else if (validParents.length > 1) {
        idealCenterX =
          validParents.reduce((sum, p) => sum + (p.layout.x + getNodeWidth(p) / 2), 0) /
          validParents.length;
      } else {
        idealCenterX = centralX;
      }

      const w = getNodeWidth(node);
      node.layout = {
        x: idealCenterX - w / 2,
        y: layerYMap.get(l),
      };
    });

    for (let i = 1; i < layer.length; i++) {
      const prev = layer[i - 1];
      const prevW = getNodeWidth(prev);
      const curr = layer[i];
      const minX = prev.layout.x + prevW + gapX;
      if (curr.layout.x < minX) {
        curr.layout.x = minX;
      }
    }

    const allParentCenters = layer
      .flatMap((n) => edgesMap.in.get(n.id) || [])
      .map((pId) => allNodesMap.get(pId))
      .filter((p) => p && p.layout)
      .map((p) => p.layout.x + getNodeWidth(p) / 2);

    if (allParentCenters.length > 0) {
      const targetCenter =
        allParentCenters.reduce((sum, x) => sum + x, 0) / allParentCenters.length;
      const actualCenter =
        (layer[0].layout.x + layer[layer.length - 1].layout.x + getNodeWidth(layer[layer.length - 1])) / 2;
      const shift = targetCenter - actualCenter;
      layer.forEach((n) => (n.layout.x += shift));
    }
  }

  for (let pass = 0; pass < 6; pass++) {
    for (let l = orderedLayers.length - 2; l >= 0; l--) {
      const layer = orderedLayers[l];
      layer.forEach((node) => {
        const childIds = edgesMap.out.get(node.id) || [];
        const validChildren = childIds
          .map((cId) => allNodesMap.get(cId))
          .filter((c) => c && c.layout);

        if (validChildren.length > 0) {
          const avgChildX =
            validChildren.reduce((sum, c) => sum + (c.layout.x + getNodeWidth(c) / 2), 0) /
            validChildren.length;
          const w = getNodeWidth(node);
          node.layout.x = avgChildX - w / 2;
        }
      });

      for (let i = 1; i < layer.length; i++) {
        const prev = layer[i - 1];
        const prevW = getNodeWidth(prev);
        const curr = layer[i];
        const minX = prev.layout.x + prevW + gapX;
        if (curr.layout.x < minX) {
          curr.layout.x = minX;
        }
      }
    }

    for (let l = 1; l < orderedLayers.length; l++) {
      const layer = orderedLayers[l];
      layer.forEach((node) => {
        const parentIds = edgesMap.in.get(node.id) || [];
        const validParents = parentIds
          .map((pId) => allNodesMap.get(pId))
          .filter((p) => p && p.layout);

        if (validParents.length > 0) {
          const avgParentX =
            validParents.reduce((sum, p) => sum + (p.layout.x + getNodeWidth(p) / 2), 0) /
            validParents.length;
          const w = getNodeWidth(node);
          node.layout.x = avgParentX - w / 2;
        }
      });

      for (let i = 1; i < layer.length; i++) {
        const prev = layer[i - 1];
        const prevW = getNodeWidth(prev);
        const curr = layer[i];
        const minX = prev.layout.x + prevW + gapX;
        if (curr.layout.x < minX) {
          curr.layout.x = minX;
        }
      }
    }
  }

  // 写回真实节点坐标
  nodes.forEach((node) => {
    const calculated = allNodesMap.get(node.id);
    if (calculated && calculated.layout) {
      node.layout = {
        x: Math.round(calculated.layout.x),
        y: Math.round(calculated.layout.y),
      };
    }
  });

  // 全局标准化
  const minX = Math.min(...nodes.map((n) => n.layout?.x ?? startX));
  if (minX < startX) {
    const offsetX = startX - minX;
    nodes.forEach((n) => {
      if (n.layout) n.layout.x += offsetX;
    });
  }
  const minY = Math.min(...nodes.map((n) => n.layout?.y ?? startY));
  if (minY < startY) {
    const offsetY = startY - minY;
    nodes.forEach((n) => {
      if (n.layout) n.layout.y += offsetY;
    });
  }
}

/**
 * 紧凑布局：高密度 2D 拓扑矩阵折行流布局 (Compact 2D Matrix Wrapping Layout)
 * 将长线性或多分支 DAG 图折叠为固定列数 (2~4 列) 的紧凑 2D 方阵，
 * 节点在有限的可视区域内高密度聚集，避免向单一水平方向无限延伸。
 */
export function layoutCompact(workflow, options = {}) {
  const nodes = workflow.nodes || [];
  const edges = workflow.edges || [];
  if (nodes.length === 0) return;

  const startX = options.startX ?? 80;
  const startY = options.startY ?? 80;
  const gapX = options.gapX ?? 48;
  const gapY = options.gapY ?? 20;
  const rowGap = options.rowGap ?? 56;

  // 1. 构建无环 DAG
  const { forwardEdges, dagOut, dagIn } = buildAcyclicDag(nodes, edges);

  // 2. 拓扑分层 (ASAP)
  const ranks = computeRanks(workflow, nodes, dagOut, dagIn);

  // 3. 紧凑化沉降与松弛压缩 (Sink-Pulling / Slack Reduction)
  const sortedNodes = [...nodes].sort(
    (a, b) => (ranks.get(b.id) ?? 0) - (ranks.get(a.id) ?? 0),
  );
  sortedNodes.forEach((node) => {
    if (node.id === workflow.entryNodeId) return;
    const outs = dagOut.get(node.id) || [];
    if (outs.length > 0) {
      const minChildRank = Math.min(...outs.map((o) => ranks.get(o.targetId) ?? Infinity));
      if (Number.isFinite(minChildRank)) {
        const ins = dagIn.get(node.id) || [];
        const maxParentRank =
          ins.length > 0
            ? Math.max(...ins.map((i) => ranks.get(i.sourceId) ?? -Infinity))
            : -1;
        const targetRank = Math.min(minChildRank - 1, Math.max(ranks.get(node.id) ?? 0, maxParentRank + 1));
        if (targetRank >= (ranks.get(node.id) ?? 0)) {
          ranks.set(node.id, targetRank);
        }
      }
    }
  });

  // 再次确保循环体拓扑聚合
  clusterLoopRanks(workflow, nodes, ranks, dagOut, dagIn);

  // 4. 压缩空层
  const usedRanks = Array.from(new Set(Array.from(ranks.values()))).sort((a, b) => a - b);
  const rankRemap = new Map(usedRanks.map((r, idx) => [r, idx]));
  nodes.forEach((n) => ranks.set(n.id, rankRemap.get(ranks.get(n.id)) ?? 0));

  // 5. 插入跨层虚拟走线节点
  const { allNodesMap, augmentedEdges } = insertDummyNodes(nodes, forwardEdges, ranks);

  // 6. 按层分组
  const maxRank = Math.max(0, ...ranks.values());
  const rawLayers = Array.from({ length: maxRank + 1 }, () => []);
  allNodesMap.forEach((n) => {
    const r = ranks.get(n.id) || 0;
    rawLayers[r].push(n);
  });
  const activeLayers = rawLayers.filter((layer) => layer.length > 0);

  // 7. 交叉最小化排序 (Crossing Minimization)
  const { orderedLayers, edgesMap } = orderLayersCrossingMinimized(
    activeLayers,
    allNodesMap,
    augmentedEdges,
    10,
  );

  // 8. 计算 2D 紧凑矩阵的列数 (Column Count)
  const totalLayers = orderedLayers.length;
  let columnCount = 3;
  if (totalLayers <= 3) {
    columnCount = totalLayers;
  } else if (nodes.length <= 6) {
    columnCount = Math.min(3, totalLayers);
  } else if (nodes.length <= 12) {
    columnCount = Math.min(4, Math.max(3, Math.ceil(Math.sqrt(nodes.length * 1.2))));
  } else {
    columnCount = Math.min(4, Math.max(3, Math.ceil(Math.sqrt(nodes.length * 1.4))));
  }

  const rowCount = Math.ceil(totalLayers / columnCount);

  // 9. 将各层真实节点分配至 2D 网格单元 (row, col)
  const grid = Array.from({ length: rowCount }, () =>
    Array.from({ length: columnCount }, () => []),
  );

  orderedLayers.forEach((layer, layerIdx) => {
    const r = Math.floor(layerIdx / columnCount);
    const c = layerIdx % columnCount;
    const realNodes = layer.filter((n) => !n.isDummy);
    grid[r][c] = realNodes;
  });

  // 10. 计算各列最大宽度与各行最大高度
  const colWidths = Array.from({ length: columnCount }, () => 240);
  const rowHeights = Array.from({ length: rowCount }, () => 140);

  for (let r = 0; r < rowCount; r++) {
    let maxCellHeightInRow = 140;
    for (let c = 0; c < columnCount; c++) {
      const cellNodes = grid[r][c];
      if (cellNodes.length > 0) {
        cellNodes.forEach((node) => {
          const dim = getNodeDimensions(node);
          colWidths[c] = Math.max(colWidths[c], dim.width);
        });
        const cellH =
          cellNodes.reduce((sum, n) => sum + getNodeDimensions(n).height, 0) +
          Math.max(0, cellNodes.length - 1) * gapY;
        maxCellHeightInRow = Math.max(maxCellHeightInRow, cellH);
      }
    }
    rowHeights[r] = maxCellHeightInRow;
  }

  // 11. 计算各列 X 起点与各行 Y 起点
  const colXPositions = [];
  let xCursor = startX;
  for (let c = 0; c < columnCount; c++) {
    colXPositions.push(xCursor);
    xCursor += colWidths[c] + gapX;
  }

  const rowYPositions = [];
  let yCursor = startY;
  for (let r = 0; r < rowCount; r++) {
    rowYPositions.push(yCursor);
    yCursor += rowHeights[r] + rowGap;
  }

  // 12. 节点坐标初始赋值（分支沿法线对立展开）
  for (let r = 0; r < rowCount; r++) {
    for (let c = 0; c < columnCount; c++) {
      const cellNodes = grid[r][c];
      if (cellNodes.length === 0) continue;

      const cellTotalHeight =
        cellNodes.reduce((sum, n) => sum + getNodeDimensions(n).height, 0) +
        Math.max(0, cellNodes.length - 1) * gapY;
      let cellYCursor = rowYPositions[r] + (rowHeights[r] - cellTotalHeight) / 2;

      cellNodes.forEach((node) => {
        const dim = getNodeDimensions(node);
        node.layout = {
          x: colXPositions[c],
          y: cellYCursor,
        };
        cellYCursor += dim.height + gapY;
      });
    }
  }

  // 13. 行内直线对齐与双向分支展开微调
  for (let r = 0; r < rowCount; r++) {
    for (let c = 1; c < columnCount; c++) {
      const cellNodes = grid[r][c];
      const prevCellNodes = grid[r][c - 1];
      if (cellNodes.length === 1 && prevCellNodes.length === 1) {
        const curr = cellNodes[0];
        const prev = prevCellNodes[0];
        const hasEdge = edges.some(
          (e) => e.source?.nodeId === prev.id && e.target?.nodeId === curr.id,
        );
        if (hasEdge) {
          const edge = edges.find(
            (e) => e.source?.nodeId === prev.id && e.target?.nodeId === curr.id,
          );
          const isFromLoop = LOOP_HEADER_TYPES.includes(prev.type);
          const isBodyPort = edge?.source?.portId === "body";
          const isCompletedPort = edge?.source?.portId === "completed";
          const isFailurePort = edge?.source?.portId === "failure";

          const prevDim = getNodeDimensions(prev);
          const currDim = getNodeDimensions(curr);
          let targetY;

          if (isFromLoop && isBodyPort) {
            targetY = prev.layout.y - (currDim.height + gapY);
          } else if (isFromLoop && isCompletedPort) {
            targetY = prev.layout.y + (prevDim.height - currDim.height) / 2;
          } else if (isFromLoop && isFailurePort) {
            targetY = prev.layout.y + prevDim.height + gapY;
          } else {
            const pRelY = getPortRelativeOffset(prev, edge?.source?.portId, "out");
            targetY = prev.layout.y + prevDim.height * pRelY - currDim.height / 2;
          }

          if (targetY >= rowYPositions[r] && targetY + currDim.height <= rowYPositions[r] + rowHeights[r]) {
            curr.layout.y = targetY;
          }
        }
      }
    }
  }

  // 14. 刚性防重叠扫描与全局标准化
  for (let r = 0; r < rowCount; r++) {
    for (let c = 0; c < columnCount; c++) {
      const cellNodes = grid[r][c];
      for (let i = 1; i < cellNodes.length; i++) {
        const prev = cellNodes[i - 1];
        const curr = cellNodes[i];
        const prevDim = getNodeDimensions(prev);
        const minY = prev.layout.y + prevDim.height + gapY;
        if (curr.layout.y < minY) {
          curr.layout.y = minY;
        }
      }
    }
  }

  // 15. 将计算出的真实节点坐标写回 workflow.nodes
  nodes.forEach((node) => {
    const calculated = allNodesMap.get(node.id);
    if (calculated && calculated.layout) {
      node.layout = {
        x: Math.round(calculated.layout.x),
        y: Math.round(calculated.layout.y),
      };
    } else if (!node.layout) {
      node.layout = { x: startX, y: startY };
    }
  });

  const minY = Math.min(...nodes.map((n) => n.layout?.y ?? startY));
  if (minY < startY) {
    const offsetY = startY - minY;
    nodes.forEach((n) => {
      if (n.layout) n.layout.y += offsetY;
    });
  }
  const minX = Math.min(...nodes.map((n) => n.layout?.x ?? startX));
  if (minX < startX) {
    const offsetX = startX - minX;
    nodes.forEach((n) => {
      if (n.layout) n.layout.x += offsetX;
    });
  }
}

// 注册内置算法
registerLayoutAlgorithm(DEFAULT_LAYOUT_ID, "从左到右 (LR)", layoutLeftToRight);
registerLayoutAlgorithm("dag-tb", "从上到下 (TB)", layoutTopToBottom);
registerLayoutAlgorithm(COMPACT_LAYOUT_ID, "紧凑布局", layoutCompact);

/**
 * 按照指定算法（或全局默认方向）执行自动排版。
 * @param {Object} workflow 工作流对象
 * @param {string|null} algorithmId 布局算法 ID，默认读取当前全局配置
 * @returns {boolean} 是否成功执行
 */
export function executeAutoLayout(workflow, algorithmId = null) {
  const algoId = algorithmId || DEFAULT_LAYOUT_ID;
  const entry = layoutAlgorithms.get(algoId) || layoutAlgorithms.get(DEFAULT_LAYOUT_ID);
  if (entry?.layout) {
    entry.layout(workflow);
    return true;
  }
  return false;
}

/**
 * 检查工作流节点是否需要自动布局，并在必要时执行。
 * @param {Object} workflow 工作流对象
 * @param {boolean} force 是否强制重新布局
 * @param {string} algorithmId 布局算法 ID，默认 'dag-lr'
 * @returns {boolean} 是否执行了布局
 */
export function ensureAutoLayout(workflow, force = false, algorithmId = DEFAULT_LAYOUT_ID) {
  if (!workflow || !Array.isArray(workflow.nodes) || workflow.nodes.length === 0) {
    return false;
  }

  const nodes = workflow.nodes;
  const needsLayout =
    force ||
    nodes.some(
      (n) =>
        !n.layout ||
        typeof n.layout.x !== "number" ||
        typeof n.layout.y !== "number" ||
        isNaN(n.layout.x) ||
        isNaN(n.layout.y),
    ) ||
    (nodes.length > 1 &&
      nodes.every(
        (n) =>
          (n.layout?.x === 0 || n.layout?.x === undefined) &&
          (n.layout?.y === 0 || n.layout?.y === undefined),
      ));

  if (!needsLayout) {
    return false;
  }

  return executeAutoLayout(workflow, algorithmId);
}
