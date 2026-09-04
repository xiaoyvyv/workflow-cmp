package com.xiaoyv.workflow.port

import com.xiaoyv.workflow.port.impl.DefaultActionWorkflowFileStorage
import io.github.vinceglb.filekit.PlatformFile

/**
 * 工作流文件沙箱端口。
 */
interface ActionWorkflowFileStorage {
    /**
     * 工作流文件沙箱的基础目录。
     */
    val homeDir: PlatformFile

    /**
     * 将工作流相对路径解析为已校验的沙箱内路径。
     */
    fun resolveSandboxPath(workflowId: String, path: String): PlatformFile

    /**
     * 读取 UTF-8 文本文件。
     */
    suspend fun readText(workflowId: String, path: String): String

    /**
     * 写入 UTF-8 文本文件。
     */
    suspend fun writeText(workflowId: String, path: String, text: String, append: Boolean)

    /**
     * 创建空文件；若目标已经存在，则不修改原内容。
     */
    suspend fun createFile(workflowId: String, path: String)

    /**
     * 写入二进制文件内容。
     */
    suspend fun writeBytes(workflowId: String, path: String, bytes: ByteArray, append: Boolean = false)

    /**
     * 获取当前工作流的文件沙箱目录绝对路径。
     */
    fun workingDirectory(workflowId: String): String

    /**
     * 删除文件或目录。
     */
    suspend fun delete(workflowId: String, path: String)

    /**
     * 判断文件或目录是否存在。
     */
    suspend fun exists(workflowId: String, path: String): Boolean

    /**
     * 创建目录及其缺失父目录。
     */
    suspend fun mkdir(workflowId: String, path: String)

    /**
     * 列出目录中直接包含的文件和目录相对路径。
     */
    suspend fun list(workflowId: String, path: String): List<String>

    /**
     * 复制文件或目录。
     */
    suspend fun copy(workflowId: String, fromPath: String, toPath: String)

    /**
     * 移动或重命名文件、目录。
     */
    suspend fun move(workflowId: String, fromPath: String, toPath: String)

    /**
     * 将沙箱内的文件或目录压缩为 ZIP 文件。
     */
    suspend fun compressZip(workflowId: String, paths: List<String>, toPath: String)

    /**
     * 将 ZIP 文件安全解压至沙箱内目录。
     */
    suspend fun extractZip(workflowId: String, fromPath: String, toPath: String)

    companion object {
        val Default = DefaultActionWorkflowFileStorage()
    }
}

