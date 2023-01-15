package com.wanna.framework.context.annotation

import kotlin.reflect.KClass

/**
 * 通过Import注解, 可以给SpringBeanFactory中去批量导入导入配置类, 它导入的组件会被当成配置类去进行**递归**处理
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class Import(val value: Array<KClass<*>> = [])
