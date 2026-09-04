package com.xiaoyv.workflow.demo.mvi

sealed class UiSideEffect<T> {
    data class Loading<T>(val isLoading: Boolean) : UiSideEffect<T>()
    data class Toast<T>(val message: String) : UiSideEffect<T>()
    data class Wrapped<T>(val effect: T) : UiSideEffect<T>()
}