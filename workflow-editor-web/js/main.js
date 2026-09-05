import { renderPalette, initializePaletteEvents } from "./palette/palette.js";
import { renderNodes } from "./canvas/nodes.js";
import { drawEdges } from "./canvas/edges.js";
import { renderInspector, initializeInspectorEvents } from "./inspector/inspector.js";
import { initializeCanvasInteractions } from "./canvas/interaction.js";
import { initializeConsoleAndResizers } from "./console/console.js";
import { initializeSmallScreenOverlay } from "./ui/smallScreenOverlay.js";
import {
  initializeDeviceAddress,
  initializeToolbarActions,
  tryAutoConnect,
} from "./services/api.js";

import { state, restoreDraft, saveDraft } from "./state.js";
import { applyCanvasTransform, zoomFit } from "./canvas/interaction.js";

export function render() {
  renderPalette();
  renderNodes();
  drawEdges();
  renderInspector();
  saveDraft();
}

export async function loadDefaultManifest() {
  if (state.manifest?.nodeTypes?.length > 0) return;
  try {
    const res = await fetch(`./manifest.json?_t=${Date.now()}`, { cache: "no-store" });
    if (res.ok) {
      const data = await res.json();
      const manifest = data.manifest ?? data;
      if (manifest && Array.isArray(manifest.nodeTypes) && manifest.nodeTypes.length > 0) {
        if (!state.manifest?.nodeTypes || state.manifest.nodeTypes.length === 0) {
          state.manifest = manifest;
          render();
        }
      }
    }
  } catch (e) {
    console.debug("[WorkflowEditor] Default manifest.json not available:", e);
  }
}

export function init() {
  let hasDraft = false;
  try {
    hasDraft = restoreDraft();
  } catch (e) {
    console.warn("[WorkflowEditor] restoreDraft failed:", e);
  }

  try {
    initializeSmallScreenOverlay();
  } catch (e) {
    console.warn("[WorkflowEditor] initializeSmallScreenOverlay failed:", e);
  }

  try {
    initializeDeviceAddress();
  } catch (e) {
    console.warn("[WorkflowEditor] initializeDeviceAddress failed:", e);
  }

  try {
    initializeToolbarActions(render);
  } catch (e) {
    console.error("[WorkflowEditor] initializeToolbarActions failed:", e);
  }

  try {
    initializeConsoleAndResizers();
  } catch (e) {
    console.warn("[WorkflowEditor] initializeConsoleAndResizers failed:", e);
  }

  try {
    initializePaletteEvents(render);
  } catch (e) {
    console.warn("[WorkflowEditor] initializePaletteEvents failed:", e);
  }

  try {
    initializeInspectorEvents(render);
  } catch (e) {
    console.warn("[WorkflowEditor] initializeInspectorEvents failed:", e);
  }

  try {
    initializeCanvasInteractions(render);
  } catch (e) {
    console.warn("[WorkflowEditor] initializeCanvasInteractions failed:", e);
  }

  render();
  applyCanvasTransform();
  if (!hasDraft) {
    setTimeout(zoomFit, 30);
  }
  loadDefaultManifest();
  tryAutoConnect(render);

  window.addEventListener("beforeunload", () => {
    saveDraft(true);
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", init);
} else {
  init();
}
