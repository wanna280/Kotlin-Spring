package com.wanna.boot.autoconfigure

import kotlin.reflect.KClass

/**
 * 标注这个注解的作用是, 指定自动装配的顺序, 要求标注这个注解的当前配置类, 必须在给定的这些配置类装配之前去进行装配
 *
 * @param value 当前的配置类必须在给定的这些配置类装配之前去进行装配(以Class的方式给出)
 * @param name 当前的配置类必须在给定的这些配置类装配之前去进行装配(以ClassName的方式给出)
 * @see AutoConfigureAfter
 * @see AutoConfigureOrder
 */
annotation class AutoConfigureBefore(
    val value: Array<KClass<*>> = [], val name: Array<String> = []
)
