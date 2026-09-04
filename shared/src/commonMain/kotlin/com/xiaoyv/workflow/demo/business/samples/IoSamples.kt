package com.xiaoyv.workflow.demo.business.samples

import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.spec.ActionCapability
import com.xiaoyv.workflow.model.spec.ActionControlPortId
import com.xiaoyv.workflow.model.spec.ActionFileConfigKey
import com.xiaoyv.workflow.model.spec.ActionHttpConfigKey
import com.xiaoyv.workflow.model.spec.ActionNodeType
import com.xiaoyv.workflow.model.spec.ActionStorageConfigKey
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 文件沙箱、HTTP 网络请求与私有持久化存储节点测试样例集合（workflow-node-io）。
 */
internal object IoSamples {
    val all: List<ActionWorkflow> = buildList {
        // 1. Storage 私有持久化存储
        add(
            linear(
                "storage_preferences_set",
                "写入工作流持久化存储",
                ActionNodeType.STORAGE_PREFERENCES_SET,
                config(ActionStorageConfigKey.KEY to "sample_favorite", ActionStorageConfigKey.VALUE to true, ActionStorageConfigKey.OUTPUT_KEY to "savedFavorite"),
            )
        )
        add(
            linear(
                "storage_preferences_get",
                "读取工作流持久化存储",
                ActionNodeType.STORAGE_PREFERENCES_GET,
                config(ActionStorageConfigKey.KEY to "sample_favorite", ActionStorageConfigKey.OUTPUT_KEY to "savedFavorite"),
            )
        )
        add(
            linear(
                "storage_preferences_has",
                "检查工作流持久化存储",
                ActionNodeType.STORAGE_PREFERENCES_HAS,
                config(ActionStorageConfigKey.KEY to "sample_favorite", ActionStorageConfigKey.OUTPUT_KEY to "exists"),
            )
        )
        add(
            linear(
                "storage_preferences_delete",
                "删除工作流持久化存储",
                ActionNodeType.STORAGE_PREFERENCES_DELETE,
                config(ActionStorageConfigKey.KEY to "sample_favorite", ActionStorageConfigKey.OUTPUT_KEY to "deleted"),
            )
        )
        add(linear("storage_preferences_clear", "清空工作流持久化存储", ActionNodeType.STORAGE_PREFERENCES_CLEAR))

        // 2. HTTP 网络请求与下载
        add(
            linear(
                "action_http_request_get",
                "HTTP GET 请求 (带重试机制)",
                ActionNodeType.HTTP_REQUEST,
                config(
                    ActionHttpConfigKey.URL to "https://next.bgm.tv/p1/subjects/633836",
                    ActionHttpConfigKey.METHOD to "GET",
                    ActionHttpConfigKey.RETRY_COUNT to 2,
                    ActionHttpConfigKey.RETRY_DELAY_MILLIS to 300L,
                    ActionHttpConfigKey.USE_LOCAL_COOKIE_STORAGE to true,
                ),
                setOf(ActionCapability.NETWORK, ActionCapability.NETWORK_LOCAL_COOKIE_ACCESS),
            )
        )
        add(
            linear(
                "action_http_request_post",
                "HTTP POST 请求 (提交 JSON 数据)",
                ActionNodeType.HTTP_REQUEST,
                config(
                    ActionHttpConfigKey.URL to "https://httpbin.org/post",
                    ActionHttpConfigKey.METHOD to "POST",
                    ActionHttpConfigKey.BODY to """{"client":"workflow-cmp","version":"1.0.0"}""",
                    ActionHttpConfigKey.HEADERS to buildJsonObject {
                        put("Content-Type", "application/json")
                    },
                ),
                setOf(ActionCapability.NETWORK),
            )
        )
        add(
            linear(
                "action_http_download",
                "HTTP 文件下载 (保存至工作流沙箱)",
                ActionNodeType.HTTP_DOWNLOAD,
                config(
                    ActionHttpConfigKey.URL to "https://lain.bgm.tv/pic/cover/l/7e/00/633836_8p8p1.jpg",
                    ActionHttpConfigKey.PATH to "covers/bangumi_cover.jpg",
                    ActionHttpConfigKey.OUTPUT_KEY to "downloadPath",
                ),
                setOf(ActionCapability.NETWORK),
            )
        )

        // 3. File 工作流沙箱文件管理
        add(
            linear(
                "file_get_working_directory",
                "获取当前沙箱工作目录",
                ActionNodeType.FILE_GET_WORKING_DIRECTORY,
                config(ActionFileConfigKey.OUTPUT_KEY to "workDir"),
            )
        )
        add(
            linear(
                "file_mkdir",
                "在沙箱创建多级目录",
                ActionNodeType.FILE_MKDIR,
                config(ActionFileConfigKey.PATH to "data/export/json", ActionFileConfigKey.OUTPUT_KEY to "createdDir"),
            )
        )
        add(fileReadWriteSample())
        add(
            linear(
                "file_exists",
                "检查沙箱文件是否存在",
                ActionNodeType.FILE_EXISTS,
                config(ActionFileConfigKey.PATH to "logs/workflow.log", ActionFileConfigKey.OUTPUT_KEY to "fileExists"),
            )
        )
        add(
            linear(
                "file_copy",
                "复制沙箱文件",
                ActionNodeType.FILE_COPY,
                config(
                    ActionFileConfigKey.FROM_PATH to "logs/workflow.log",
                    ActionFileConfigKey.TO_PATH to "logs/workflow_backup.log",
                    ActionFileConfigKey.OUTPUT_KEY to "copiedPath",
                ),
            )
        )
        add(
            linear(
                "file_move",
                "移动/重命名沙箱文件",
                ActionNodeType.FILE_MOVE,
                config(
                    ActionFileConfigKey.FROM_PATH to "logs/workflow_backup.log",
                    ActionFileConfigKey.TO_PATH to "logs/archived_workflow.log",
                    ActionFileConfigKey.OUTPUT_KEY to "movedPath",
                ),
            )
        )
        add(
            linear(
                "file_list",
                "遍历沙箱目录文件列表",
                ActionNodeType.FILE_LIST,
                config(ActionFileConfigKey.PATH to "logs", ActionFileConfigKey.OUTPUT_KEY to "fileList"),
            )
        )
        add(fileZipSample())
        add(
            linear(
                "file_delete",
                "删除沙箱文件",
                ActionNodeType.FILE_DELETE,
                config(ActionFileConfigKey.PATH to "logs/archived_workflow.log", ActionFileConfigKey.OUTPUT_KEY to "deleted"),
            )
        )
    }

    /**
     * 沙箱文件读写完整工作流。
     */
    private fun fileReadWriteSample(): ActionWorkflow = workflow(
        id = "file_read_write",
        name = "沙箱文件：写入与读取",
        description = "向专属沙箱写入数据日志并随后读取内容，各工作流沙箱严格互相隔离。",
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node(
                "write",
                ActionNodeType.FILE_WRITE_TEXT,
                "写入日志",
                config(
                    ActionFileConfigKey.PATH to "logs/workflow.log",
                    ActionFileConfigKey.TEXT to "Workflow CMP initialized at 2026",
                    ActionFileConfigKey.OUTPUT_KEY to "written",
                ),
            ),
            node(
                "read",
                ActionNodeType.FILE_READ_TEXT,
                "读取日志",
                config(
                    ActionFileConfigKey.PATH to "logs/workflow.log",
                    ActionFileConfigKey.OUTPUT_KEY to "logContent",
                ),
            ),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "write"),
            edge("write", ActionControlPortId.NEXT, "read"),
            edge("read", ActionControlPortId.NEXT, "end"),
        ),
    )

    /**
     * 沙箱文件 Zip 压缩与解压工作流。
     */
    private fun fileZipSample(): ActionWorkflow = workflow(
        id = "file_zip_unzip",
        name = "沙箱文件：ZIP 压缩与解压",
        description = "将沙箱 logs 目录打包压缩为 archive.zip，并解压到 unzipped 目录。",
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node(
                "zip",
                ActionNodeType.FILE_COMPRESS_ZIP,
                "压缩目录",
                config(
                    ActionFileConfigKey.PATHS to listOf("logs"),
                    ActionFileConfigKey.TO_PATH to "archive.zip",
                    ActionFileConfigKey.OUTPUT_KEY to "zipPath",
                ),
            ),
            node(
                "unzip",
                ActionNodeType.FILE_EXTRACT_ZIP,
                "解压归档",
                config(
                    ActionFileConfigKey.FROM_PATH to "archive.zip",
                    ActionFileConfigKey.TO_PATH to "unzipped_logs",
                    ActionFileConfigKey.OUTPUT_KEY to "unzipPath",
                ),
            ),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "zip"),
            edge("zip", ActionControlPortId.NEXT, "unzip"),
            edge("unzip", ActionControlPortId.NEXT, "end"),
        ),
    )
}
