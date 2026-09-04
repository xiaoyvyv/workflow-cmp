package com.xiaoyv.workflow.ui.sideeffect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.xiaoyv.workflow.engine.ActionSideEffectResult
import com.xiaoyv.workflow.model.spec.ActionSelectDialogConfigKey
import com.xiaoyv.workflow.model.spec.ActionSelectOutputMode
import com.xiaoyv.workflow.node.effect.ActionConfirmEffect
import com.xiaoyv.workflow.node.effect.ActionImagePreviewEffect
import com.xiaoyv.workflow.node.effect.ActionInputDialogEffect
import com.xiaoyv.workflow.node.effect.ActionNotificationEffect
import com.xiaoyv.workflow.node.effect.ActionOpenExternalAppEffect
import com.xiaoyv.workflow.node.effect.ActionOpenExternalUrlEffect
import com.xiaoyv.workflow.node.effect.ActionOpenInternalWebEffect
import com.xiaoyv.workflow.node.effect.ActionReadClipboardEffect
import com.xiaoyv.workflow.node.effect.ActionSelectDialogEffect
import com.xiaoyv.workflow.node.effect.ActionShareEffect
import com.xiaoyv.workflow.node.effect.ActionShowToastEffect
import com.xiaoyv.workflow.node.effect.ActionSyncCookieEffect
import com.xiaoyv.workflow.node.effect.ActionVibrateEffect
import com.xiaoyv.workflow.node.effect.ActionVideoPreviewEffect
import com.xiaoyv.workflow.node.effect.ActionWriteClipboardEffect
import com.xiaoyv.workflow.ui.WorkflowSideEffectHostState
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * 将完整工作流副作用队列交由 Compose 宿主处理。
 *
 * 平台行为以回调形式显式注入；确认、输入、选择与 Cookie 同步的 UI 由插槽提供，
 * 因而该基础模块不依赖 WebView、播放器或应用导航实现。
 */
@Composable
fun WorkflowSideEffectHost(
    hostState: WorkflowSideEffectHostState,
    onShowToast: (message: String) -> Unit,
    onWriteClipboard: (text: String) -> Unit,
    onReadClipboard: suspend () -> String?,
    onOpenExternalUrl: (url: String) -> Unit,
    onOpenExternalApp: (uri: String, fallbackUrl: String?) -> Unit,
    onOpenInternalWeb: (url: String) -> Unit,
    onImagePreview: (images: List<String>, index: Int) -> Unit,
    onVideoPreview: (url: String, headers: Map<String, String>) -> Unit,
    onShareText: (title: String, text: String, url: String) -> Unit,
    onSendNotification: (title: String, content: String) -> Unit,
    onVibrate: (durationMillis: Long) -> Unit,
    confirmDialogSlot: @Composable (
        effect: ActionConfirmEffect,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
    ) -> Unit,
    inputDialogSlot: @Composable (
        effect: ActionInputDialogEffect,
        onConfirm: (input: String) -> Unit,
        onCancel: () -> Unit,
    ) -> Unit,
    selectDialogSlot: @Composable (
        effect: ActionSelectDialogEffect,
        onConfirm: (selectedIndices: List<Int>, selectedValues: List<String>) -> Unit,
        onCancel: () -> Unit,
    ) -> Unit,
    syncCookieDialogSlot: @Composable (
        effect: ActionSyncCookieEffect,
        onConfirm: () -> Unit,
    ) -> Unit,
) {
    val oneShotData = hostState.currentOneShotData

    LaunchedEffect(key1 = oneShotData?.id) {
        val entry = oneShotData ?: return@LaunchedEffect
        val result = when (val effect = entry.effect) {
            is ActionShowToastEffect -> onShowToast(effect.message).let { ActionSideEffectResult.Success() }
            is ActionWriteClipboardEffect -> onWriteClipboard(effect.text).let { ActionSideEffectResult.Success() }
            is ActionReadClipboardEffect -> ActionSideEffectResult.Success(
                buildJsonObject {
                    put(effect.outputKey, JsonPrimitive(onReadClipboard().orEmpty()))
                },
            )

            is ActionOpenExternalUrlEffect -> onOpenExternalUrl(effect.url).let { ActionSideEffectResult.Success() }
            is ActionOpenExternalAppEffect -> onOpenExternalApp(effect.uri, effect.fallbackUrl).let { ActionSideEffectResult.Success() }
            is ActionOpenInternalWebEffect -> onOpenInternalWeb(effect.url).let { ActionSideEffectResult.Success() }
            is ActionImagePreviewEffect -> onImagePreview(effect.images, effect.index).let { ActionSideEffectResult.Success() }
            is ActionVideoPreviewEffect -> onVideoPreview(effect.url, effect.headers).let { ActionSideEffectResult.Success() }
            is ActionShareEffect -> onShareText(effect.title, effect.text, effect.url).let { ActionSideEffectResult.Success() }
            is ActionNotificationEffect -> onSendNotification(effect.title, effect.content).let { ActionSideEffectResult.Success() }
            is ActionVibrateEffect -> onVibrate(effect.durationMillis).let { ActionSideEffectResult.Success() }
            else -> ActionSideEffectResult.Success()
        }
        hostState.popOneShotData { data ->
            data.onResult(result)
        }
    }

    hostState.currentConfirmData?.let { entry ->
        confirmDialogSlot(
            entry.effect,
            {
                hostState.popConfirmData { data ->
                    data.onResult(ActionSideEffectResult.Success())
                }
            },
            {
                hostState.popConfirmData { data ->
                    data.onResult(ActionSideEffectResult.Cancelled)
                }
            },
        )
    }
    hostState.currentInputData?.let { entry ->
        inputDialogSlot(
            entry.effect,
            { input ->
                hostState.popInputData { data ->
                    data.onResult(
                        ActionSideEffectResult.Success(
                            buildJsonObject {
                                put(data.effect.outputKey, JsonPrimitive(input))
                            },
                        ),
                    )
                }
            },
            {
                hostState.popInputData { data ->
                    data.onResult(ActionSideEffectResult.Cancelled)
                }
            },
        )
    }
    hostState.currentSelectData?.let { entry ->
        selectDialogSlot(
            entry.effect,
            { indices, values ->
                hostState.popSelectData { data ->
                    data.onResult(ActionSideEffectResult.Success(createSelectResult(data.effect, indices, values)))
                }
            },
            {
                hostState.popSelectData { data ->
                    data.onResult(ActionSideEffectResult.Cancelled)
                }
            },
        )
    }
    hostState.currentSyncCookieData?.let { entry ->
        syncCookieDialogSlot(
            entry.effect,
            {
                hostState.popSyncCookieData { data ->
                    data.onResult(ActionSideEffectResult.Success())
                }
            },
        )
    }
}

private fun createSelectResult(
    effect: ActionSelectDialogEffect,
    indices: List<Int>,
    values: List<String>,
) = buildJsonObject {
    val output = if (effect.isMultiSelect) {
        when (effect.outputMode) {
            ActionSelectOutputMode.INDEX -> JsonArray(indices.map(::JsonPrimitive))
            ActionSelectOutputMode.BOTH -> buildJsonObject {
                put(ActionSelectDialogConfigKey.INDICES, JsonArray(indices.map(::JsonPrimitive)))
                put(ActionSelectDialogConfigKey.VALUES, JsonArray(values.map(::JsonPrimitive)))
            }

            else -> JsonArray(values.map(::JsonPrimitive))
        }
    } else {
        val index = indices.firstOrNull() ?: -1
        val value = values.firstOrNull().orEmpty()
        when (effect.outputMode) {
            ActionSelectOutputMode.INDEX -> JsonPrimitive(index)
            ActionSelectOutputMode.BOTH -> buildJsonObject {
                put(ActionSelectDialogConfigKey.INDEX, JsonPrimitive(index))
                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive(value))
            }

            else -> JsonPrimitive(value)
        }
    }
    put(effect.outputKey, output)
}
