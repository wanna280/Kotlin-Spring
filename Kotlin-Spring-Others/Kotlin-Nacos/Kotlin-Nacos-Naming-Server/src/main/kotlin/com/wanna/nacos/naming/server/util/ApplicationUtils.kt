package com.wanna.nacos.naming.server.util

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * ApplicationContext的工具类，它实现了ApplicationContextInitializer，在SpringApplicationContext创建完成时，会自动回调ApplicationContext
 */
class ApplicationUtils : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        private var applicationContext: ApplicationContext? = null
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        ApplicationUtils.applicationContext = applicationContext
    }
}