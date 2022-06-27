package com.wanna.boot.context.properties

import com.wanna.framework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * 开启ConfigurationProperties属性Bean的快速注册，
 * 在EnableConfigurationPropertiesRegistrar当中会往容器当中批量注册组件，
 * 从而去完成@ConfigurationProperties的Bean的属性的绑定工作
 *
 * @see EnableConfigurationPropertiesRegistrar
 * @see NestedConfigurationProperty
 */
@Import([EnableConfigurationPropertiesRegistrar::class])
annotation class EnableConfigurationProperties(
    val value: Array<KClass<*>> = []
)
