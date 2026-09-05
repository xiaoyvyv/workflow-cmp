/**
 * Workflow Editor 配置与协议常量定义
 * 与 Kotlin Bridge 服务端契约 (EditorBridgeContract.kt) 保持 1:1 对齐
 */
export const EditorConfig = {
  workflowFormatVersion: 1,
  defaultNodePosition: { x: 360, y: 160 },
  nodeWidth: 240,
  portVerticalOffset: 45,
  edgeControlOffset: 60,
  maximumEventCharacters: 6000,
  defaultDeviceUrl: "http://localhost:8080",

  httpStatus: {
    ok: 200,
    accepted: 202,
    unauthorized: 401,
    notFound: 404,
    conflict: 409,
    unprocessableEntity: 422,
    notImplemented: 501,
  },

  // 兼容别名
  httpNotFound: 404,
  httpConflict: 409,

  storage: {
    draftKey: "workflow_editor_draft_v1",
  },

  api: {
    device: "/api/v1/device",
    manifest: "/api/v1/manifest",
    workflows: "/api/v1/workflows",
    validateWorkflow: "/api/v1/workflows/validate",
    events: "/api/v1/events",
    authorizationHeader: "Authorization",
    bearerPrefix: "Bearer ",
    eventAccessTokenQuery: "access_token",
  },

  eventType: {
    entryChanged: "editor.entry.changed",
    globalErrorChanged: "editor.global_error.changed",
    capabilitiesAutoCompleted: "editor.capabilities.auto_completed",
    connectionRejected: "editor.connection.rejected",
    bridgeConnected: "bridge.connected",
    bridgeMessage: "bridge.message",
    bridgeError: "bridge.error",
    workflowSaved: "workflow.saved",

    // 服务端执行事件（与 EditorBridgeEventType 对齐）
    runStarted: "run.started",
    runCompleted: "run.completed",
    runFailed: "run.failed",
    nodeStarted: "node.started",
    nodeCompleted: "node.completed",
    sideEffectRequested: "side-effect.requested",
  },
};
