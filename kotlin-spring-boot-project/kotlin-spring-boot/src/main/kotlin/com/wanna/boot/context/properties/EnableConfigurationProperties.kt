package com.wanna.boot.context.properties

import com.wanna.framework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * 开启[ConfigurationProperties]属性Bean的快速注册,
 * 在[EnableConfigurationPropertiesRegistrar]当中会往SpringBeanFactory当中批量注Bean件,
 * 从而去完成标注了`@ConfigurationProperties`的那些Bean的属性的绑定工作
 *
 * @see EnableConfigurationPropertiesRegistrar
 * @see NestedConfigurationProperty
 *
 * @param value 要去进行注册到SpringBeanFactory当中的配置类, 并且这些配置类将会用于属性的自动绑定功能
 */
@Import([EnableConfigurationPropertiesRegistrar::class])
annotation class EnableConfigurationProperties(
    val value: Array<KClass<*>> = []
)
