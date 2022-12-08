package com.wanna.boot.context.properties.source

import java.util.function.Predicate

/**
 * 对于属性值拥有过滤功能的[ConfigurationPropertySource], 对于filter匹配不上的不合法的属性值, 将会直接pass掉
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/5
 *
 * @param source source
 * @param filter 对[ConfigurationPropertyName]去进行匹配的filter断言
 */
open class FilteredConfigurationPropertiesSource(
    val source: ConfigurationPropertySource,
    val filter: Predicate<ConfigurationPropertyName>
) : ConfigurationPropertySource {

    /**
     * 根据给定的属性名去解析到[ConfigurationProperty], 只有filter匹配上的才需要返回
     *
     * @param name 用于去进行获取属性的属性名
     * @return 如果它能够被filter所匹配上, 那么返回对应的[ConfigurationProperty]; 匹配不上的话, return null
     */
    override fun getConfigurationProperty(name: ConfigurationPropertyName): ConfigurationProperty? {
        val filtered = filter.test(name)
        return if (filtered) source.getConfigurationProperty(name) else null
    }
}