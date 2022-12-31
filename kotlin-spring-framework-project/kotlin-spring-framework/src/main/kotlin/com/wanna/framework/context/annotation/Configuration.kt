package com.wanna.framework.context.annotation

import com.wanna.framework.context.stereotype.Component
import org.springframework.core.annotation.AliasFor

/**
 * 标识这是Spring当中的一个配置类
 *
 * @param value beanName
 * @param proxyBeanMethods 是否需要去代理@Bean方法？
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Component
annotation class Configuration(
    @get:com.wanna.framework.core.annotation.AliasFor(annotation = Component::class, value = "value")
    @get:AliasFor(annotation = Component::class, value = "value")
    val value: String = "",
    val proxyBeanMethods: Boolean = true
)
