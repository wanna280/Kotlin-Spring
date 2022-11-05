package com.wanna.boot.test.context

import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.test.context.CacheAwareContextLoaderDelegate
import com.wanna.framework.test.context.ContextLoader
import com.wanna.framework.test.context.MergedContextConfiguration

/**
 * Mvc场景下的[MergedContextConfiguration]，继承的目的仅仅是标识作用。
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
open class MvcMergedContextConfiguration(
    testClass: Class<*>,
    contextLoader: ContextLoader,
    locations: Array<String>,
    classes: Array<Class<*>>,
    initializers: Array<Class<out ApplicationContextInitializer<*>>>,
    propertySourceLocations: Array<String>,
    propertySourceProperties: Array<String>,
    activeProfiles: Array<String>,
    cacheAwareContextLoaderDelegate: CacheAwareContextLoaderDelegate
) : MergedContextConfiguration(
    testClass,
    contextLoader,
    locations,
    classes,
    initializers,
    propertySourceLocations,
    propertySourceProperties,
    activeProfiles,
    cacheAwareContextLoaderDelegate
) {
    /**
     * 根据[MergedContextConfiguration]去构建[MvcMergedContextConfiguration]的构造器
     *
     * @param config MergedContextConfiguration
     */
    constructor(config: MergedContextConfiguration) : this(
        config.getTestClass(),
        config.getContextLoader(),
        config.getLocations(),
        config.getClasses(),
        config.getInitializers(),
        config.getPropertySourceLocations(),
        config.getPropertySourceProperties(),
        config.getActiveProfiles(),
        config.getCacheAwareContextLoaderDelegate()
    )
}