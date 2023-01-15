package com.wanna.framework.context.annotation

/**
 * 这个注解有两种用法：
 * 1.可以标识这个Bean在运行时才去进行实例化和初始化
 * 2.可以标在要进行注入的元素上, 在运行时去再从容器当中进行Bean的获取
 */
@Target(
    AnnotationTarget.TYPE,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
annotation class Lazy(val value: Boolean = true)
