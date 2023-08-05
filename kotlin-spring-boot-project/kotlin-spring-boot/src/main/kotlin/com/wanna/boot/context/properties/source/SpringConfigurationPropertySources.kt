package com.wanna.boot.context.properties.source

import com.wanna.framework.core.environment.PropertySource
import java.util.function.Function

/**
 * 根据Spring的[PropertySource]列表去构建出来[ConfigurationPropertySource]的列表
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
class SpringConfigurationPropertySources(private val sources: Iterable<PropertySource<*>>) :
    Iterable<ConfigurationPropertySource> {

    override fun iterator(): Iterator<ConfigurationPropertySource> = SourcesIterator(sources.iterator(), this::adapt)

    /**
     * 将[PropertySource]去转换成为[ConfigurationPropertySource]
     *
     * @param source PropertySource
     * @return ConfigurationPropertySource
     */
    private fun adapt(source: PropertySource<*>): ConfigurationPropertySource {
        return SpringConfigurationPropertySource.from(source)
    }

    class SourcesIterator(
        private val iterator: Iterator<PropertySource<*>>,
        private val adapter: Function<PropertySource<*>, ConfigurationPropertySource>
    ) : Iterator<ConfigurationPropertySource> {

        override fun hasNext(): Boolean = iterator.hasNext()

        override fun next(): ConfigurationPropertySource = adapter.apply(iterator.next())
    }
}