package com.xiaoyv.workflow.demo.support

@DslMarker
annotation class LogScopeDsl

@LogScopeDsl
data class LogScope(var tag: String = "WorkflowDemo")

inline fun debugLog(crossinline message: LogScope.() -> Any) {
    LogScope().message()
}
