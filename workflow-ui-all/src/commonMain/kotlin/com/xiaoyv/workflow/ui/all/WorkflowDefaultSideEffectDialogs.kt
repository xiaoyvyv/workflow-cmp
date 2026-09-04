package com.xiaoyv.workflow.ui.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xiaoyv.workflow.model.spec.ActionProgressDialogMode
import com.xiaoyv.workflow.node.effect.ActionConfirmEffect
import com.xiaoyv.workflow.node.effect.ActionInputDialogEffect
import com.xiaoyv.workflow.node.effect.ActionProgressDialogEffect
import com.xiaoyv.workflow.node.effect.ActionSelectDialogEffect
import com.xiaoyv.workflow.ui.image.WorkflowSelectOptionImage

/**
 * 默认 Material3 确认对话框。
 */
@Composable
fun WorkflowConfirmAlertDialog(
    effect: ActionConfirmEffect,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = effect.title.takeIf(String::isNotBlank)?.let { title ->
            { Text(text = title) }
        },
        text = { Text(text = effect.message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = effect.confirmText.ifBlank { "确认" })
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = effect.cancelText.ifBlank { "取消" })
            }
        },
    )
}

/**
 * 默认 Material3 输入对话框，支持数字键盘与 IME 完成动作。
 */
@Composable
fun WorkflowInputAlertDialog(
    effect: ActionInputDialogEffect,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var value by remember(key1 = effect) {
        mutableStateOf(effect.defaultValue)
    }
    AlertDialog(
        onDismissRequest = onCancel,
        title = effect.title.takeIf(String::isNotBlank)?.let { title ->
            { Text(text = title) }
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = { input -> value = input },
                label = effect.subtitle.takeIf(String::isNotBlank)?.let { subtitle ->
                    { Text(text = subtitle) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (effect.onlyNumber) KeyboardType.Number else KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onConfirm(value.trim()) },
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value.trim()) }) {
                Text(text = effect.confirmText.ifBlank { "确认" })
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = effect.cancelText.ifBlank { "取消" })
            }
        },
    )
}

/**
 * 默认 Material3 单选与多选对话框，保留图片选项、默认选中值和输出排序。
 */
@Composable
fun WorkflowSelectAlertDialog(
    effect: ActionSelectDialogEffect,
    onConfirm: (selectedIndices: List<Int>, selectedValues: List<String>) -> Unit,
    onCancel: () -> Unit,
) {
    val initialSelection = remember(key1 = effect) {
        effect.defaultIndices.filter { index -> index in effect.options.indices }.toSet().ifEmpty {
            effect.options.mapIndexedNotNull { index, option ->
                index.takeIf { option.value in effect.defaultValues }
            }.toSet()
        }
    }
    var selected by remember(key1 = effect) {
        mutableStateOf(initialSelection)
    }
    AlertDialog(
        onDismissRequest = onCancel,
        title = effect.title.takeIf(String::isNotBlank)?.let { title ->
            { Text(text = title) }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                effect.subtitle.takeIf(String::isNotBlank)?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                effect.options.forEachIndexed { index, option ->
                    val isSelected = index in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (effect.isMultiSelect) {
                                    selected = if (isSelected) selected - index else selected + index
                                } else {
                                    onConfirm(listOf(index), listOf(option.value))
                                }
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (effect.isMultiSelect) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                        } else if (option.image.isNotBlank()) {
                            WorkflowSelectOptionImage(
                                url = option.image,
                                modifier = Modifier.size(width = 30.dp, height = 40.dp),
                            )
                        } else {
                            RadioButton(selected = isSelected, onClick = null)
                        }
                        Text(text = option.title.ifBlank { option.value })
                    }
                }
            }
        },
        confirmButton = {
            if (effect.isMultiSelect) {
                TextButton(
                    onClick = {
                        val indices = selected.sorted()
                        onConfirm(indices, indices.map { index -> effect.options[index].value })
                    },
                ) {
                    Text(text = effect.confirmText.ifBlank { "确认" })
                }
            }
        },
        dismissButton = {
            if (effect.isMultiSelect) {
                TextButton(onClick = onCancel) {
                    Text(text = effect.cancelText.ifBlank { "取消" })
                }
            }
        },
    )
}

/**
 * 聚合展示全部活动进度任务；进度框不可被误关闭。
 */
@Composable
fun WorkflowProgressAlertDialog(
    tasks: List<WorkflowSideEffectData<ActionProgressDialogEffect>>,
) {
    if (tasks.isEmpty()) return
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                tasks.forEachIndexed { index, task ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    task.effect.title.takeIf(String::isNotBlank)?.let { title ->
                        Text(text = title, style = MaterialTheme.typography.titleSmall)
                    }
                    task.effect.message.takeIf(String::isNotBlank)?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    val isDeterminate = task.effect.mode == ActionProgressDialogMode.DETERMINATE
                    if (!isDeterminate) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        )
                    } else {
                        val current = task.effect.progress ?: 0f
                        val max = (task.effect.maxProgress ?: 1f).takeIf { it > 0f } ?: 1f
                        val fraction = (current / max).coerceIn(0f, 1f)
                        val animatedProgress by animateFloatAsState(
                            targetValue = fraction,
                            label = "workflow_progress_animation",
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                val percent = (fraction * 100).toInt()
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

/**
 * 兼容旧调用的默认对话框入口。
 */
@Composable
fun WorkflowDefaultSideEffectDialogs(hostState: WorkflowSideEffectHostState) {
    WorkflowProgressAlertDialog(tasks = hostState.progressTasks)
}
