package com.wanna.boot.context.properties.source

import com.wanna.framework.util.ClassUtils.getQualifiedName
import java.util.function.Predicate

/**
 * 提供对于[ConfigurationPropertySource]当中的[ConfigurationPropertyName]去进行过滤的功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/5
 *
 * @param source source
 * @param filter 需要对source当中的元素去进行过滤的filter断言
 */
open class FilteredIterableConfigurationPropertiesSource(
    source: ConfigurationPropertySource,
    filter: Predicate<ConfigurationPropertyName>
) : FilteredConfigurationPropertiesSource(source, filter), IterableConfigurationPropertySource {

    /**
     * 使用给定的filter对于[ConfigurationPropertyName]去进行过滤
     *
     * @return 用于去进行[ConfigurationPropertyName]的迭代的迭代器
     */
    override fun iterator(): Iterator<ConfigurationPropertyName> {
        if (source is IterableConfigurationPropertySource) {
            return source.filter { filter.test(it) }.iterator()
        }
        return emptyList<ConfigurationPropertyName>().iterator()
    }

    /**
     * 是否含有给定的属性名的子属性Key?
     *
     * @param name 前缀
     * @return 是否包含该前缀的属性Key?
     */
    override fun containsDescendantOf(name: ConfigurationPropertyName): ConfigurationPropertyState {
        return source.containsDescendantOf(name)
    }
}