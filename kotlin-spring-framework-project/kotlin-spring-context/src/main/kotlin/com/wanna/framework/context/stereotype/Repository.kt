package com.wanna.framework.context.stereotype

import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是一个数据访问层的Spring Bean
 *
 * @see Component
 *
 * @param value beanName
 */
@Target(AnnotationTarget.CLASS)
@Component
annotation class Repository(
    @get:AliasFor(value = "value", annotation = Component::class)
    val value: String = ""
)
