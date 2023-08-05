package com.wanna.boot.origin

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.lang.Nullable

/**
 * PropertySource Origin, 用于对一个属性值的来源去进行描述
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param propertySource 来源的PropertySource
 * @param propertyName 来源的属性名
 */
open class PropertySourceOrigin(val propertySource: PropertySource<*>, val propertyName: String) : Origin {

    /**
     * toString
     */
    override fun toString(): String = "\"$propertyName\" from property source \"${propertySource.name}\""
    override fun equals(@Nullable other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PropertySourceOrigin
        return propertySource == other.propertySource && propertyName == other.propertyName
    }

    override fun hashCode(): Int {
        var result = propertySource.hashCode()
        result = 31 * result + propertyName.hashCode()
        return result
    }


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