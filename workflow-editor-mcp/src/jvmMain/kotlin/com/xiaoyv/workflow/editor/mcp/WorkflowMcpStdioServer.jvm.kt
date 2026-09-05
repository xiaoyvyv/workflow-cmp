package com.xiaoyv.workflow.editor.mcp

import java.io.BufferedReader
import java.io.PrintWriter

fun WorkflowMcpStdioServer.serve(input: BufferedReader, output: PrintWriter) {
    input.lineSequence().forEach { line -> handle(line)?.let(output::println) }
    output.flush()
}
