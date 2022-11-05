package com.wanna.framework.test.context

import com.wanna.framework.context.ApplicationContextInitializer
import java.io.Serializable

/**
 * 经过Merge之后的TestContext的配置信息，由[ContextConfigurationAttributes]所Merge而来；
 * 并添加了更多相关的配置信息，去进行合并
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
open class MergedContextConfiguration(
    private val testClass: Class<*>,
    private val contextLoader: ContextLoader,
    private val locations: Array<String>,
    private val classes: Array<Class<*>>,
    private val initializers: Array<Class<out ApplicationContextInitializer<*>>>,
    private val propertySourceLocations: Array<String>,
    private val propertySourceProperties: Array<String>,
    private val activeProfiles: Array<String>,
    private val cacheAwareContextLoaderDelegate: CacheAwareContextLoaderDelegate
) : Serializable {

    open fun getContextLoader(): ContextLoader = this.contextLoader

    open fun getLocations(): Array<String> = this.locations

    open fun getClasses(): Array<Class<*>> = this.classes
}