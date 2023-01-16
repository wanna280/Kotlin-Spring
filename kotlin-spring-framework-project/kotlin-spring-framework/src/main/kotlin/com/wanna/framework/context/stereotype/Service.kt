package com.wanna.framework.context.stereotype

import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是一个Service层的Spring Bean, 作用和[Component]完全相同
 *
 * @see Component
 *
 * @param value beanName
 */
@Component
annotation class Service(
    @get:AliasFor(value = "value", annotation = Component::class)
    val value: String = ""
)
