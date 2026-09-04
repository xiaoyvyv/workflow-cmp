package com.xiaoyv.workflow.demo.business

import com.xiaoyv.workflow.demo.support.Screen
import com.xiaoyv.workflow.engine.ActionSideEffectResult

/**
 * [WorkflowsEvent]
 *
 * @author why
 * @since 2025/1/12
 */
sealed class WorkflowsEvent {
    sealed class UI : WorkflowsEvent() {
        data object OnNavUp : UI()
        data class OnNavScreen(val screen: Screen) : UI()
    }

    sealed class Action : WorkflowsEvent() {
        data class OnRefresh(val loading: Boolean) : Action()
        data class OnRunSample(val sampleId: String) : Action()
        data class OnSideEffectResult(val id: String, val result: ActionSideEffectResult) : Action()
    }
}
