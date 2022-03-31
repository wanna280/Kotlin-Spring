package com.wanna.framework.beans.annotations

import kotlin.reflect.KClass

/**
 * 通过Import注解，可以给容器中导入一个配置类，它导入的组件会被当成配置类去进行处理
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class Import(val value: Array<KClass<*>> = [])
