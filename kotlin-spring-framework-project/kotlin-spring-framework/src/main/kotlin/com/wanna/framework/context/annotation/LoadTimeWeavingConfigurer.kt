package com.wanna.framework.context.annotation

import com.wanna.framework.instrument.classloading.LoadTimeWeaver

/**
 * 这是对于[LoadTimeWeaver]的一个配置类, 可以通过它去配置SpringBeanFactory当中的默认[LoadTimeWeaver];
 * 如果用户不配置一个[LoadTimeWeavingConfigurer]到容器当中的话, 那么将会采用Spring默认的[LoadTimeWeaver]去进行完成编制
 *
 * @see LoadTimeWeaver
 * @see LoadTimeWeavingConfiguration
 */
@FunctionalInterface
fun interface LoadTimeWeavingConfigurer {
    fun getLoadTimeWeaver(): LoadTimeWeaver
}