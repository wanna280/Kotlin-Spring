package com.wanna.boot.context.properties.source

import java.util.function.Predicate

/**
 * 对于属性值拥有过滤功能的[ConfigurationPropertySource], 对于不合法的属性值, 将会直接pass掉
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/5
 */
open class FilteredConfigurationPropertiesSource(
    val source: ConfigurationPropertySource,
    val filter: Predicate<ConfigurationPropertyName>
) : ConfigurationPropertySource {

    override fun getConfigurationProperty(name: ConfigurationPropertyName): ConfigurationProperty? {
        val filtered = filter.test(name)
        return if (filtered) source.getConfigurationProperty(name) else null
    }
}