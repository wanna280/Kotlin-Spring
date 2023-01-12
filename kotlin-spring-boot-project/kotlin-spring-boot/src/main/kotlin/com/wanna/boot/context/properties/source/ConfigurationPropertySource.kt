package com.wanna.boot.context.properties.source

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.lang.Nullable
import java.util.function.Predicate

/**
 * 针对Configuration属性配置情况下的PropertySource, 提供根据name属性名去获取具体的属性值的相关API
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 * @see ConfigurationPropertyName
 * @see ConfigurationProperty
 */
interface ConfigurationPropertySource {

    /**
     * 根据属性名, 去获取到对应的属性值[ConfigurationProperty]
     *
     * @param name 用于去进行获取属性值的属性名
     * @return 根据属性名去找到的找到的属性值[ConfigurationProperty], 获取不到return null
     */
    @Nullable
    fun getConfigurationProperty(name: ConfigurationPropertyName): ConfigurationProperty?

    /**
     * 使用filter去过滤出来合适的属性值, 并返回出来一个新的带有属性名功能过滤功能的[ConfigurationPropertySource]
     *
     * @param filter 需要去过滤属性值的filter
     * @return 新的[ConfigurationPropertySource]
     */
    fun filter(filter: Predicate<ConfigurationPropertyName>): ConfigurationPropertySource {
        return FilteredIterableConfigurationPropertiesSource(this, filter)
    }

    /**
     * 检查当前的[ConfigurationPropertySource]当中是否存在有给定的属性Key作为前缀的配置信息
     *
     * @param name 属性前缀Key
     * @return 如果存在return PRESENT, 如果不存在, return ABSENT; 默认实现为UNKNOWN
     */
    fun containsDescendantOf(name: ConfigurationPropertyName): ConfigurationPropertyState =
        ConfigurationPropertyState.UNKNOWN

    companion object {
        @JvmStatic
        fun from(propertySource: PropertySource<*>): ConfigurationPropertySource? {
            return SpringConfigurationPropertySource.from(propertySource)
        }
    }

}