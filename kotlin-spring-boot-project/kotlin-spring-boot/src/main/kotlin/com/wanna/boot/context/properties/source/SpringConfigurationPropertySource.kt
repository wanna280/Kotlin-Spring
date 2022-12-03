package com.wanna.boot.context.properties.source

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.lang.Nullable

/**
 * 将Spring原生的[PropertySource]去转换成为[ConfigurationPropertySource]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
class SpringConfigurationPropertySource(private val propertySource: PropertySource<*>) : ConfigurationPropertySource {

    /**
     * 根据属性名去获取到属性值
     *
     * @param name name
     * @return 根据属性名去获取到的属性值(获取不到return null)
     */
    @Nullable
    override fun getConfigurationProperty(name: ConfigurationPropertyName): ConfigurationProperty? {
        val property = propertySource.getProperty(name.toString()) ?: return null
        return ConfigurationProperty.of(name, property)
    }

    companion object {

        /**
         * 根据一个原生的Spring的[PropertySource]去构建出来[SpringConfigurationPropertySource]
         *
         * @param propertySource PropertySource
         * @return ConfigurationPropertySource
         */
        @JvmStatic
        fun from(propertySource: PropertySource<*>): SpringConfigurationPropertySource {
            return SpringConfigurationPropertySource(propertySource)
        }
    }
}