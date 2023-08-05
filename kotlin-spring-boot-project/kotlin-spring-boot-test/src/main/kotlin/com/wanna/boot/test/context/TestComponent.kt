package com.wanna.boot.test.context

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.annotation.AliasFor

/**
 * SpringBoot的测试环境下的一个Component
 *
 * @param value beanName
 */
@Component
annotation class TestComponent(
    @get:AliasFor(annotation = Component::class, attribute = "value")
    val value: String = ""
)
