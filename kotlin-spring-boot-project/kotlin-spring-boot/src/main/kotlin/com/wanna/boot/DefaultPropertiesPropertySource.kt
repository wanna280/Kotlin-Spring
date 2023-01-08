package com.wanna.boot

import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.MapPropertySource
import com.wanna.framework.core.environment.MutablePropertySources
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.lang.Nullable

/**
 * 默认的PropertiesPropertySource实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
open class DefaultPropertiesPropertySource(source: Map<String, Any>) : MapPropertySource(NAME, source) {

    companion object {
        /**
         * 默认的Properties PropertySource的name
         */
        const val NAME = "defaultProperties"

        /**
         * 检查给定的PropertySource和[DefaultPropertiesPropertySource]的name是否匹配
         *
         * @param propertySource PropertySource
         * @return 如果该PropertySource的name是"defaultProperties", return true; 否则return false
         */
        fun hasMatchingName(@Nullable propertySource: PropertySource<*>?): Boolean {
            return propertySource != null && propertySource.name == NAME
        }

        /**
         * 将[DefaultPropertiesPropertySource]从Environment的PropertySources当中去移动到最后
         *
         * @param environment Environment
         */
        @JvmStatic
        fun moveToEnd(environment: ConfigurableEnvironment) {
            moveToEnd(environment.getPropertySources())
        }

        /**
         * 将[DefaultPropertiesPropertySource]移动到PropertySources的最后
         *
         * @param propertySources PropertySources
         */
        @JvmStatic
        fun moveToEnd(propertySources: MutablePropertySources) {
            val propertySource = propertySources.remove(NAME)
            if (propertySource != null) {
                propertySources.addLast(propertySource)
            }
        }
    }

}