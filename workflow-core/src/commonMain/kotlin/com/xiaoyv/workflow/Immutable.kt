package com.xiaoyv.workflow

/**
 * 标记公开工作流模型为值不可变对象，不引入 Compose 运行时。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Immutable
