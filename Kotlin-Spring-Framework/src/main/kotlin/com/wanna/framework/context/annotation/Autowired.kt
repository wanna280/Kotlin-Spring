package com.wanna.framework.context.annotation

/**
 * Autowired，用来完成容器中Bean的注入
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FIELD)
annotation class Autowired(val required: Boolean = true)
