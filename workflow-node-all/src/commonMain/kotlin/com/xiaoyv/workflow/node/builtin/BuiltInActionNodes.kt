package com.xiaoyv.workflow.node.builtin

import com.xiaoyv.workflow.node.builtin.control.controlActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.control.flowActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.control.loopActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.data.arrayActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.data.dataActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.data.mathActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.data.objectActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.data.textActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.extension.bilibiliActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.io.fileActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.io.httpActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.io.sideEffectActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.io.storageActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.parse.codecActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.parse.cryptoActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.parse.csvActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.parse.dateActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.parse.htmlActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.parse.jsonActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.parse.urlActionNodeDefinitions
import com.xiaoyv.workflow.node.builtin.parse.xmlActionNodeDefinitions
import com.xiaoyv.workflow.node.core.ActionNodeDefinition
import com.xiaoyv.workflow.port.ActionHttpRequestExecutor
import com.xiaoyv.workflow.port.ActionWorkflowFileStorage
import com.xiaoyv.workflow.port.ActionWorkflowLogger
import com.xiaoyv.workflow.port.ActionWorkflowPreferencesStore

/**
 * App 随附的内置节点集合。
 */
fun builtInActionNodeDefinitions(
    httpRequestExecutor: ActionHttpRequestExecutor,
    preferencesStore: ActionWorkflowPreferencesStore = ActionWorkflowPreferencesStore.Default,
    fileStorage: ActionWorkflowFileStorage = ActionWorkflowFileStorage.Default,
    logger: ActionWorkflowLogger = ActionWorkflowLogger.Default,
): List<ActionNodeDefinition> = buildList {
    addAll(flowActionNodeDefinitions(logger))
    addAll(dataActionNodeDefinitions)
    addAll(dateActionNodeDefinitions)
    addAll(codecActionNodeDefinitions)
    addAll(cryptoActionNodeDefinitions)
    addAll(htmlActionNodeDefinitions)
    addAll(objectActionNodeDefinitions)
    addAll(jsonActionNodeDefinitions)
    addAll(controlActionNodeDefinitions)
    addAll(loopActionNodeDefinitions)
    addAll(mathActionNodeDefinitions)
    addAll(textActionNodeDefinitions)
    addAll(arrayActionNodeDefinitions)
    addAll(urlActionNodeDefinitions)
    addAll(csvActionNodeDefinitions)
    addAll(xmlActionNodeDefinitions)
    addAll(bilibiliActionNodeDefinitions)
    addAll(sideEffectActionNodeDefinitions)
    addAll(httpActionNodeDefinitions(httpRequestExecutor, fileStorage))
    addAll(storageActionNodeDefinitions(preferencesStore))
    addAll(fileActionNodeDefinitions(fileStorage))
}
