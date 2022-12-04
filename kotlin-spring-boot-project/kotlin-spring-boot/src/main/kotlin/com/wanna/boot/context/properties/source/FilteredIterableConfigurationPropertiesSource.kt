package com.wanna.boot.context.properties.source

import java.util.function.Predicate

/**
 * 提供对于迭代的[ConfigurationPropertyName]的过滤功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/5
 */
open class FilteredIterableConfigurationPropertiesSource(
    source: ConfigurationPropertySource,
    filter: Predicate<ConfigurationPropertyName>
) : FilteredConfigurationPropertiesSource(source, filter), IterableConfigurationPropertySource {

    /**
     * 对于[ConfigurationPropertyName]去进行过滤
     */
    override fun iterator(): Iterator<ConfigurationPropertyName> {
        if (source is IterableConfigurationPropertySource) {
            return source.filter { filter.test(it) }.iterator()
        }
        return emptyList<ConfigurationPropertyName>().iterator()
    }
}