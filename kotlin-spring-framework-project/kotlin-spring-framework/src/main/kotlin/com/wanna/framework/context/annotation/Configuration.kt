package com.wanna.framework.context.annotation

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是Spring当中的一个配置类
 *
 * @param value beanName
 * @param proxyBeanMethods 是否需要去代理配置类的@Bean方法？
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Component
annotation class Configuration(
    @get:AliasFor(annotation = Component::class, value = "value")
    val value: String = "",
    val proxyBeanMethods: Boolean = true
)
