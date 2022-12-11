package com.wanna.boot.context.properties.source

import com.wanna.boot.context.properties.bind.DataObjectPropertyName
import com.wanna.framework.core.environment.EnumerablePropertySource
import com.wanna.framework.core.environment.PropertySource

/**
 * 针对Spring的[PropertySource]去提供[ConfigurationPropertyName]的迭代功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @param propertySource 可以支持去进行属性名的迭代的[PropertySource]
 * @param propertyMappers PropertyMappers
 */
open class SpringIterableConfigurationPropertySource(
    propertySource: EnumerablePropertySource<*>,
    propertyMappers: Array<PropertyMapper>
) : SpringConfigurationPropertySource(propertySource, propertyMappers), IterableConfigurationPropertySource {

    /**
     * 获取到用于去对[ConfigurationPropertyName]去进行迭代的迭代器
     *
     * @return [ConfigurationPropertyName]的迭代器(Kotlin可以直接对数组去使用迭代器, 因此直接返回)
     */
    override fun iterator(): Iterator<ConfigurationPropertyName> = getConfigurationPropertyNames().iterator()

    /**
     * 获取需要去进行迭代的[ConfigurationPropertyName]列表, 根据每个属性名, 去构建出来一个[ConfigurationPropertyName]
     *
     * @return ConfigurationPropertyName List
     */
    private fun getConfigurationPropertyNames(): Array<ConfigurationPropertyName> =
        getPropertySource().getPropertyNames()
            .map { name ->
                propertyMappers.mapNotNull { mapper ->
                    try {
                        mapper.map(name)
                    } catch (ex: Throwable) {
                        null
                    }
                }.toList()
            }.flatMap { it.toList() }.toTypedArray()

    /**
     * 快速将[PropertySource]去转换成为[EnumerablePropertySource], 因为类型一定是[EnumerablePropertySource]
     *
     * @return EnumerablePropertySource
     */
    open fun getPropertySource(): EnumerablePropertySource<*> = this.propertySource as EnumerablePropertySource<*>

    /**
     * 检查当前的[ConfigurationPropertySource]当中是否存在有给定的属性Key作为前缀的配置信息
     *
     * @param name 属性前缀Key
     * @return 如果存在return PRESENT, 如果不存在, return ABSENT; 默认实现为UNKNOWN
     */
    override fun containsDescendantOf(name: ConfigurationPropertyName): ConfigurationPropertyState {
        // TODO, 这里需要根据属性名去进行一个个匹配...
        return ConfigurationPropertyState.UNKNOWN
    }
}