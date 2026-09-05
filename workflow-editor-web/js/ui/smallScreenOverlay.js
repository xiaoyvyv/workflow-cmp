import { $ } from "../utils.js";
import { showToast } from "./toast.js";

/**
 * 判断当前是否为移动端手机（PC / Mac / 平板设备一律返回 false）
 */
export function isMobilePhoneDevice() {
  if (typeof navigator === "undefined") return false;
  const ua = navigator.userAgent || navigator.vendor || window.opera || "";
  // 平板 (iPad, Android Tablet, etc.) 排除掉，不作为手机小屏处理
  const isTablet =
    /(iPad|Tablet|(Android(?!.*Mobile))|PlayBook|Silk)/i.test(ua) ||
    (navigator.maxTouchPoints > 1 && /Macintosh/i.test(ua));
  if (isTablet) return false;

  // 手机端 User Agent
  const isMobile = /Android.*Mobile|iPhone|iPod|BlackBerry|IEMobile|Opera Mini|Mobile/i.test(ua);
  return isMobile;
}

/**
 * 屏幕过小时全局引导蒙版控制（仅针对手机移动端设备生效，PC/平板不启用）
 */
export function initializeSmallScreenOverlay() {
  const overlayEl = $("small-screen-overlay");
  if (!overlayEl) return;

  const isMobile = isMobilePhoneDevice();
  if (!isMobile) {
    // PC / Mac / 平板设备：彻底禁用蒙层，确保永远不显示也不拦截任何点击
    document.body.classList.remove("is-mobile-device");
    overlayEl.style.display = "none";
    overlayEl.style.pointerEvents = "none";
    return;
  }

  document.body.classList.add("is-mobile-device");

  const currentSizeEl = $("screen-current-size");
  const copyUrlBtn = $("btn-copy-web-url");
  const dismissBtn = $("btn-dismiss-small-screen");

  const updateSizeInfo = () => {
    const w = window.innerWidth;
    const h = window.innerHeight;
    if (currentSizeEl) {
      currentSizeEl.textContent = `${w} × ${h} px`;
    }
  };

  updateSizeInfo();
  window.addEventListener("resize", updateSizeInfo);

  copyUrlBtn?.addEventListener("click", async () => {
    const url = window.location.href;
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(url);
      } else {
        const input = document.createElement("textarea");
        input.value = url;
        document.body.appendChild(input);
        input.select();
        document.execCommand("copy");
        input.remove();
      }
      showToast("已成功复制访问地址，可在电脑浏览器打开", "success");
    } catch {
      showToast(`访问地址: ${url}`, "info");
    }
  });

  dismissBtn?.addEventListener("click", () => {
    document.body.classList.add("dismiss-small-screen-warning");
    showToast("已暂时忽略小屏提示（可通过旋转屏幕或使用大屏设备获得最佳体验）", "info", 3500);
  });
}
