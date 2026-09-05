import { EditorConfig } from "../config.js";
import { specForNode } from "../state.js";

/**
 * 布局算法注册表，支持未来扩展自定义算法（如横向、纵向、网格、力导向等）。
 */
export const layoutAlgorithms = new Map();

const DEFAULT_LAYOUT_ID = "dag-lr";
const COMPACT_LAYOUT_ID = "dag-compact";
const DEFAULT_ORDERING_PASSES = 1;
const COMPACT_ORDERING_PASSES = 4;
const COMPACT_HORIZONTAL_GAP = 56;
const COMPACT_VERTICAL_GAP = 20;
const COMPACT_MINIMUM_COLUMNS = 2;
const COMPACT_DENSITY_FACTOR = 1.5;

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
 * 构建消除后向边（循环）的有向无环图 (DAG)，防止死循环及层级爆炸。
 */
function buildDagGraph(nodes, edges) {
  const nodeMap = new Map(nodes.map((n) => [n.id, n]));
  const adj = new Map();
  nodes.forEach((n) => adj.set(n.id, []));

  edges.forEach((e) => {
    const sId = e.source?.nodeId;
    const tId = e.target?.nodeId;
    if (sId && tId && nodeMap.has(sId) && nodeMap.has(tId) && sId !== tId) {
      adj.get(sId).push(tId);
    }
  });

  // DFS 检测环并过滤掉后向边
  const visited = new Set();
  const visiting = new Set();
  const dagOutEdges = new Map();
  const dagInEdges = new Map();
  nodes.forEach((n) => {
    dagOutEdges.set(n.id, new Set());
    dagInEdges.set(n.id, new Set());
  });

  function dfs(u) {
    visited.add(u);
    visiting.add(u);
    for (const v of adj.get(u)) {
      if (visiting.has(v)) {
        // 后向边（形成环），跳过此边
        continue;
      }
      dagOutEdges.get(u).add(v);
      dagInEdges.get(v).add(u);
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

  return { nodeMap, dagOutEdges, dagInEdges };
}

/**
 * 分配拓扑层级 (Rank Assignment)
 */
function assignRanks(workflow, nodes, dagOutEdges, dagInEdges) {
  const ranks = new Map();
  const entryId = workflow.entryNodeId;
  const rootIds = [];

  if (entryId && nodes.some((n) => n.id === entryId)) {
    rootIds.push(entryId);
  }
  nodes.forEach((n) => {
    if (dagInEdges.get(n.id).size === 0 && !rootIds.includes(n.id)) {
      rootIds.push(n.id);
    }
  });
  if (rootIds.length === 0 && nodes.length > 0) {
    rootIds.push(nodes[0].id);
  }

  rootIds.forEach((id) => ranks.set(id, 0));

  // 拓扑排序计算最长路径层级
  const inDegree = new Map();
  nodes.forEach((n) => inDegree.set(n.id, dagInEdges.get(n.id).size));
  const queue = [...rootIds];
  const processed = new Set();

  while (queue.length > 0) {
    const u = queue.shift();
    processed.add(u);
    const currRank = ranks.get(u) || 0;

    for (const v of dagOutEdges.get(u)) {
      const targetRank = Math.max(ranks.get(v) || 0, currRank + 1);
      ranks.set(v, targetRank);

      const remainingIn = inDegree.get(v) - 1;
      inDegree.set(v, remainingIn);
      if (remainingIn <= 0 && !processed.has(v)) {
        queue.push(v);
      }
    }
  }

  // 补全所有未覆盖的节点并继续松弛
  nodes.forEach((n) => {
    if (!ranks.has(n.id)) {
      ranks.set(n.id, 0);
      queue.push(n.id);
    }
  });
  while (queue.length > 0) {
    const u = queue.shift();
    const currRank = ranks.get(u) || 0;
    for (const v of dagOutEdges.get(u)) {
      const targetRank = Math.max(ranks.get(v) || 0, currRank + 1);
      if (!ranks.has(v) || ranks.get(v) < targetRank) {
        ranks.set(v, targetRank);
        queue.push(v);
      }
    }
  }

  return ranks;
}

/**
 * 重心法 (Barycenter) 排序以减少连线交叉。
 */
function orderLayers(activeLayers, dagInEdges, dagOutEdges) {
  // 正向遍历 (依据父节点平均位置对齐)
  for (let l = 1; l < activeLayers.length; l++) {
    const prevLayer = activeLayers[l - 1];
    const prevPosMap = new Map(prevLayer.map((node, idx) => [node.id, idx]));

    activeLayers[l].sort((a, b) => {
      const inA = [...dagInEdges.get(a.id)].filter((id) => prevPosMap.has(id));
      const inB = [...dagInEdges.get(b.id)].filter((id) => prevPosMap.has(id));
      const avgA =
        inA.length > 0
          ? inA.reduce((sum, id) => sum + prevPosMap.get(id), 0) / inA.length
          : prevLayer.length / 2;
      const avgB =
        inB.length > 0
          ? inB.reduce((sum, id) => sum + prevPosMap.get(id), 0) / inB.length
          : prevLayer.length / 2;
      return avgA - avgB;
    });
  }

  // 反向遍历 (依据子节点平均位置微调)
  for (let l = activeLayers.length - 2; l >= 0; l--) {
    const nextLayer = activeLayers[l + 1];
    const nextPosMap = new Map(nextLayer.map((node, idx) => [node.id, idx]));

    activeLayers[l].sort((a, b) => {
      const outA = [...dagOutEdges.get(a.id)].filter((id) => nextPosMap.has(id));
      const outB = [...dagOutEdges.get(b.id)].filter((id) => nextPosMap.has(id));
      const avgA =
        outA.length > 0
          ? outA.reduce((sum, id) => sum + nextPosMap.get(id), 0) / outA.length
          : nextLayer.length / 2;
      const avgB =
        outB.length > 0
          ? outB.reduce((sum, id) => sum + nextPosMap.get(id), 0) / outB.length
          : nextLayer.length / 2;
      return avgA - avgB;
    });
  }
}

/**
 * 水平布局算法：从左到右有向分层中心对称布局 (Left-to-Right Centered Hierarchical DAG Layout)
 * 特性：
 * 1. 依据拓扑深度向右延伸 (X 坐标按层级累加)；
 * 2. 每层节点严格以中心基准线 (Central Horizontal Axis) 对齐，父子节点垂直居中对称；
 * 3. 动态测量各节点高度，杜绝任何垂直重叠。
 */
export function layoutLeftToRight(workflow, options = {}) {
  const nodes = workflow.nodes || [];
  const edges = workflow.edges || [];
  if (nodes.length === 0) return;

  const startX = options.startX ?? 80;
  const startY = options.startY ?? 80;
  const gapX = options.gapX ?? 100;
  const gapY = options.gapY ?? 36;
  const orderingPasses = options.orderingPasses ?? DEFAULT_ORDERING_PASSES;

  // 1. 构建无环图与邻接表
  const { nodeMap, dagOutEdges, dagInEdges } = buildDagGraph(nodes, edges);

  // 2. 拓扑分层
  const ranks = assignRanks(workflow, nodes, dagOutEdges, dagInEdges);

  // 3. 将节点按层归类
  const maxRank = Math.max(0, ...ranks.values());
  const layers = Array.from({ length: maxRank + 1 }, () => []);
  nodes.forEach((n) => {
    const r = ranks.get(n.id);
    layers[r].push(n);
  });
  const activeLayers = layers.filter((layer) => layer.length > 0);

  // 4. 重心法减少连线交叉排序。紧凑布局会增加扫描轮次以获得更稳定的顺序。
  for (let pass = 0; pass < orderingPasses; pass += 1) {
    orderLayers(activeLayers, dagInEdges, dagOutEdges);
  }

  // 5. 坐标计算与中心对称排布
  // 计算各层总高度，找出最大跨度以确定中心水平基准线
  const layerHeights = activeLayers.map((layer) => {
    const heights = layer.map((n) => getNodeDimensions(n).height);
    return heights.reduce((sum, h) => sum + h, 0) + Math.max(0, layer.length - 1) * gapY;
  });
  const maxTotalHeight = Math.max(...layerHeights, 200);
  const centralY = startY + maxTotalHeight / 2;

  let currentX = startX;

  activeLayers.forEach((layer, layerIdx) => {
    const dimensions = layer.map((node) => getNodeDimensions(node));
    const maxLayerWidth = Math.max(...dimensions.map((d) => d.width), 240);
    const totalHeight = layerHeights[layerIdx];

    if (layerIdx === 0) {
      // 第 0 层整体以 centralY 为中心垂直居中排布
      let yCursor = centralY - totalHeight / 2;
      layer.forEach((node, idx) => {
        const dim = dimensions[idx];
        node.layout = {
          x: Math.round(currentX),
          y: Math.round(yCursor),
        };
        yCursor += dim.height + gapY;
      });
    } else {
      // 后续层优先对齐关联父节点的中心 Y
      layer.forEach((node, idx) => {
        const dim = dimensions[idx];
        const parentIds = [...dagInEdges.get(node.id)];
        const parents = parentIds
          .map((id) => nodeMap.get(id))
          .filter((p) => p && p.layout);

        let idealCenterY;
        if (parents.length > 0) {
          idealCenterY =
            parents.reduce((sum, p) => {
              const pDim = getNodeDimensions(p);
              return sum + (p.layout.y + pDim.height / 2);
            }, 0) / parents.length;
        } else {
          idealCenterY = centralY;
        }

        node.layout = {
          x: Math.round(currentX),
          y: Math.round(idealCenterY - dim.height / 2),
        };
      });

      // 消除同层重叠：按 ideal Y 升序排列并向下推移
      layer.sort((a, b) => a.layout.y - b.layout.y);
      for (let i = 1; i < layer.length; i++) {
        const prev = layer[i - 1];
        const prevDim = getNodeDimensions(prev);
        const curr = layer[i];
        const minY = prev.layout.y + prevDim.height + gapY;
        if (curr.layout.y < minY) {
          curr.layout.y = Math.round(minY);
        }
      }

      // 计算当前层实际包围盒中心，向父节点整体中心做平移微调居中
      const layerMinY = layer[0].layout.y;
      const lastNode = layer[layer.length - 1];
      const lastDim = getNodeDimensions(lastNode);
      const layerMaxY = lastNode.layout.y + lastDim.height;
      const actualCenterY = (layerMinY + layerMaxY) / 2;

      // 获取当前层所有父节点的平均中心
      const allParentNodes = layer
        .flatMap((n) => [...dagInEdges.get(n.id)])
        .map((id) => nodeMap.get(id))
        .filter((p) => p && p.layout);

      let targetCenter = centralY;
      if (allParentNodes.length > 0) {
        targetCenter =
          allParentNodes.reduce((sum, p) => {
            const pDim = getNodeDimensions(p);
            return sum + (p.layout.y + pDim.height / 2);
          }, 0) / allParentNodes.length;
      }

      const shiftY = Math.round(targetCenter - actualCenterY);
      if (Math.abs(shiftY) > 2) {
        layer.forEach((n) => {
          n.layout.y += shiftY;
        });
      }
    }

    currentX += maxLayerWidth + gapX;
  });

  // 6. 全局规范化：确保所有节点 Y >= startY, X >= startX
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
 * 垂直布局算法：从上到下有向分层中心对称布局 (Top-to-Bottom Centered Hierarchical DAG Layout)
 * 特性：
 * 1. 依据拓扑深度向下延伸 (Y 坐标按层级累加)；
 * 2. 每层节点严格以中心垂直基准线 (Central Vertical Axis) 对齐，父子节点水平居中对称；
 * 3. 动态测量各节点宽度与高度，杜绝任何重叠。
 */
export function layoutTopToBottom(workflow, options = {}) {
  const nodes = workflow.nodes || [];
  const edges = workflow.edges || [];
  if (nodes.length === 0) return;

  const startX = options.startX ?? 80;
  const startY = options.startY ?? 80;
  const gapX = options.gapX ?? 50;
  const gapY = options.gapY ?? 90;

  // 1. 构建无环图与邻接表
  const { nodeMap, dagOutEdges, dagInEdges } = buildDagGraph(nodes, edges);

  // 2. 拓扑分层
  const ranks = assignRanks(workflow, nodes, dagOutEdges, dagInEdges);

  // 3. 将节点按层归类
  const maxRank = Math.max(0, ...ranks.values());
  const layers = Array.from({ length: maxRank + 1 }, () => []);
  nodes.forEach((n) => {
    const r = ranks.get(n.id);
    layers[r].push(n);
  });
  const activeLayers = layers.filter((layer) => layer.length > 0);

  // 4. 重心法减少连线交叉排序 (在水平方向排序)
  orderLayers(activeLayers, dagInEdges, dagOutEdges);

  // 5. 坐标计算与中心对称排布
  // 计算各层总宽度，确定中心垂直基准线
  const layerWidths = activeLayers.map((layer) => {
    const widths = layer.map((n) => getNodeDimensions(n).width);
    return widths.reduce((sum, w) => sum + w, 0) + Math.max(0, layer.length - 1) * gapX;
  });
  const maxTotalWidth = Math.max(...layerWidths, 240);
  const centralX = startX + maxTotalWidth / 2;

  let currentY = startY;

  activeLayers.forEach((layer, layerIdx) => {
    const dimensions = layer.map((node) => getNodeDimensions(node));
    const maxLayerHeight = Math.max(...dimensions.map((d) => d.height), 130);
    const totalWidth = layerWidths[layerIdx];

    if (layerIdx === 0) {
      // 第 0 层整体以 centralX 为中心水平居中排布
      let xCursor = centralX - totalWidth / 2;
      layer.forEach((node, idx) => {
        const dim = dimensions[idx];
        node.layout = {
          x: Math.round(xCursor),
          y: Math.round(currentY),
        };
        xCursor += dim.width + gapX;
      });
    } else {
      // 后续层优先对齐关联父节点的中心 X
      layer.forEach((node, idx) => {
        const dim = dimensions[idx];
        const parentIds = [...dagInEdges.get(node.id)];
        const parents = parentIds
          .map((id) => nodeMap.get(id))
          .filter((p) => p && p.layout);

        let idealCenterX;
        if (parents.length > 0) {
          idealCenterX =
            parents.reduce((sum, p) => {
              const pDim = getNodeDimensions(p);
              return sum + (p.layout.x + pDim.width / 2);
            }, 0) / parents.length;
        } else {
          idealCenterX = centralX;
        }

        node.layout = {
          x: Math.round(idealCenterX - dim.width / 2),
          y: Math.round(currentY),
        };
      });

      // 消除同层重叠：按 ideal X 升序排列并向右推移
      layer.sort((a, b) => a.layout.x - b.layout.x);
      for (let i = 1; i < layer.length; i++) {
        const prev = layer[i - 1];
        const prevDim = getNodeDimensions(prev);
        const curr = layer[i];
        const minX = prev.layout.x + prevDim.width + gapX;
        if (curr.layout.x < minX) {
          curr.layout.x = Math.round(minX);
        }
      }

      // 计算当前层实际包围盒中心，向父节点整体中心做平移微调居中
      const layerMinX = layer[0].layout.x;
      const lastNode = layer[layer.length - 1];
      const lastDim = getNodeDimensions(lastNode);
      const layerMaxX = lastNode.layout.x + lastDim.width;
      const actualCenterX = (layerMinX + layerMaxX) / 2;

      // 获取当前层所有父节点的平均中心
      const allParentNodes = layer
        .flatMap((n) => [...dagInEdges.get(n.id)])
        .map((id) => nodeMap.get(id))
        .filter((p) => p && p.layout);

      let targetCenter = centralX;
      if (allParentNodes.length > 0) {
        targetCenter =
          allParentNodes.reduce((sum, p) => {
            const pDim = getNodeDimensions(p);
            return sum + (p.layout.x + pDim.width / 2);
          }, 0) / allParentNodes.length;
      }

      const shiftX = Math.round(targetCenter - actualCenterX);
      if (Math.abs(shiftX) > 2) {
        layer.forEach((n) => {
          n.layout.x += shiftX;
        });
      }
    }

    currentY += maxLayerHeight + gapY;
  });

  // 6. 全局规范化：确保所有节点 X >= startX, Y >= startY
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
 * 紧凑布局算法：使用多轮重心排序的拓扑网格布局。
 *
 * 不将所有拓扑层强制排成一列或一行，而是将已经按重心排序的节点按列填充到
 * 紧凑网格中。这样长链会自动换列，分支也会共享画布空间；重心排序则用于让
 * 关联节点尽可能相邻，从而降低边的长度与交叉概率。
 */
export function layoutCompact(workflow, options = {}) {
  const nodes = workflow.nodes || [];
  const edges = workflow.edges || [];
  if (nodes.length === 0) return;

  const startX = options.startX ?? 80;
  const startY = options.startY ?? 80;
  const gapX = options.gapX ?? COMPACT_HORIZONTAL_GAP;
  const gapY = options.gapY ?? COMPACT_VERTICAL_GAP;
  const orderingPasses = options.orderingPasses ?? COMPACT_ORDERING_PASSES;
  const { dagOutEdges, dagInEdges } = buildDagGraph(nodes, edges);
  const ranks = assignRanks(workflow, nodes, dagOutEdges, dagInEdges);
  const maxRank = Math.max(0, ...ranks.values());
  const layers = Array.from({ length: maxRank + 1 }, () => []);

  nodes.forEach((node) => {
    layers[ranks.get(node.id)].push(node);
  });

  const activeLayers = layers.filter((layer) => layer.length > 0);
  for (let pass = 0; pass < orderingPasses; pass += 1) {
    orderLayers(activeLayers, dagInEdges, dagOutEdges);
  }

  const orderedNodes = activeLayers.flat();
  const columnCount = Math.min(
    orderedNodes.length,
    Math.max(
      COMPACT_MINIMUM_COLUMNS,
      Math.ceil(Math.sqrt(orderedNodes.length * COMPACT_DENSITY_FACTOR)),
    ),
  );
  const rowCount = Math.ceil(orderedNodes.length / columnCount);
  const dimensions = orderedNodes.map((node) => getNodeDimensions(node));
  const columnWidths = Array.from({ length: columnCount }, () => 0);
  const rowHeights = Array.from({ length: rowCount }, () => 0);

  dimensions.forEach((dimension, index) => {
    const columnIndex = Math.floor(index / rowCount);
    const rowIndex = index % rowCount;
    columnWidths[columnIndex] = Math.max(columnWidths[columnIndex], dimension.width);
    rowHeights[rowIndex] = Math.max(rowHeights[rowIndex], dimension.height);
  });

  const columnPositions = [];
  let xCursor = startX;
  columnWidths.forEach((width) => {
    columnPositions.push(xCursor);
    xCursor += width + gapX;
  });

  const rowPositions = [];
  let yCursor = startY;
  rowHeights.forEach((height) => {
    rowPositions.push(yCursor);
    yCursor += height + gapY;
  });

  orderedNodes.forEach((node, index) => {
    const columnIndex = Math.floor(index / rowCount);
    const rowIndex = index % rowCount;
    const dimension = dimensions[index];
    node.layout = {
      x: Math.round(columnPositions[columnIndex]),
      y: Math.round(rowPositions[rowIndex] + (rowHeights[rowIndex] - dimension.height) / 2),
    };
  });
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
