package com.wanna.boot.autoconfigure

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.type.filter.TypeFilter

/**
 * 自动配置包的排除过滤器，避免ComponentScan过程当中扫描到了自动配置的类
 */
open class AutoConfigurationExcludeFilter : TypeFilter {

    private var configurations: List<String>? = null

    override fun matches(clazz: Class<*>?): Boolean {
        return clazz != null && isConfiguration(clazz) && isAutoConfiguration(clazz)
    }

    open fun isConfiguration(clazz: Class<*>): Boolean {
        return clazz.isAnnotationPresent(Configuration::class.java)
    }

    open fun isAutoConfiguration(clazz: Class<*>): Boolean {
        return getAutoConfigurations().contains(clazz.name)
    }

    private fun getAutoConfigurations(): List<String> {
        if (this.configurations == null) {
            this.configurations = SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration::class.java)
        }
        return this.configurations!!
    }
}