package com.wanna.boot.test.context

import com.wanna.framework.context.stereotype.Component
import org.springframework.core.annotation.AliasFor

/**
 * SpringBoot的测试环境下的一个Component
 *
 * @param value beanName
 */
@Component
annotation class TestComponent(
    @get:com.wanna.framework.core.annotation.AliasFor(annotation = Component::class, attribute = "value")
    @get:AliasFor(annotation = Component::class, attribute = "value")
    val value: String = ""
)
