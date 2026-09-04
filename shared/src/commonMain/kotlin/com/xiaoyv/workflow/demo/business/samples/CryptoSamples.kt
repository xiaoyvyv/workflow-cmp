package com.xiaoyv.workflow.demo.business.samples

import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.spec.ActionCryptoConfigKey
import com.xiaoyv.workflow.model.spec.ActionHashAlgorithm
import com.xiaoyv.workflow.model.spec.ActionNodeType

/**
 * workflow-node-crypto 加解密与摘要测试工作流样例集合。
 */
internal object CryptoSamples {
    val all: List<ActionWorkflow> = listOf(
        linear(
            "crypto_hash_sha256",
            "哈希计算 (SHA-256)",
            ActionNodeType.CRYPTO_HASH,
            config(
                ActionCryptoConfigKey.TEXT to "Hello, Workflow!",
                ActionCryptoConfigKey.ALGORITHM to ActionHashAlgorithm.SHA_256,
                ActionCryptoConfigKey.OUTPUT_KEY to "hashSha256"
            )
        ),
        linear(
            "crypto_hash_md5",
            "哈希计算 (MD5)",
            ActionNodeType.CRYPTO_HASH,
            config(
                ActionCryptoConfigKey.TEXT to "Hello, Workflow!",
                ActionCryptoConfigKey.ALGORITHM to ActionHashAlgorithm.MD5,
                ActionCryptoConfigKey.OUTPUT_KEY to "hashMd5"
            )
        ),
        linear(
            "crypto_hmac",
            "HMAC 签名",
            ActionNodeType.CRYPTO_HMAC,
            config(
                ActionCryptoConfigKey.TEXT to "Hello, Workflow!",
                ActionCryptoConfigKey.SECRET to "my_secret_key",
                ActionCryptoConfigKey.ALGORITHM to ActionHashAlgorithm.SHA_256,
                ActionCryptoConfigKey.OUTPUT_KEY to "hmac"
            )
        ),
        linear(
            "crypto_encrypt",
            "AES 对称加密",
            ActionNodeType.CRYPTO_ENCRYPT,
            config(
                ActionCryptoConfigKey.TEXT to "Sensitive Data 123",
                ActionCryptoConfigKey.SECRET_KEY to "1234567890123456",
                ActionCryptoConfigKey.OUTPUT_KEY to "encryptedHex"
            )
        ),
        linear(
            "crypto_decrypt",
            "AES 对称解密",
            ActionNodeType.CRYPTO_DECRYPT,
            config(
                ActionCryptoConfigKey.TEXT to "4c0423c8e4baee60b411d51a66a1a8c3eefee64b85c792caea680e5e0192d19b",
                ActionCryptoConfigKey.SECRET_KEY to "1234567890123456",
                ActionCryptoConfigKey.OUTPUT_KEY to "decryptedText"
            )
        ),
        linear(
            "crypto_random_bytes",
            "生成伪随机字节 Hex",
            ActionNodeType.CRYPTO_RANDOM_BYTES,
            config(
                ActionCryptoConfigKey.KEY_BYTES to 16,
                ActionCryptoConfigKey.OUTPUT_KEY to "randomHex"
            )
        ),
        linear(
            "crypto_uuid",
            "生成 Crypto UUID",
            ActionNodeType.CRYPTO_UUID,
            config(
                ActionCryptoConfigKey.OUTPUT_KEY to "uuid"
            )
        ),
    )
}
