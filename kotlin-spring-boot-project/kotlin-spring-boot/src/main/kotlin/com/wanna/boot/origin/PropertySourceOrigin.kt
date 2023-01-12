package com.wanna.boot.origin

import com.wanna.framework.core.environment.PropertySource

/**
 * PropertySource Origin
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
open class PropertySourceOrigin(val propertySource: PropertySource<*>, val propertyName: String) : Origin {

    companion object {

        /**
         * 构建PropertySourceOrigin的工厂方法
         *
         * @param propertySource PropertySource
         * @param propertyName PropertyName
         * @return Origin
         */
        @JvmStatic
        fun get(propertySource: PropertySource<*>, propertyName: String): Origin {
            val origin = OriginLookup.getOrigin(propertySource, propertyName)
            return origin ?: PropertySourceOrigin(propertySource, propertyName)
        }
    }

}