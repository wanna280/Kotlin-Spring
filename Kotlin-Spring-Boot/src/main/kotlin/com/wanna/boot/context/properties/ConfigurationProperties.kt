package com.wanna.boot.context.properties

import org.springframework.core.annotation.AliasFor

/**
 * 标识这是一个ConfigurationProperties，它会自动匹配环境当中的属性去对Bean当中的属性去进行属性的填充
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
annotation class ConfigurationProperties(
    @get:AliasFor("value")
    val prefix: String = "",
    @get:AliasFor("prefix")
    val value: String = ""
)
