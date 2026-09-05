import { EditorConfig } from "./config.js";

export const $ = (id) => document.getElementById(id);

export function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

export function createBezierPath(x1, y1, x2, y2) {
  const dx = Math.max(30, Math.abs(x2 - x1) * 0.5);
  const controlOffset = Math.max(EditorConfig.edgeControlOffset, dx);
  return `M ${x1} ${y1} C ${x1 + controlOffset} ${y1}, ${x2 - controlOffset} ${y2}, ${x2} ${y2}`;
}

/**
 * 清理并提取 JSON 文本（过滤 BOM 标记、去除 Markdown 代码块包裹 ```json ... ``` 等）
 * @param {string} raw 原始文本
 * @returns {string} 清理后的 JSON 字符串
 */
export function cleanJsonText(raw) {
  if (!raw) return "";
  let text = String(raw).trim();
  if (text.charCodeAt(0) === 0xfeff) {
    text = text.slice(1).trim();
  }
  if (text.startsWith("```")) {
    text = text
      .replace(/^```(?:json)?\s*\n?/i, "")
      .replace(/\n?```\s*$/i, "")
      .trim();
  }
  return text;
}
