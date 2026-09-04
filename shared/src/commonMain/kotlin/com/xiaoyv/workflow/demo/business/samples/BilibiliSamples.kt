package com.xiaoyv.workflow.demo.business.samples

import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.spec.ActionBilibiliConfigKey
import com.xiaoyv.workflow.model.spec.ActionNodeType

/**
 * workflow-node-bilibili 业务扩展节点测试工作流样例集合。
 */
internal object BilibiliSamples {
    val all: List<ActionWorkflow> = listOf(
        linear(
            "bilibili_sign_url",
            "Bilibili WBI 链接加签",
            ActionNodeType.BILIBILI_SIGN_URL,
            config(
                ActionBilibiliConfigKey.URL to "https://api.bilibili.com/x/space/wbi/acc/info?mid=123456",
                ActionBilibiliConfigKey.OUTPUT_KEY to "signedUrl",
            )
        )
    )
}
