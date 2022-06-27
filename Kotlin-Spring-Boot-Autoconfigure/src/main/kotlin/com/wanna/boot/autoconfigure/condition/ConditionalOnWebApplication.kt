package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional

/**
 * WebApplicationType的类型的匹配
 */
@Conditional([OnWebApplicationCondition::class])
annotation class ConditionalOnWebApplication(val type: Type = Type.ANY) {
    enum class Type { ANY, MVC }
}