package com.wanna.framework.test.context.support

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.test.context.ContextConfigurationAttributes
import com.wanna.framework.test.context.MergedContextConfiguration
import com.wanna.framework.test.context.SmartContextLoader

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
abstract class AbstractContextLoader : SmartContextLoader {

    override fun processLocations(vararg locations: String): Array<String> {
        TODO("Not yet implemented")
    }

    override fun loadContext(vararg locations: String): ApplicationContext {
        TODO("Not yet implemented")
    }

    override fun processContextConfiguration(configAttributes: ContextConfigurationAttributes) {
        TODO("Not yet implemented")
    }

    override fun loadContext(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext {
        TODO("Not yet implemented")
    }
}