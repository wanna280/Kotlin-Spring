package com.wanna.boot.context.properties.source

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
 */
open class ConfigurationProperty(val name: ConfigurationPropertyName, @Nullable val value: Any?) {

    companion object {

        /**
         * 构建[ConfigurationPropertyName]的工厂方法
         *
         * @param name name
         * @param value value
         */
        @JvmStatic
        fun of(name: ConfigurationPropertyName, @Nullable value: Any?): ConfigurationProperty {
            return ConfigurationProperty(name, value)
        }
    }
}