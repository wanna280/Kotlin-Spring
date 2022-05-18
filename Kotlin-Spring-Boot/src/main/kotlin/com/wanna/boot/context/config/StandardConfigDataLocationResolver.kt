package com.wanna.boot.context.config

import com.wanna.boot.env.PropertySourceLoader
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.util.StringUtils

open class StandardConfigDataLocationResolver(_environment: ConfigurableEnvironment) : Ordered {
    companion object {
        const val CONFIG_NAME_PROPERTY = "spring.config.name"
        val DEFAULT_CONFIG_NAMES = arrayOf("application")
    }

    private var order: Int = Ordered.ORDER_LOWEST

    override fun getOrder(): Int {
        return order
    }

    // PropertySourceLocators
    val propertySourceLoaders: List<PropertySourceLoader> =
        SpringFactoriesLoader.loadFactories(PropertySourceLoader::class.java)

    // 配置文件name列表(默认为application)
    private val configNames: Array<String> = this.getConfigNames()

    private val environment: ConfigurableEnvironment? = _environment

    open fun getConfigNames(): Array<String> {
        val configNames =
            StringUtils.commaDelimitedListToStringArray(environment?.getProperty(CONFIG_NAME_PROPERTY) ?: "")
        return if (configNames.isEmpty()) return DEFAULT_CONFIG_NAMES else configNames
    }


}