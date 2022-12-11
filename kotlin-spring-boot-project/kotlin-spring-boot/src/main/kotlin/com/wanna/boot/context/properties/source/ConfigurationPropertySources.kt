package com.wanna.boot.context.properties.source

import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.PropertySource

/**
 * 根据[PropertySource]列表去构建出来[ConfigurationPropertySource]的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
object ConfigurationPropertySources {

    /**
     * 根据[Environment]去构建出来[ConfigurationPropertySource]列表
     *
     * @param environment Environment, 必须为ConfigurableEnvironment
     * @return 根据[PropertySource]去转换得到的[ConfigurationPropertySource]列表
     */
    @JvmStatic
    fun get(environment: Environment): Iterable<ConfigurationPropertySource> {
        if (environment !is ConfigurableEnvironment) {
            throw IllegalStateException("不支持通过非ConfigurableEnvironment去构建ConfigurationPropertySources")
        }
        return SpringConfigurationPropertySources(environment.getPropertySources())
    }

    /**
     * 根据给定的Spring原生的[PropertySource]列表, 去构建出来合适的[ConfigurationPropertySource]列表
     *
     * @param sources 原始的PropertySource列表
     * @return ConfigurationPropertySource列表
     */
    @JvmStatic
    fun from(sources: Iterable<PropertySource<*>>): Iterable<ConfigurationPropertySource> {
        return SpringConfigurationPropertySources(sources)
    }
}