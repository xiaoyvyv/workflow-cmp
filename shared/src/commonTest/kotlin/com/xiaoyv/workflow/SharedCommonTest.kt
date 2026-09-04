package com.xiaoyv.workflow

import com.xiaoyv.workflow.demo.business.WorkflowSamples
import com.xiaoyv.workflow.di.WorkflowRuntimeConfig
import com.xiaoyv.workflow.di.createWorkflowRuntime
import com.xiaoyv.workflow.port.impl.DefaultActionHttpRequestExecutor
import io.ktor.client.HttpClient
import kotlin.test.Test
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun validateAllSamples() {
        val runtime = createWorkflowRuntime(
            config = WorkflowRuntimeConfig(
                httpRequestExecutor = DefaultActionHttpRequestExecutor(HttpClient()),
            ),
        )

        val errors = mutableListOf<String>()
        for (wf in WorkflowSamples.all) {
            if (wf.id == "workflow_sample_error_missing_config") continue
            val result = runtime.validator.validate(wf)
            if (!result.isValid) {
                errors.add(
                    "Workflow [${wf.id}] '${wf.name}' failed validation:\n" +
                            result.issues.joinToString("\n") { "  - [${it.code}] ${it.message} (node: ${it.nodeId})" })
            }
        }

        assertTrue(errors.isEmpty(), "Found ${errors.size} invalid workflows:\n" + errors.joinToString("\n\n"))
    }

    @Test
    fun testExecutionOfProgressDialogLoop() {
        kotlinx.coroutines.runBlocking {
            val runtime = createWorkflowRuntime(
                config = WorkflowRuntimeConfig(
                    httpRequestExecutor = DefaultActionHttpRequestExecutor(HttpClient()),
                ),
            )
            val sample = WorkflowSamples.all.first { it.id == "workflow_sample_ui_progress_dialog_loop" }
            val progressList = mutableListOf<Float>()

            runtime.engine.execute(
                workflow = sample,
                initialContext = com.xiaoyv.workflow.model.execution.ActionExecutionContext(),
                sideEffectHandler = { effect ->
                    if (effect is com.xiaoyv.workflow.node.effect.ActionProgressDialogEffect) {
                        effect.progress?.let { progressList.add(it) }
                    }
                    com.xiaoyv.workflow.engine.ActionSideEffectResult.Success()
                }
            ).collect {}

            println("Collected progress list: $progressList")
            kotlin.test.assertEquals(
                listOf(0f, 10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f, 100f),
                progressList
            )
        }
    }

    @Test
    fun testStringResourceFormatting() {
        val nodesText = com.xiaoyv.workflow.demo.support.stringResource(
            com.xiaoyv.workflow.demo.support.Res.string.workflow_run_nodes,
            "start → end",
        )
        kotlin.test.assertEquals("执行节点：start → end", nodesText)

        val urlText = com.xiaoyv.workflow.demo.support.stringResource(
            com.xiaoyv.workflow.demo.support.Res.string.workflow_run_opened_url,
            "https://bgm.tv",
        )
        kotlin.test.assertEquals("打开链接：https://bgm.tv", urlText)
    }
}