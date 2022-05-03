package com.wanna.boot.context.properties

import com.wanna.framework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * 开启ConfigurationProperties属性Bean的快速注册，在EnableConfigurationPropertiesRegistrar当中会往容器当中批量注册组件
 *
 * @see EnableConfigurationPropertiesRegistrar
 */
@Import([EnableConfigurationPropertiesRegistrar::class])
annotation class EnableConfigurationProperties(
    val value: Array<KClass<*>> = []
)
