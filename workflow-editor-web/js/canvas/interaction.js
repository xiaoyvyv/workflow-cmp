import { EditorConfig } from "../config.js";
import { state, nodeById, specForNode, newWorkflow, clearDraft, saveDraft } from "../state.js";
import { $, generateUUID } from "../utils.js";
import { showToast } from "../ui/toast.js";
import { showModalDialog } from "../ui/modal.js";
import { showEvent } from "../console/console.js";
import { drawEdges, canConnect, highlightCompatiblePorts } from "./edges.js";
import { executeAutoLayout, layoutAlgorithms } from "./layout.js";
import { renderPalette } from "../palette/palette.js";

export async function deleteNode(nodeId, renderCallback) {
  if (!nodeId) return;
  const node = nodeById(nodeId);
  if (!node) return;

  const connectedEdges = state.workflow.edges.filter(
    (edge) => edge.source.nodeId === nodeId || edge.target.nodeId === nodeId,
  );

  if (connectedEdges.length > 0) {
    const spec = specForNode(node);
    const nodeTitle = node.label || spec?.editor?.title || node.type || nodeId;
    const confirmed = await showModalDialog({
      title: "确认删除节点",
      message: `节点“${nodeTitle}”当前包含 ${connectedEdges.length} 条关联连线，删除节点将同时清除所有关联连线。\n确定要继续删除吗？`,
      type: "warning",
      confirmText: "确定删除",
      cancelText: "取消",
    });
    if (!confirmed) return;
  }

  state.workflow.nodes = state.workflow.nodes.filter((n) => n.id !== nodeId);
  state.workflow.edges = state.workflow.edges.filter(
    (edge) => edge.source.nodeId !== nodeId && edge.target.nodeId !== nodeId,
  );
  if (state.workflow.entryNodeId === nodeId) state.workflow.entryNodeId = "";
  if (state.workflow.globalErrorNodeId === nodeId) state.workflow.globalErrorNodeId = null;
  if (state.selectedNodeId === nodeId) state.selectedNodeId = null;
  state.errorNodeIds.delete(nodeId);

  if (renderCallback) renderCallback();
  else saveDraft(true);
  showToast("节点已删除", "info", 1500);
}

/**
 * 应用无限画布当前的平移与缩放矩阵变换，并联动背景网格与 LOD 视图
 */
export function applyCanvasTransform() {
  const viewportEl = $("canvas-viewport");
  const canvasEl = $("canvas");
  const zoomResetBtn = $("zoom-reset");
  const zoom = state.zoom ?? 1.0;
  const panX = state.panX ?? 0;
  const panY = state.panY ?? 0;

  if (viewportEl) {
    viewportEl.style.transform = `translate(${panX}px, ${panY}px) scale(${zoom})`;
  }

  if (canvasEl) {
    // 视差无限网格：动态同步背景位置与网格密度
    const gridSize = Math.max(12, Math.round(24 * zoom));
    canvasEl.style.backgroundPosition = `${panX}px ${panY}px`;
    canvasEl.style.backgroundSize = `${gridSize}px ${gridSize}px`;

    canvasEl.classList.toggle("canvas-zoom-compact", zoom < 0.5);
    canvasEl.classList.toggle("canvas-zoom-micro", zoom < 0.25);
  }

  if (zoomResetBtn) {
    zoomResetBtn.textContent = `${Math.round(zoom * 100)}%`;
  }

  drawEdges();
  saveDraft();
}

/**
 * 以指定屏幕锚点（默认视口中心）进行平滑缩放
 */
export function setCanvasZoom(newZoom, anchorPoint = null) {
  const canvasEl = $("canvas");
  const oldZoom = state.zoom || 1.0;
  const zoom = Math.min(2.5, Math.max(0.05, Math.round(newZoom * 10000) / 10000));

  let mouseX, mouseY;
  if (canvasEl) {
    if (anchorPoint && typeof anchorPoint.clientX === "number") {
      const canvasRect = canvasEl.getBoundingClientRect();
      mouseX = anchorPoint.clientX - canvasRect.left;
      mouseY = anchorPoint.clientY - canvasRect.top;
    } else if (anchorPoint && typeof anchorPoint.x === "number") {
      mouseX = anchorPoint.x;
      mouseY = anchorPoint.y;
    } else {
      mouseX = canvasEl.clientWidth / 2;
      mouseY = canvasEl.clientHeight / 2;
    }
  } else {
    mouseX = 0;
    mouseY = 0;
  }

  // 保持鼠标光标所在的逻辑坐标在缩放前后绝对静止
  const currentPanX = state.panX || 0;
  const currentPanY = state.panY || 0;
  state.panX = Math.round(mouseX - (mouseX - currentPanX) * (zoom / oldZoom));
  state.panY = Math.round(mouseY - (mouseY - currentPanY) * (zoom / oldZoom));
  state.zoom = zoom;

  applyCanvasTransform();
}

/**
 * ComfyUI 风格的智能全图自适应居中（Auto Zoom-Fit）
 * 无论节点在何种坐标，计算包围盒并将其居中放置在当前屏幕视窗正中央
 */
export function zoomFit() {
  const canvasEl = $("canvas");
  if (!canvasEl) return;
  const nodes = state.workflow.nodes;
  if (!nodes || nodes.length === 0) {
    state.zoom = 1.0;
    state.panX = Math.round(canvasEl.clientWidth / 2);
    state.panY = Math.round(canvasEl.clientHeight / 2);
    applyCanvasTransform();
    return;
  }

  let minX = Infinity;
  let maxX = -Infinity;
  let minY = Infinity;
  let maxY = -Infinity;

  nodes.forEach((node) => {
    const nodeEl = document.getElementById(`node-${node.id}`);
    const x = Number.isFinite(node.layout?.x) ? node.layout.x : 0;
    const y = Number.isFinite(node.layout?.y) ? node.layout.y : 0;
    const w = nodeEl && nodeEl.offsetWidth > 0 ? nodeEl.offsetWidth : 240;
    const h = nodeEl && nodeEl.offsetHeight > 0 ? nodeEl.offsetHeight : 140;

    minX = Math.min(minX, x);
    maxX = Math.max(maxX, x + w);
    minY = Math.min(minY, y);
    maxY = Math.max(maxY, y + h);
  });

  const boxW = Math.max(60, maxX - minX);
  const boxH = Math.max(60, maxY - minY);
  const boxCenterX = (minX + maxX) / 2;
  const boxCenterY = (minY + maxY) / 2;

  const canvasW = canvasEl.clientWidth || 800;
  const canvasH = canvasEl.clientHeight || 600;

  // 舒适的视口边缘留白（左右至少 120px，上下至少 100px）
  const paddingX = Math.min(140, Math.max(60, canvasW * 0.1));
  const paddingY = Math.min(120, Math.max(60, canvasH * 0.1));

  const availableW = Math.max(80, canvasW - paddingX * 2);
  const availableH = Math.max(80, canvasH - paddingY * 2);

  // 计算适配缩放比（留出 8% 余量使得视野开阔舒适）
  let fitZoom = Math.min(availableW / boxW, availableH / boxH) * 0.92;
  fitZoom = Math.min(1.2, Math.max(0.05, Math.round(fitZoom * 100) / 100));

  // 精准平移：让内容包围盒几何中心严格落入视窗屏幕中心 (canvasW/2, canvasH/2)
  state.zoom = fitZoom;
  state.panX = Math.round(canvasW / 2 - boxCenterX * fitZoom);
  state.panY = Math.round(canvasH / 2 - boxCenterY * fitZoom);

  applyCanvasTransform();
}

/**
 * 将屏幕事件坐标转换为无限画布内的逻辑绝对坐标
 */
export function getCanvasLogicalCoords(event, canvasEl) {
  const canvasRect = canvasEl.getBoundingClientRect();
  const zoom = state.zoom || 1.0;
  const panX = state.panX || 0;
  const panY = state.panY || 0;
  const mouseX = event.clientX - canvasRect.left;
  const mouseY = event.clientY - canvasRect.top;
  const x = (mouseX - panX) / zoom;
  const y = (mouseY - panY) / zoom;
  return { x, y };
}

export function initializeCanvasInteractions(renderCallback) {
  const canvasEl = $("canvas");
  if (!canvasEl) return;

  // 初始化缩放与自适应按钮
  $("zoom-out")?.addEventListener("click", () => setCanvasZoom((state.zoom || 1.0) - 0.1));
  $("zoom-in")?.addEventListener("click", () => setCanvasZoom((state.zoom || 1.0) + 0.1));
  $("zoom-reset")?.addEventListener("click", () => setCanvasZoom(1.0));
  $("zoom-fit")?.addEventListener("click", zoomFit);

  // 切换布局类型后立即格式化，无需额外确认按钮。
  const triggerAutoLayout = (forceDir = null) => {
    if (!state.workflow.nodes || state.workflow.nodes.length === 0) {
      showToast("画布中暂无节点可排版", "info");
      return;
    }
    const dir = forceDir || state.layoutDirection || "dag-lr";
    state.layoutDirection = dir;
    updateLayoutPicker(dir);
    executeAutoLayout(state.workflow, dir);
    if (renderCallback) renderCallback();
    zoomFit();
    const dirName = layoutAlgorithms.get(dir)?.name || "从左到右 (LR)";
    showToast(`已完成${dirName}自动排版并自适应视野`, "success");
  };

  const layoutPicker = $("canvas-layout-picker");
  const layoutTrigger = $("canvas-layout-trigger");
  const layoutMenu = $("canvas-layout-menu");

  const updateLayoutPicker = (layoutId) => {
    const layoutName = layoutAlgorithms.get(layoutId)?.name || "从左到右 (LR)";
    const layoutLabel = $("canvas-layout-label");
    if (layoutLabel) {
      const selectedOption = layoutMenu?.querySelector(`[data-layout-id="${layoutId}"] span`);
      layoutLabel.textContent = selectedOption?.textContent || layoutName;
    }
    layoutMenu?.querySelectorAll("[data-layout-id]").forEach((option) => {
      const isSelected = option.dataset.layoutId === layoutId;
      option.classList.toggle("selected", isSelected);
      option.setAttribute("aria-checked", String(isSelected));
    });
  };

  const setLayoutMenuOpen = (isOpen) => {
    if (!layoutMenu || !layoutTrigger) return;
    layoutMenu.hidden = !isOpen;
    layoutTrigger.setAttribute("aria-expanded", String(isOpen));
  };

  updateLayoutPicker(state.layoutDirection || "dag-lr");

  layoutTrigger?.addEventListener("click", () => {
    setLayoutMenuOpen(layoutMenu?.hidden);
  });

  layoutMenu?.querySelectorAll("[data-layout-id]").forEach((option) => {
    option.addEventListener("click", () => {
      const layoutId = option.dataset.layoutId;
      if (layoutId) {
        triggerAutoLayout(layoutId);
      }
      setLayoutMenuOpen(false);
    });
  });

  document.addEventListener("click", (event) => {
    if (layoutPicker && !layoutPicker.contains(event.target)) {
      setLayoutMenuOpen(false);
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      setLayoutMenuOpen(false);
    }
  });

  if (layoutPicker) {
    layoutPicker.addEventListener("focusout", (event) => {
      if (!layoutPicker.contains(event.relatedTarget)) {
        setLayoutMenuOpen(false);
      }
    });
  }

  // 清空画布
  $("canvas-clear")?.addEventListener("click", async () => {
    const nodeCount = state.workflow.nodes?.length || 0;
    const edgeCount = state.workflow.edges?.length || 0;
    const confirmed = await showModalDialog({
      title: "确认清空工作流",
      message: `确定要清空画布吗？当前画布包含 ${nodeCount} 个节点和 ${edgeCount} 条连线。\n此操作将重置为包含开始与结束节点的初始工作流并重置视口。`,
      type: "warning",
      confirmText: "确定清空",
      cancelText: "取消",
    });
    if (!confirmed) return;

    state.workflow = newWorkflow();
    state.selectedNodeId = null;
    state.selectedEdgeId = null;
    state.pendingLink = null;
    state.errorNodeIds.clear();
    clearDraft();
    if (renderCallback) renderCallback();
    zoomFit();
    showToast("已清空画布并重置为初始状态", "info");
  });

  // 滚轮与 Mac 触控板事件：Ctrl/Cmd 缩放，普通滚轮无缝平移（支持双向手势）
  canvasEl.addEventListener(
    "wheel",
    (event) => {
      event.preventDefault();
      if (event.ctrlKey || event.metaKey) {
        const currentZoom = state.zoom || 1.0;
        let newZoom;
        if (Math.abs(event.deltaY) >= 40) {
          const multiplier = event.deltaY < 0 ? 1.15 : 0.85;
          newZoom = currentZoom * multiplier;
        } else {
          const factor = Math.min(0.2, Math.max(-0.2, -event.deltaY * 0.01));
          if (currentZoom <= 0.06 && factor > 0) {
            newZoom = currentZoom + Math.max(0.003, currentZoom * factor);
          } else {
            newZoom = currentZoom * (1 + factor);
          }
        }
        setCanvasZoom(newZoom, {
          clientX: event.clientX,
          clientY: event.clientY,
        });
      } else {
        // 双指滑动或鼠标滚轮平移无限画布
        state.panX = Math.round((state.panX || 0) - event.deltaX);
        state.panY = Math.round((state.panY || 0) - event.deltaY);
        applyCanvasTransform();
      }
    },
    { passive: false },
  );

  // 鼠标指针按下事件
  canvasEl.addEventListener("pointerdown", (event) => {
    // 忽略悬浮控制面板的点击
    if (event.target.closest(".canvas-controls")) {
      return;
    }

    // 1. 点击端口触发连线
    const portEl = event.target.closest(".port");
    if (portEl) {
      event.preventDefault();
      event.stopPropagation();
      const nodeEl = portEl.closest(".node");
      const nodeId = nodeEl?.dataset.id;
      const portId = portEl.dataset.port;
      const direction = portEl.dataset.dir;

      if (direction === "out") {
        const coords = getCanvasLogicalCoords(event, canvasEl);
        state.linking = {
          nodeId,
          portId,
          direction: "out",
          currentX: coords.x,
          currentY: coords.y,
        };
        highlightCompatiblePorts(state.linking, true);
        drawEdges();
      }
      return;
    }

    // 2. 点击连线选择或双击删除
    const edgeGroup = event.target.closest("[data-edge-id]");
    if (edgeGroup && edgeGroup.dataset.edgeId && !event.target.closest(".node")) {
      event.preventDefault();
      event.stopPropagation();
      const clickedEdgeId = edgeGroup.dataset.edgeId;
      const loopReturnId = edgeGroup.dataset.loopReturnId;

      if (loopReturnId) {
        // 点击虚拟回环边时，直接选中对应的 loop.next 控制节点
        state.selectedNodeId = loopReturnId;
        state.selectedEdgeId = null;
        if (renderCallback) renderCallback();
        else {
          drawEdges();
          saveDraft();
        }
        return;
      }

      state.selectedEdgeId = clickedEdgeId;
      state.selectedNodeId = null;
      if (renderCallback) renderCallback();
      else {
        drawEdges();
        saveDraft();
      }
      return;
    }

    // 3. 点击节点进行选中和拖拽
    const nodeEl = event.target.closest(".node");
    if (nodeEl) {
      const node = nodeById(nodeEl.dataset.id);
      if (!node) return;
      state.selectedNodeId = node.id;
      state.selectedEdgeId = null;
      state.dragging = {
        id: node.id,
        startPointerX: event.clientX,
        startPointerY: event.clientY,
        initialX: node.layout?.x ?? 0,
        initialY: node.layout?.y ?? 0,
      };
      if (renderCallback) renderCallback();
      else saveDraft();
      return;
    }

    // 4. 点击画布空白区域：取消选中并恢复高亮状态，开启视口拖拽平移（Pan）
    state.selectedNodeId = null;
    state.selectedEdgeId = null;
    state.pendingLink = null;
    state.errorNodeIds.clear();
    state.panning = {
      startX: event.clientX,
      startY: event.clientY,
      initialPanX: state.panX || 0,
      initialPanY: state.panY || 0,
    };
    canvasEl.classList.add("panning");
    if (renderCallback) renderCallback();
    else saveDraft();
  });

  // 双击连线删除
  canvasEl.addEventListener("dblclick", (event) => {
    const edgeGroup = event.target.closest("[data-edge-id]");
    if (edgeGroup && edgeGroup.dataset.edgeId && !edgeGroup.dataset.loopReturnId && !event.target.closest(".node")) {
      event.preventDefault();
      event.stopPropagation();
      const edgeId = edgeGroup.dataset.edgeId;
      edgeGroup.classList.add("edge-deleting");
      setTimeout(() => {
        state.workflow.edges = state.workflow.edges.filter((e) => e.id !== edgeId);
        if (state.selectedEdgeId === edgeId) state.selectedEdgeId = null;
        drawEdges();
        if (renderCallback) renderCallback();
        else saveDraft(true);
      }, 100);
    }
  });

  window.addEventListener("pointermove", (event) => {
    // 视口拖拽平移中
    if (state.panning) {
      const dx = event.clientX - state.panning.startX;
      const dy = event.clientY - state.panning.startY;
      state.panX = Math.round(state.panning.initialPanX + dx);
      state.panY = Math.round(state.panning.initialPanY + dy);
      applyCanvasTransform();
      return;
    }

    // 连线拖拽中
    if (state.linking) {
      const coords = getCanvasLogicalCoords(event, canvasEl);
      state.linking.currentX = coords.x;
      state.linking.currentY = coords.y;

      document
        .querySelectorAll(".port-hover")
        .forEach((el) => el.classList.remove("port-hover"));
      const hoveredPort = document
        .elementFromPoint(event.clientX, event.clientY)
        ?.closest(".port.in");
      if (hoveredPort) {
        hoveredPort.classList.add("port-hover");
      }
      drawEdges();
      return;
    }

    // 节点拖拽中
    if (state.dragging) {
      const node = nodeById(state.dragging.id);
      if (!node) return;
      const zoom = state.zoom || 1.0;
      const dx = (event.clientX - state.dragging.startPointerX) / zoom;
      const dy = (event.clientY - state.dragging.startPointerY) / zoom;
      node.layout.x = Math.round(state.dragging.initialX + dx);
      node.layout.y = Math.round(state.dragging.initialY + dy);

      const nodeEl = document.getElementById(`node-${node.id}`);
      if (nodeEl) {
        nodeEl.style.left = `${node.layout.x}px`;
        nodeEl.style.top = `${node.layout.y}px`;
      }
      drawEdges();
    }
  });

  window.addEventListener("pointerup", (event) => {
    if (state.panning) {
      state.panning = null;
      canvasEl.classList.remove("panning");
      saveDraft(true);
    }

    if (state.linking) {
      const targetPortEl = document
        .elementFromPoint(event.clientX, event.clientY)
        ?.closest(".port.in");
      if (targetPortEl) {
        const nodeEl = targetPortEl.closest(".node");
        const target = {
          nodeId: nodeEl?.dataset.id,
          portId: targetPortEl.dataset.port,
        };
        if (canConnect(state.linking, target)) {
          const sourceSpec = specForNode(nodeById(state.linking.nodeId));
          const outputPort = sourceSpec?.outputPorts?.find(
            (p) => p.id === state.linking.portId,
          );
          const kind = outputPort ? (outputPort.kind || "control") : "control";
          state.workflow.edges.push({
            id: generateUUID(),
            source: { nodeId: state.linking.nodeId, portId: state.linking.portId },
            target,
            kind,
          });
        } else {
          showEvent({
            type: EditorConfig.eventType.connectionRejected,
            message: "端口方向、类型或连接数量不兼容",
          });
        }
        state.pendingLink = null;
      } else {
        state.pendingLink = { nodeId: state.linking.nodeId, portId: state.linking.portId };
      }
      highlightCompatiblePorts(null, false);
      state.linking = null;
      if (renderCallback) renderCallback();
      else saveDraft(true);
    }

    if (state.dragging) {
      saveDraft(true);
      state.dragging = null;
    }
  });

  // 平板电脑 / 触控屏多指手势支持 (Pinch to Zoom & Two-Finger Pan on Tablets / iPad)
  let initialTouchDistance = 0;
  let initialTouchZoom = 1.0;
  let initialTouchCenter = { x: 0, y: 0 };
  let initialPanOnTouch = { x: 0, y: 0 };
  let isPinching = false;

  canvasEl.addEventListener(
    "touchstart",
    (event) => {
      if (event.touches.length === 2) {
        event.preventDefault();
        isPinching = true;

        // 终止单指可能存在的连线/拖拽/单指平移
        if (state.dragging) state.dragging = null;
        if (state.linking) {
          highlightCompatiblePorts(null, false);
          state.linking = null;
        }
        if (state.panning) {
          state.panning = null;
          canvasEl.classList.remove("panning");
        }

        const t0 = event.touches[0];
        const t1 = event.touches[1];
        initialTouchDistance = Math.hypot(t1.clientX - t0.clientX, t1.clientY - t0.clientY);
        initialTouchZoom = state.zoom || 1.0;
        initialTouchCenter = {
          x: (t0.clientX + t1.clientX) / 2,
          y: (t0.clientY + t1.clientY) / 2,
        };
        initialPanOnTouch = {
          x: state.panX || 0,
          y: state.panY || 0,
        };
      }
    },
    { passive: false }
  );

  canvasEl.addEventListener(
    "touchmove",
    (event) => {
      if (isPinching && event.touches.length === 2) {
        event.preventDefault();
        const t0 = event.touches[0];
        const t1 = event.touches[1];
        const currentDistance = Math.hypot(t1.clientX - t0.clientX, t1.clientY - t0.clientY);
        const currentCenter = {
          x: (t0.clientX + t1.clientX) / 2,
          y: (t0.clientY + t1.clientY) / 2,
        };

        if (initialTouchDistance > 10) {
          const scale = currentDistance / initialTouchDistance;
          const targetZoom = initialTouchZoom * scale;
          setCanvasZoom(targetZoom, {
            clientX: currentCenter.x,
            clientY: currentCenter.y,
          });

          // 双指位移联动平移
          const deltaX = currentCenter.x - initialTouchCenter.x;
          const deltaY = currentCenter.y - initialTouchCenter.y;
          state.panX = Math.round(initialPanOnTouch.x + deltaX);
          state.panY = Math.round(initialPanOnTouch.y + deltaY);
          applyCanvasTransform();
        }
      }
    },
    { passive: false }
  );

  const handleTouchEnd = (event) => {
    if (isPinching && event.touches.length < 2) {
      isPinching = false;
      initialTouchDistance = 0;
      saveDraft(true);
    }
  };

  canvasEl.addEventListener("touchend", handleTouchEnd, { passive: true });
  canvasEl.addEventListener("touchcancel", handleTouchEnd, { passive: true });

  // Safari / WebKit 浏览器手势与多点触控增强
  let safariGestureStartZoom = 1.0;
  canvasEl.addEventListener("gesturestart", (event) => {
    event.preventDefault();
    safariGestureStartZoom = state.zoom || 1.0;
  });
  canvasEl.addEventListener("gesturechange", (event) => {
    event.preventDefault();
    setCanvasZoom(safariGestureStartZoom * event.scale, {
      clientX: event.clientX,
      clientY: event.clientY,
    });
  });
  canvasEl.addEventListener("gestureend", (event) => {
    event.preventDefault();
    saveDraft(true);
  });

  // 禁用非 Canvas 区域的触控板捏合与 Ctrl/Cmd+滚轮浏览器全页面缩放
  window.addEventListener(
    "wheel",
    (event) => {
      if ((event.ctrlKey || event.metaKey) && !event.target.closest("#canvas")) {
        event.preventDefault();
      }
    },
    { passive: false }
  );

  // 禁用非 Canvas 区域的 Safari / WebKit 浏览器默认手势缩放
  ["gesturestart", "gesturechange", "gestureend"].forEach((evtName) => {
    window.addEventListener(
      evtName,
      (event) => {
        if (!event.target.closest("#canvas")) {
          event.preventDefault();
        }
      },
      { passive: false }
    );
  });

  document.addEventListener("keydown", (event) => {
    // 拦截 Ctrl/Cmd + (+, -, =, 0) 浏览器整页缩放快捷键，转为作用于画布缩放
    if (event.ctrlKey || event.metaKey) {
      if (event.key === "+" || event.key === "=" || event.code === "NumpadAdd" || event.code === "Equal") {
        event.preventDefault();
        setCanvasZoom((state.zoom || 1.0) + 0.1);
        return;
      }
      if (event.key === "-" || event.key === "_" || event.code === "NumpadSubtract" || event.code === "Minus") {
        event.preventDefault();
        setCanvasZoom((state.zoom || 1.0) - 0.1);
        return;
      }
      if (event.key === "0" || event.code === "Numpad0" || event.code === "Digit0") {
        event.preventDefault();
        setCanvasZoom(1.0);
        return;
      }
    }

    if (event.key === "Escape") {
      if (state.linking) {
        state.linking = null;
        highlightCompatiblePorts(null, false);
        drawEdges();
      }
      if (state.pendingLink) {
        state.pendingLink = null;
        renderPalette();
      }
      if (state.selectedEdgeId) {
        state.selectedEdgeId = null;
        drawEdges();
        if (renderCallback) renderCallback();
        else saveDraft();
      }
      if (state.selectedNodeId || state.errorNodeIds.size > 0) {
        state.selectedNodeId = null;
        state.errorNodeIds.clear();
        if (renderCallback) renderCallback();
        else saveDraft();
      }
      return;
    }

    const editing = /INPUT|TEXTAREA|SELECT/.test(
      document.activeElement?.tagName ?? "",
    );
    if (editing || (event.key !== "Delete" && event.key !== "Backspace")) {
      return;
    }

    if (state.selectedEdgeId) {
      const edgeId = state.selectedEdgeId;
      state.workflow.edges = state.workflow.edges.filter((e) => e.id !== edgeId);
      state.selectedEdgeId = null;
      drawEdges();
      if (renderCallback) renderCallback();
      else saveDraft(true);
      return;
    }

    if (!state.selectedNodeId) return;
    deleteNode(state.selectedNodeId, renderCallback);
  });

  // 阻止画布非输入元素的默认文本选择
  document.addEventListener("contextmenu", (event) => {
    if (!event.target.closest("input, textarea, select, button, option")) {
      event.preventDefault();
    }
  });
  document.addEventListener("selectstart", (event) => {
    if (!event.target.closest("input, textarea, select, button, option")) {
      event.preventDefault();
    }
  });
  document.addEventListener("dragstart", (event) => {
    if (!event.target.closest("input, textarea, select, button, option")) {
      event.preventDefault();
    }
  });
}
