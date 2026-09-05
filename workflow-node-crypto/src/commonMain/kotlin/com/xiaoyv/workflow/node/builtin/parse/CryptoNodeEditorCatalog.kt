package com.xiaoyv.workflow.node.builtin.parse

import com.xiaoyv.workflow.model.spec.ActionCryptoAlgorithm
import com.xiaoyv.workflow.model.spec.ActionCryptoConfigKey
import com.xiaoyv.workflow.node.core.ActionConfigFieldSpec
import com.xiaoyv.workflow.node.core.ActionEditorFieldKind
import com.xiaoyv.workflow.node.core.ActionEditorFieldOption
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonPrimitive

/**
 * 加密模块的集中编辑器说明目录。
 */
internal object CryptoNodeEditorCatalog {
    val hash =
        ActionNodeEditorSpec(
            title = "计算哈希",
            description = "计算输入文本的哈希散列值（支持 MD5、SHA-256、SHA-512、SM3、CRC32 等算法）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "待计算哈希的文本内容",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.ALGORITHM,
                        label = "哈希算法",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionCryptoAlgorithm.SHA_256),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.MD5), "MD5"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_1), "SHA-1"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_224), "SHA-224"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_256), "SHA-256"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_384), "SHA-384"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_512), "SHA-512"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA3_224), "SHA3-224"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA3_256), "SHA3-256"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA3_384), "SHA3-384"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA3_512), "SHA3-512"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SM3), "SM3"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.CRC32), "CRC32"),
                            ),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("hash"),
                        placeholder = "存放哈希结果的字段名",
                        order = 2,
                    ),
                ),
        )

    val hmac =
        ActionNodeEditorSpec(
            title = "计算 HMAC",
            description = "使用指定密钥和哈希算法计算消息认证码 (HMAC)。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "待计算 HMAC 的文本内容",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.SECRET,
                        label = "HMAC 密钥",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        sensitive = true,
                        placeholder = "用于 HMAC 计算的密钥",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.ALGORITHM,
                        label = "哈希算法",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionCryptoAlgorithm.SHA_256),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.MD5), "MD5"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_1), "SHA-1"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_224), "SHA-224"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_256), "SHA-256"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_384), "SHA-384"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA_512), "SHA-512"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA3_224), "SHA3-224"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA3_256), "SHA3-256"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA3_384), "SHA3-384"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SHA3_512), "SHA3-512"),
                                ActionEditorFieldOption(JsonPrimitive(ActionCryptoAlgorithm.SM3), "SM3"),
                            ),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("hmac"),
                        placeholder = "存放 HMAC 结果的字段名",
                        order = 3,
                    ),
                ),
        )

    val encrypt =
        ActionNodeEditorSpec(
            title = "对称加密",
            description = "使用密钥对文本内容进行加密并输出十六进制字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.TEXT,
                        label = "明文内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "待加密的原始文本内容",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.SECRET_KEY,
                        label = "加密密钥",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        sensitive = true,
                        placeholder = "对称加密密钥",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("encryptedText"),
                        placeholder = "存放密文字符串的字段名",
                        order = 2,
                    ),
                ),
        )

    val decrypt =
        ActionNodeEditorSpec(
            title = "对称解密",
            description = "使用密钥对十六进制密文进行解密还原为原始文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.TEXT,
                        label = "密文内容 (十六进制)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "待解密的十六进制密文字符串",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.SECRET_KEY,
                        label = "解密密钥",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        sensitive = true,
                        placeholder = "对称解密密钥",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("decryptedText"),
                        placeholder = "存放解密后明文的字段名",
                        order = 2,
                    ),
                ),
        )

    val randomBytes =
        ActionNodeEditorSpec(
            title = "生成随机字节",
            description = "生成指定长度的安全随机字节并输出为十六进制字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.KEY_BYTES,
                        label = "随机字节长度",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(16),
                        placeholder = "默认 16 字节",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("randomBytes"),
                        placeholder = "存放随机字节十六进制的字段名",
                        order = 1,
                    ),
                ),
        )

    val uuid =
        ActionNodeEditorSpec(
            title = "生成 UUID",
            description = "生成全新的随机通用唯一识别码 (UUID v4)。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCryptoConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("uuid"),
                        placeholder = "存放生成 UUID 的字段名",
                        order = 0,
                    ),
                ),
        )
}
