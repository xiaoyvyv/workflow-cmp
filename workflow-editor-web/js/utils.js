import { EditorConfig } from "./config.js";

/**
 * 生成符合 RFC4122 v4 规范的 UUID。
 * 优先使用 crypto.randomUUID()，若在非安全 HTTP 局域网环境（crypto.randomUUID 未提供）
 * 则自动回退到 getRandomValues 或随机算法，确保在所有网络环境下 100% 正常运行。
 */
export function generateUUID() {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    try {
      return crypto.randomUUID();
    } catch {}
  }

  if (typeof crypto !== "undefined" && typeof crypto.getRandomValues === "function") {
    try {
      const bytes = new Uint8Array(16);
      crypto.getRandomValues(bytes);
      bytes[6] = (bytes[6] & 0x0f) | 0x40; // version 4
      bytes[8] = (bytes[8] & 0x3f) | 0x80; // variant RFC4122
      const hex = Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
      return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
    } catch {}
  }

  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

// 自动 Polyfill 浏览器环境，防止第三方依赖或直接调用报错
if (typeof window !== "undefined") {
  if (typeof window.crypto === "undefined") {
    window.crypto = { randomUUID: generateUUID };
  } else if (typeof window.crypto.randomUUID !== "function") {
    try {
      window.crypto.randomUUID = generateUUID;
    } catch {}
  }
}

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
  if (x2 < x1) {
    const dx = Math.max(60, Math.abs(x2 - x1) * 0.25);
    const dy = Math.max(40, Math.abs(y2 - y1) * 0.3);
    const cx1 = x1 + dx;
    const cy1 = y1 + (y2 >= y1 ? dy : -dy);
    const cx2 = x2 - dx;
    const cy2 = y2 - (y2 >= y1 ? dy : -dy);
    return `M ${x1} ${y1} C ${cx1} ${cy1}, ${cx2} ${cy2}, ${x2} ${y2}`;
  }
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
