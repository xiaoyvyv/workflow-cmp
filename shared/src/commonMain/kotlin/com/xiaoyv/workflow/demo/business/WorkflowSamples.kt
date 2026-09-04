package com.xiaoyv.workflow.demo.business

import com.xiaoyv.workflow.demo.business.samples.ActionSamples
import com.xiaoyv.workflow.demo.business.samples.BilibiliSamples
import com.xiaoyv.workflow.demo.business.samples.BusinessSamples
import com.xiaoyv.workflow.demo.business.samples.CodecSamples
import com.xiaoyv.workflow.demo.business.samples.CryptoSamples
import com.xiaoyv.workflow.demo.business.samples.DataSamples
import com.xiaoyv.workflow.demo.business.samples.ErrorSamples
import com.xiaoyv.workflow.demo.business.samples.FlowSamples
import com.xiaoyv.workflow.demo.business.samples.HtmlSamples
import com.xiaoyv.workflow.demo.business.samples.IoSamples
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import kotlinx.collections.immutable.toPersistentList

/**
 * 内置节点的可运行回归样例门面。
 */
object WorkflowSamples {
    /**
     * 所有当前内置节点的测试工作流列表。
     */
    val all: List<ActionWorkflow> = buildList {
        // 错误与异常故障测试样例
        addAll(ErrorSamples.all)

        // 示例工作流（复合业务实操样例）
        addAll(BusinessSamples.all)

        // 各模块节点测试用例
        addAll(FlowSamples.all)
        addAll(DataSamples.all)
        addAll(CodecSamples.all)
        addAll(HtmlSamples.all)
        addAll(CryptoSamples.all)
        addAll(IoSamples.all)
        addAll(ActionSamples.all)
        addAll(BilibiliSamples.all)
    }.toPersistentList()

    /**
     * 按稳定 ID 查询测试工作流。
     */
    fun find(id: String): ActionWorkflow? = all.firstOrNull { it.id == id }
}
