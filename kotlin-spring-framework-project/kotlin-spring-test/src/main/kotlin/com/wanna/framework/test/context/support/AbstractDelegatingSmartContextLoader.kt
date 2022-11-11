package com.wanna.framework.test.context.support

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.test.context.ContextConfigurationAttributes
import com.wanna.framework.test.context.MergedContextConfiguration
import com.wanna.framework.test.context.SmartContextLoader

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
abstract class AbstractDelegatingSmartContextLoader : SmartContextLoader {

    /**
     * 获取注解的ContextLoader
     */
    protected abstract fun getAnnotationConfigLoader(): SmartContextLoader

    /**
     * 获取XML的Loader
     */
    protected abstract fun getXmlLoader(): SmartContextLoader

    override fun processLocations(vararg locations: String): Array<String> {
        return arrayOf(*locations)
    }

    override fun loadContext(vararg locations: String): ApplicationContext {
        return getXmlLoader().loadContext(*locations)
    }

    override fun processContextConfiguration(configAttributes: ContextConfigurationAttributes) {

    }

    override fun loadContext(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext {
        return getAnnotationConfigLoader().loadContext(mergedContextConfiguration)
    }
}