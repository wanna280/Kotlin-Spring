package com.wanna.boot

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是一个SpringBoot的配置类, 就是一个普通配置类, 没啥区别
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Configuration
annotation class SpringBootConfiguration(
    @get:AliasFor(annotation = Configuration::class, value = "proxyBeanMethods")
    val proxyBeanMethods: Boolean = false  // 是否代理@Bean方法？
)
