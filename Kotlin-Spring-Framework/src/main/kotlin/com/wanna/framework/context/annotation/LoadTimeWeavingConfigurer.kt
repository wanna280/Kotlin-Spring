package com.wanna.framework.context.annotation

import com.wanna.framework.instrument.classloading.LoadTimeWeaver

/**
 * 这是对于LoadTimeWeaver的一个配置类，可以通过它去配置容器当中的默认LoadTimeWeaver；
 * 如果用户不配置一个LoadTimeWeavingConfigurer到容器当中的话，那么将会采用Spring默认的LoadTimeWeaver去进行完成编制
 *
 * @see LoadTimeWeaver
 * @see LoadTimeWeavingConfiguration
 */
interface LoadTimeWeavingConfigurer {
    fun getLoadTimeWeaver(): LoadTimeWeaver
}