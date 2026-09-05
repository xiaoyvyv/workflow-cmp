package com.xiaoyv.workflow.editor.mcp

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * 面向本地 AI 宿主的按行分隔 JSON-RPC MCP 服务。
 *
 * 全平台通用。
 */
class WorkflowMcpStdioServer(
    private val service: WorkflowMcpService,
    private val json: Json,
) {
    fun serve(lines: Sequence<String>, emit: (String) -> Unit) {
        lines.forEach { line -> handle(line)?.let(emit) }
    }

    fun handle(line: String): String? {
        val request =
            try {
                json.parseToJsonElement(line).jsonObject
            } catch (_: Exception) {
                return json.encodeToString(
                    JsonObject.serializer(),
                    error(null, JsonRpcErrorCode.PARSE_ERROR, "Parse error"),
                )
            }
        val id = request["id"]
        val method =
            request["method"]?.jsonPrimitive?.content
                ?: return json.encodeToString(
                    JsonObject.serializer(),
                    error(id, JsonRpcErrorCode.INVALID_REQUEST, "Invalid request"),
                )
        val result =
            try {
                dispatch(method, request["params"]?.jsonObject ?: JsonObject(emptyMap()))
            } catch (exception: IllegalArgumentException) {
                return id?.let {
                    json.encodeToString(
                        JsonObject.serializer(),
                        error(
                            it,
                            JsonRpcErrorCode.INVALID_PARAMS,
                            exception.message ?: "Invalid params",
                        ),
                    )
                }
            } catch (exception: Exception) {
                return id?.let {
                    json.encodeToString(
                        JsonObject.serializer(),
                        error(
                            it,
                            JsonRpcErrorCode.INTERNAL_ERROR,
                            exception.message ?: "Internal error",
                        ),
                    )
                }
            }
        // JSON-RPC 通知不返回响应。
        return id?.let { json.encodeToString(JsonObject.serializer(), response(it, result)) }
    }

    private fun dispatch(method: String, params: JsonObject): JsonObject =
        when (method) {
            "initialize",
            "server/discover" ->
                buildJsonObject {
                    put("protocolVersion", JsonPrimitive(PROTOCOL_VERSION))
                    put(
                        "serverInfo",
                        buildJsonObject {
                            put("name", JsonPrimitive("workflow-editor"))
                            put("version", JsonPrimitive("1"))
                        },
                    )
                    put(
                        "capabilities",
                        buildJsonObject {
                            put(
                                "resources",
                                buildJsonObject { put("listChanged", JsonPrimitive(false)) },
                            )
                            put("tools", buildJsonObject {})
                        },
                    )
                }

            "notifications/initialized" -> JsonObject(emptyMap())
            "resources/list" ->
                buildJsonObject {
                    put(
                        "resources",
                        buildJsonArray {
                            add(
                                resource(
                                    WorkflowMcpService.ARCHITECTURE_URI,
                                    "Workflow editor architecture",
                                )
                            )
                            add(
                                resource(
                                    WorkflowMcpService.MANIFEST_URI,
                                    "Registered workflow nodes and editor schema",
                                )
                            )
                        },
                    )
                }

            "resources/templates/list" ->
                buildJsonObject {
                    put(
                        "resourceTemplates",
                        buildJsonArray {
                            add(
                                buildJsonObject {
                                    put("uriTemplate", JsonPrimitive("workflow://nodes/{type}"))
                                    put("name", JsonPrimitive("Workflow node"))
                                    put("mimeType", JsonPrimitive("application/json"))
                                }
                            )
                            add(
                                buildJsonObject {
                                    put(
                                        "uriTemplate",
                                        JsonPrimitive("workflow://categories/{category}"),
                                    )
                                    put("name", JsonPrimitive("Workflow node category"))
                                    put("mimeType", JsonPrimitive("application/json"))
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("uriTemplate", JsonPrimitive("workflow://workflows/{id}"))
                                    put("name", JsonPrimitive("Saved workflow"))
                                    put("mimeType", JsonPrimitive("application/json"))
                                }
                            )
                        },
                    )
                }

            "resources/read" -> {
                val uri =
                    params["uri"]?.jsonPrimitive?.content
                        ?: throw IllegalArgumentException("uri is required")
                val text =
                    if (uri.startsWith("workflow://workflows/"))
                        runBlocking {
                            service.getWorkflow(uri.removePrefix("workflow://workflows/"))?.let {
                                json.encodeToString(
                                    com.xiaoyv.workflow.editor.bridge.contract
                                        .EditorWorkflowDocument
                                        .serializer(),
                                    it,
                                )
                            } ?: throw IllegalArgumentException("Unknown workflow")
                        }
                    else service.readResource(uri)
                buildJsonObject {
                    put(
                        "contents",
                        buildJsonArray {
                            add(
                                buildJsonObject {
                                    put("uri", JsonPrimitive(uri))
                                    put(
                                        "mimeType",
                                        JsonPrimitive(
                                            if (uri == WorkflowMcpService.ARCHITECTURE_URI)
                                                "text/markdown"
                                            else "application/json"
                                        ),
                                    )
                                    put("text", JsonPrimitive(text))
                                }
                            )
                        },
                    )
                }
            }

            "tools/list" ->
                buildJsonObject {
                    put(
                        "tools",
                        buildJsonArray {
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("workflow_validate"))
                                    put(
                                        "description",
                                        JsonPrimitive(
                                            "Validate a generated workflow against the device node registry."
                                        ),
                                    )
                                    put(
                                        "inputSchema",
                                        buildJsonObject {
                                            put("type", JsonPrimitive("object"))
                                            put(
                                                "properties",
                                                buildJsonObject {
                                                    put(
                                                        "workflow",
                                                        buildJsonObject {
                                                            put(
                                                                "description",
                                                                JsonPrimitive(
                                                                    "ActionWorkflow JSON object"
                                                                ),
                                                            )
                                                        },
                                                    )
                                                },
                                            )
                                            put(
                                                "required",
                                                buildJsonArray { add(JsonPrimitive("workflow")) },
                                            )
                                        },
                                    )
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("validate_workflow"))
                                    put("description", JsonPrimitive("Alias of workflow_validate."))
                                    put("inputSchema", workflowInputSchema())
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("list_node_types"))
                                    put(
                                        "description",
                                        JsonPrimitive(
                                            "List registered node types, optionally filtered by category or text."
                                        ),
                                    )
                                    put("inputSchema", listNodesInputSchema())
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("get_node_spec"))
                                    put(
                                        "description",
                                        JsonPrimitive(
                                            "Get one node's complete editor specification."
                                        ),
                                    )
                                    put("inputSchema", nodeTypeInputSchema())
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("workflow_create"))
                                    put(
                                        "description",
                                        JsonPrimitive(
                                            "Validate and create a workflow at revision zero."
                                        ),
                                    )
                                    put("inputSchema", saveInputSchema(true))
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("workflow_update"))
                                    put(
                                        "description",
                                        JsonPrimitive(
                                            "Validate and update a workflow with optimistic revision control."
                                        ),
                                    )
                                    put("inputSchema", saveInputSchema(false))
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("workflow_get"))
                                    put(
                                        "description",
                                        JsonPrimitive("Read a saved workflow and its revision."),
                                    )
                                    put(
                                        "inputSchema",
                                        buildJsonObject {
                                            put("type", JsonPrimitive("object"))
                                            put(
                                                "properties",
                                                buildJsonObject {
                                                    put(
                                                        "id",
                                                        buildJsonObject {
                                                            put("type", JsonPrimitive("string"))
                                                        },
                                                    )
                                                },
                                            )
                                            put(
                                                "required",
                                                buildJsonArray { add(JsonPrimitive("id")) },
                                            )
                                        },
                                    )
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("create_workflow"))
                                    put("description", JsonPrimitive("Alias of workflow_create."))
                                    put("inputSchema", saveInputSchema(true))
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("update_workflow"))
                                    put("description", JsonPrimitive("Alias of workflow_update."))
                                    put("inputSchema", saveInputSchema(false))
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("get_workflow"))
                                    put("description", JsonPrimitive("Alias of workflow_get."))
                                    put("inputSchema", workflowIdInputSchema())
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("workflow_run"))
                                    put(
                                        "description",
                                        JsonPrimitive(
                                            "Explicitly request device-side execution of a saved workflow."
                                        ),
                                    )
                                    put("inputSchema", runInputSchema())
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("run_cancel"))
                                    put("description", JsonPrimitive("Cancel a device-side run."))
                                    put(
                                        "inputSchema",
                                        buildJsonObject {
                                            put("type", JsonPrimitive("object"))
                                            put(
                                                "properties",
                                                buildJsonObject {
                                                    put(
                                                        "runId",
                                                        buildJsonObject {
                                                            put("type", JsonPrimitive("string"))
                                                        },
                                                    )
                                                },
                                            )
                                            put(
                                                "required",
                                                buildJsonArray { add(JsonPrimitive("runId")) },
                                            )
                                        },
                                    )
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("run_get_status"))
                                    put(
                                        "description",
                                        JsonPrimitive(
                                            "Read a device-side run status and bounded event history."
                                        ),
                                    )
                                    put(
                                        "inputSchema",
                                        buildJsonObject {
                                            put("type", JsonPrimitive("object"))
                                            put(
                                                "properties",
                                                buildJsonObject {
                                                    put(
                                                        "runId",
                                                        buildJsonObject {
                                                            put("type", JsonPrimitive("string"))
                                                        },
                                                    )
                                                },
                                            )
                                            put(
                                                "required",
                                                buildJsonArray { add(JsonPrimitive("runId")) },
                                            )
                                        },
                                    )
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("run_workflow"))
                                    put("description", JsonPrimitive("Alias of workflow_run."))
                                    put("inputSchema", runInputSchema())
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("cancel_run"))
                                    put("description", JsonPrimitive("Alias of run_cancel."))
                                    put("inputSchema", runIdInputSchema())
                                }
                            )
                            add(
                                buildJsonObject {
                                    put("name", JsonPrimitive("get_run_status"))
                                    put("description", JsonPrimitive("Alias of run_get_status."))
                                    put("inputSchema", runIdInputSchema())
                                }
                            )
                        },
                    )
                }

            "tools/call" -> callTool(params)
            else -> throw IllegalArgumentException("Method not found: $method")
        }

    private fun callTool(params: JsonObject): JsonObject {
        val arguments = params["arguments"]?.jsonObject ?: JsonObject(emptyMap())
        val name =
            params["name"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Tool name is required")
        return when (name) {
            "workflow_get",
            "get_workflow" -> getResult(arguments)

            "workflow_run",
            "run_workflow" -> runResult(arguments)

            "run_cancel",
            "cancel_run" -> cancelResult(arguments)

            "run_get_status",
            "get_run_status" -> statusResult(arguments)

            "workflow_validate",
            "validate_workflow" ->
                validationResult(
                    service.validateWorkflow(
                        json.encodeToString(
                            JsonElement.serializer(),
                            arguments["workflow"]
                                ?: throw IllegalArgumentException("workflow is required"),
                        )
                    )
                )

            "list_node_types" -> listNodesResult(arguments)
            "get_node_spec" -> nodeSpecResult(arguments)
            "workflow_create",
            "workflow_update",
            "create_workflow",
            "update_workflow" ->
                saveResult(
                    if (name == "create_workflow") "workflow_create"
                    else if (name == "update_workflow") "workflow_update" else name,
                    arguments,
                    arguments["workflow"] ?: throw IllegalArgumentException("workflow is required"),
                )

            else -> throw IllegalArgumentException("Unknown tool")
        }
    }

    private fun runResult(arguments: JsonObject): JsonObject {
        val id =
            arguments["id"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("id is required")
        val revision = arguments["revision"]?.jsonPrimitive?.longOrNull
        return when (val result = runBlocking { service.runWorkflow(id, revision) }) {
            is WorkflowMcpRunResult.Started ->
                toolResult(
                    "Workflow started",
                    buildJsonObject {
                        put("runId", JsonPrimitive(result.runId))
                        put("workflowId", JsonPrimitive(result.workflowId))
                        put("revision", JsonPrimitive(result.revision))
                    },
                    false,
                )

            WorkflowMcpRunResult.NotFound ->
                toolResult("Workflow not found", JsonObject(emptyMap()), true)

            WorkflowMcpRunResult.Unavailable ->
                toolResult("Execution is unavailable", JsonObject(emptyMap()), true)
        }
    }

    private fun cancelResult(arguments: JsonObject): JsonObject {
        val runId =
            arguments["runId"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("runId is required")
        val cancelled = runBlocking { service.cancelRun(runId) }
        return toolResult(
            if (cancelled) "Run cancelled" else "Run not found or unavailable",
            JsonObject(emptyMap()),
            !cancelled,
        )
    }

    private fun statusResult(arguments: JsonObject): JsonObject {
        val runId =
            arguments["runId"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("runId is required")
        val status =
            runBlocking { service.getRunStatus(runId) }
                ?: return toolResult("Run not found or unavailable", JsonObject(emptyMap()), true)
        return toolResult(
            "Run status",
            json.encodeToJsonElement(
                com.xiaoyv.workflow.editor.bridge.contract.EditorRunStatus.serializer(),
                status,
            ),
            false,
        )
    }

    private fun getResult(arguments: JsonObject): JsonObject {
        val id =
            arguments["id"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("id is required")
        val document =
            runBlocking { service.getWorkflow(id) }
                ?: throw IllegalArgumentException("Unknown workflow")
        return toolResult(
            "Workflow loaded",
            json.encodeToJsonElement(
                com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowDocument.serializer(),
                document,
            ),
            false,
        )
    }

    private fun listNodesResult(arguments: JsonObject): JsonObject =
        toolResult(
            "Node types",
            json.parseToJsonElement(
                service.listNodeTypes(
                    arguments["category"]?.jsonPrimitive?.contentOrNull,
                    arguments["query"]?.jsonPrimitive?.contentOrNull,
                )
            ),
            false,
        )

    private fun nodeSpecResult(arguments: JsonObject): JsonObject {
        val type =
            arguments["type"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("type is required")
        return toolResult(
            "Node specification",
            json.parseToJsonElement(service.getNodeSpec(type)),
            false,
        )
    }

    private fun workflowInputSchema() = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                put(
                    "workflow",
                    buildJsonObject {
                        put("description", JsonPrimitive("ActionWorkflow JSON object"))
                    },
                )
            },
        )
        put("required", buildJsonArray { add(JsonPrimitive("workflow")) })
    }

    private fun workflowIdInputSchema() = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject { put("id", buildJsonObject { put("type", JsonPrimitive("string")) }) },
        )
        put("required", buildJsonArray { add(JsonPrimitive("id")) })
    }

    private fun nodeTypeInputSchema() = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                put("type", buildJsonObject { put("type", JsonPrimitive("string")) })
            },
        )
        put("required", buildJsonArray { add(JsonPrimitive("type")) })
    }

    private fun listNodesInputSchema() = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                put("category", buildJsonObject { put("type", JsonPrimitive("string")) })
                put("query", buildJsonObject { put("type", JsonPrimitive("string")) })
            },
        )
    }

    private fun runIdInputSchema() = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                put("runId", buildJsonObject { put("type", JsonPrimitive("string")) })
            },
        )
        put("required", buildJsonArray { add(JsonPrimitive("runId")) })
    }

    private fun saveInputSchema(creation: Boolean) = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                put("id", buildJsonObject { put("type", JsonPrimitive("string")) })
                put("workflow", buildJsonObject {})
                if (!creation)
                    put("baseRevision", buildJsonObject { put("type", JsonPrimitive("integer")) })
            },
        )
        put(
            "required",
            buildJsonArray {
                add(JsonPrimitive("id"))
                add(JsonPrimitive("workflow"))
                if (!creation) add(JsonPrimitive("baseRevision"))
            },
        )
    }

    private fun runInputSchema() = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                put("id", buildJsonObject { put("type", JsonPrimitive("string")) })
                put("revision", buildJsonObject { put("type", JsonPrimitive("integer")) })
            },
        )
        put("required", buildJsonArray { add(JsonPrimitive("id")) })
    }

    private fun saveResult(name: String, arguments: JsonObject, workflow: JsonElement): JsonObject {
        val id =
            arguments["id"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("id is required")
        val baseRevision =
            arguments["baseRevision"]?.jsonPrimitive?.longOrNull
                ?: if (name == "workflow_create") 0
                else throw IllegalArgumentException("baseRevision is required")
        return when (
            val saved = runBlocking {
                service.saveWorkflow(
                    id,
                    baseRevision,
                    json.encodeToString(JsonElement.serializer(), workflow),
                )
            }
        ) {
            is com.xiaoyv.workflow.editor.bridge.contract.EditorSaveResult.Saved ->
                toolResult(
                    "Workflow saved",
                    json.encodeToJsonElement(
                        com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowDocument
                            .serializer(),
                        saved.document,
                    ),
                    false,
                )

            is com.xiaoyv.workflow.editor.bridge.contract.EditorSaveResult.Conflict ->
                toolResult(
                    "Workflow revision conflict",
                    buildJsonObject {
                        put(
                            "current",
                            saved.current?.let {
                                json.encodeToJsonElement(
                                    com.xiaoyv.workflow.editor.bridge.contract
                                        .EditorWorkflowDocument
                                        .serializer(),
                                    it,
                                )
                            } ?: kotlinx.serialization.json.JsonNull,
                        )
                    },
                    true,
                )

            is com.xiaoyv.workflow.editor.bridge.contract.EditorSaveResult.Invalid ->
                validationResult(saved.response)
        }
    }

    private fun validationResult(
        validation: com.xiaoyv.workflow.editor.bridge.contract.ValidateWorkflowResponse
    ) =
        toolResult(
            if (validation.validation.isValid) "Workflow is valid"
            else "Workflow validation failed",
            json.encodeToJsonElement(
                com.xiaoyv.workflow.editor.bridge.contract.ValidateWorkflowResponse.serializer(),
                validation,
            ),
            !validation.validation.isValid,
        )

    private fun toolResult(message: String, structured: JsonElement, isError: Boolean) =
        buildJsonObject {
            put(
                "content",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("type", JsonPrimitive("text"))
                            put("text", JsonPrimitive(message))
                        }
                    )
                },
            )
            put("structuredContent", structured)
            put("isError", JsonPrimitive(isError))
        }

    private fun resource(uri: String, name: String) = buildJsonObject {
        put("uri", JsonPrimitive(uri))
        put("name", JsonPrimitive(name))
        put(
            "mimeType",
            JsonPrimitive(
                if (uri == WorkflowMcpService.ARCHITECTURE_URI) "text/markdown"
                else "application/json"
            ),
        )
    }

    private fun response(id: JsonElement, result: JsonObject) = buildJsonObject {
        put("jsonrpc", JsonPrimitive("2.0"))
        put("id", id)
        put("result", result)
    }

    private fun error(id: JsonElement?, code: Int, message: String) = buildJsonObject {
        put("jsonrpc", JsonPrimitive("2.0"))
        put("id", id ?: kotlinx.serialization.json.JsonNull)
        put(
            "error",
            buildJsonObject {
                put("code", JsonPrimitive(code))
                put("message", JsonPrimitive(message))
            },
        )
    }

    companion object {
        const val PROTOCOL_VERSION = "2025-11-25"
    }
}

/**
 * JSON-RPC 标准错误码；通过名称明确调用处的协议语义。
 */
private object JsonRpcErrorCode {
    const val PARSE_ERROR = -32700
    const val INVALID_REQUEST = -32600
    const val INVALID_PARAMS = -32602
    const val INTERNAL_ERROR = -32603
}
