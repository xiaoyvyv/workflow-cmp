import { $, escapeHtml } from "../utils.js";

export function showToast(message, type = "info", duration = 3000) {
  const container = $("toast-container");
  if (!container) return;
  const toast = document.createElement("div");
  toast.className = `toast-item toast-${type}`;
  const icon =
    type === "success" ? "✓" : type === "error" ? "✕" : type === "warning" ? "⚠" : "ℹ";
  toast.innerHTML = `<span style="font-weight:bold; font-size:14px;">${icon}</span><span>${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add("toast-hiding");
    setTimeout(() => toast.remove(), 200);
  }, duration);
}
