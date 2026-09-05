import { renderPalette, initializePaletteEvents } from "./palette/palette.js";
import { renderNodes } from "./canvas/nodes.js";
import { drawEdges } from "./canvas/edges.js";
import { renderInspector, initializeInspectorEvents } from "./inspector/inspector.js";
import { initializeCanvasInteractions } from "./canvas/interaction.js";
import { initializeConsoleAndResizers } from "./console/console.js";
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

export function init() {
  const hasDraft = restoreDraft();
  initializeDeviceAddress();
  initializeConsoleAndResizers();
  initializePaletteEvents(render);
  initializeInspectorEvents(render);
  initializeCanvasInteractions(render);
  initializeToolbarActions(render);
  render();
  applyCanvasTransform();
  if (!hasDraft) {
    setTimeout(zoomFit, 30);
  }
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
