package com.xiaoyv.workflow.node.builtin.extension

import com.xiaoyv.workflow.model.spec.ActionBilibiliConfigKey
import com.xiaoyv.workflow.node.core.ActionConfigFieldSpec
import com.xiaoyv.workflow.node.core.ActionEditorFieldKind
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import kotlinx.collections.immutable.persistentListOf

/**
 * 哔哩哔哩节点的集中编辑器说明目录。
 */
internal object BilibiliNodeEditorCatalog {
    val signUrl =
        ActionNodeEditorSpec(
            title = "哔哩哔哩 URL 签名",
            description = "为哔哩哔哩请求 URL 生成 WBI 签名。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionBilibiliConfigKey.URL,
                        label = "请求 URL",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionBilibiliConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        order = 1,
                        required = true,
                    ),
                ),
        )
}
