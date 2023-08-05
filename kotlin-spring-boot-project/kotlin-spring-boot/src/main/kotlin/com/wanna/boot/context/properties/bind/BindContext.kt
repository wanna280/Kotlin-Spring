package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationProperty
import com.wanna.boot.context.properties.source.ConfigurationPropertySource

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
interface BindContext {

    fun getBinder(): Binder

    fun getDepth(): Int

    fun getSources(): Iterable<ConfigurationPropertySource>

    fun getConfigurationProperty(): ConfigurationProperty?
}