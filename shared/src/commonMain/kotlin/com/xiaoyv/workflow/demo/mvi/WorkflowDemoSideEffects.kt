package com.xiaoyv.workflow.demo.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun <STATE : Any, SIDE_EFFECT : Any, EVENT : Any> BaseViewModel<STATE, SIDE_EFFECT, EVENT>.collectBaseSideEffect(
    onEffect: suspend (SIDE_EFFECT) -> Unit,
) {
    LaunchedEffect(key1 = this) {
        container.sideEffectFlow.collect { effect ->
            if (effect is UiSideEffect.Wrapped) onEffect(effect.effect)
        }
    }
}
