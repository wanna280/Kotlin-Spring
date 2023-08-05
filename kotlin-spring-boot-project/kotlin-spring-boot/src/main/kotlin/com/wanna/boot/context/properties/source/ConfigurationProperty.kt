package com.wanna.boot.context.properties.source

import com.wanna.boot.origin.Origin
import com.wanna.framework.lang.Nullable

/**
 * 描述一个Configuration的属性值
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 *
 * @param name 属性名
 * @param value 属性值
 * @param source 当前这个[ConfigurationProperty]所来自的[ConfigurationPropertySource]
 * @param origin 描述当前这个[ConfigurationProperty]的来源信息(来自哪个PropertySource, 来自哪个属性名?)
 */
open class ConfigurationProperty(
    val source: ConfigurationPropertySource,
    val name: ConfigurationPropertyName,
    @Nullable val value: Any?,
    @Nullable val origin: Origin?
) {
    override fun toString(): String = "ConfigurationProperty(name=$name, value=$value, origin=$origin)"

    companion object {

        /**
         * 构建[ConfigurationPropertyName]的工厂方法
         *
         * @param source ConfigurationPropertySource
         * @param name name
         * @param value value
         * @param origin origin
         * @return 为给定的name-value对, 去构建出来[ConfigurationProperty]
         */
        @JvmStatic
        fun of(
            source: ConfigurationPropertySource,
            name: ConfigurationPropertyName,
            @Nullable value: Any?,
            @Nullable origin: Origin?
        ): ConfigurationProperty? {
            value ?: return null
            return ConfigurationProperty(source, name, value, origin)
        }
    }
}