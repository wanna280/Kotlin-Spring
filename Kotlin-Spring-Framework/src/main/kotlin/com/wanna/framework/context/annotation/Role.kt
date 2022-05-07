package com.wanna.framework.context.annotation

/**
 * 设置Bean的角色
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class Role(val value: Int)
