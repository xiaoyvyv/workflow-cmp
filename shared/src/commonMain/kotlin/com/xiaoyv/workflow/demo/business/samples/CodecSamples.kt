package com.xiaoyv.workflow.demo.business.samples

import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.spec.ActionCodecConfigKey
import com.xiaoyv.workflow.model.spec.ActionNodeType

/**
 * workflow-node-codec 编解码测试工作流样例集合。
 */
internal object CodecSamples {
    val all: List<ActionWorkflow> = listOf(
        linear(
            "codec_base64_encode",
            "Base64 编码",
            ActionNodeType.CODEC_BASE64_ENCODE,
            config(
                ActionCodecConfigKey.TEXT to "Hello, Workflow!",
                ActionCodecConfigKey.OUTPUT_KEY to "b64"
            )
        ),
        linear(
            "codec_base64_decode",
            "Base64 解码",
            ActionNodeType.CODEC_BASE64_DECODE,
            config(
                ActionCodecConfigKey.TEXT to "SGVsbG8sIFdvcmtmbG93IQ==",
                ActionCodecConfigKey.OUTPUT_KEY to "raw"
            )
        ),
        linear(
            "codec_base64_url_encode",
            "Base64 URL 编码",
            ActionNodeType.CODEC_BASE64_URL_ENCODE,
            config(
                ActionCodecConfigKey.TEXT to "Hello, Workflow?",
                ActionCodecConfigKey.OUTPUT_KEY to "b64url"
            )
        ),
        linear(
            "codec_base64_url_decode",
            "Base64 URL 解码",
            ActionNodeType.CODEC_BASE64_URL_DECODE,
            config(
                ActionCodecConfigKey.TEXT to "SGVsbG8sIFdvcmtmbG93Pw",
                ActionCodecConfigKey.OUTPUT_KEY to "rawurl"
            )
        ),
        linear(
            "codec_hex_encode",
            "Hex 十六进制编码",
            ActionNodeType.CODEC_HEX_ENCODE,
            config(
                ActionCodecConfigKey.TEXT to "Hello",
                ActionCodecConfigKey.OUTPUT_KEY to "hex"
            )
        ),
        linear(
            "codec_hex_decode",
            "Hex 十六进制解码",
            ActionNodeType.CODEC_HEX_DECODE,
            config(
                ActionCodecConfigKey.TEXT to "48656c6c6f",
                ActionCodecConfigKey.OUTPUT_KEY to "rawHex"
            )
        ),
        linear(
            "codec_url_encode",
            "URL 百分号编码",
            ActionNodeType.CODEC_URL_ENCODE,
            config(
                ActionCodecConfigKey.TEXT to "孤独摇滚！",
                ActionCodecConfigKey.OUTPUT_KEY to "encoded"
            )
        ),
        linear(
            "codec_url_decode",
            "URL 百分号解码",
            ActionNodeType.CODEC_URL_DECODE,
            config(
                ActionCodecConfigKey.TEXT to "%E5%AD%A4%E7%8B%AC%E6%91%87%E6%BB%9A%EF%BC%81",
                ActionCodecConfigKey.OUTPUT_KEY to "decoded"
            )
        ),
        linear(
            "codec_html_escape",
            "HTML 字符实体转义",
            ActionNodeType.CODEC_HTML_ESCAPE,
            config(
                ActionCodecConfigKey.TEXT to "<div class=\"title\">Hello & Welcome</div>",
                ActionCodecConfigKey.OUTPUT_KEY to "escaped"
            )
        ),
        linear(
            "codec_html_unescape",
            "HTML 字符实体反转义",
            ActionNodeType.CODEC_HTML_UNESCAPE,
            config(
                ActionCodecConfigKey.TEXT to "&lt;div class=&quot;title&quot;&gt;Hello &amp; Welcome&lt;/div&gt;",
                ActionCodecConfigKey.OUTPUT_KEY to "unescaped"
            )
        ),
    )
}
