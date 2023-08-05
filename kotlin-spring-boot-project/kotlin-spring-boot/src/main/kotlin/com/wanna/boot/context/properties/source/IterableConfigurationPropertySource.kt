package com.wanna.boot.context.properties.source

/**
 * 支持去对[ConfigurationPropertyName]去进行迭代的[ConfigurationPropertySource]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @see ConfigurationPropertySource
 * @see Iterable
 */
interface IterableConfigurationPropertySource : Iterable<ConfigurationPropertyName>, ConfigurationPropertySource {


}