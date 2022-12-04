package com.wanna.boot.context.properties.source

import com.wanna.boot.context.properties.bind.DataObjectPropertyName

/**
 * 默认的[PropertyMapper]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
class DefaultPropertyMapper : PropertyMapper {
    companion object {
        /**
         * [DefaultPropertyMapper]的单例对象
         */
        @JvmField
        val INSTANCE = DefaultPropertyMapper()
    }

    override fun map(configurationPropertyName: ConfigurationPropertyName): List<String> {
        return listOf(configurationPropertyName.toString())
    }

    override fun map(name: String): ConfigurationPropertyName {
        return ConfigurationPropertyName.of(DataObjectPropertyName.toDashedForm(name))
    }
}