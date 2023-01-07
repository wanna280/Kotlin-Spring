package com.wanna.boot.context.config

import com.wanna.boot.env.PropertiesPropertySourceLoader
import com.wanna.boot.env.PropertySourceLoader
import com.wanna.boot.env.YamlPropertySourceLoader
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.util.StringUtils

open class StandardConfigDataLocationResolver(private val environment: ConfigurableEnvironment) : Ordered {
    companion object {
        /**
         * SpringBoot的配置文件名对应的属性
         */
        const val CONFIG_NAME_PROPERTY = "spring.config.name"

        /**
         * 默认的SpringBoot的配置文件名, 默认为application
         */
        @JvmField
        val DEFAULT_CONFIG_NAMES = arrayOf("application")
    }

    private var order: Int = Ordered.ORDER_LOWEST

    override fun getOrder(): Int = this.order

    /**
     * PropertySourceLocators
     *
     * @see PropertiesPropertySourceLoader
     * @see YamlPropertySourceLoader
     */
    val propertySourceLoaders: List<PropertySourceLoader> =
        SpringFactoriesLoader.loadFactories(PropertySourceLoader::class.java)

    /**
     * 配置文件name列表(默认为application)
     */
    private val configNames: Array<String> = this.getConfigNames()

    open fun getConfigNames(): Array<String> {
        val configNames =
            StringUtils.commaDelimitedListToStringArray(environment.getProperty(CONFIG_NAME_PROPERTY) ?: "")
        return if (configNames.isEmpty()) return DEFAULT_CONFIG_NAMES else configNames
    }


}