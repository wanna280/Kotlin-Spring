package com.wanna.boot.autoconfigure

import kotlin.reflect.KClass

/**
 * 标注这个注解的作用是, 指定自动装配的顺序, 在装配当前配置类之前, 必须先去进行装配的一些自动配置类
 *
 * @param value 在装配当前配置类之前, 必须先去进行装配的配置类(以Class的方式给出)
 * @param name 在装配当前配置类之前, 必须先去进行装配的配置类(以ClassName的方式给出)
 *
 * @see AutoConfigureBefore
 * @see AutoConfigureOrder
 */
annotation class AutoConfigureAfter(
    val value: Array<KClass<*>> = [], val name: Array<String> = []
)
