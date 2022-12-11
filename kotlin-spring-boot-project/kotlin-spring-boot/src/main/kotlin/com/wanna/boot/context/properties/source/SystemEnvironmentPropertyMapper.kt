package com.wanna.boot.context.properties.source

/**
 * SystemEnvironment的[PropertyMapper]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
class SystemEnvironmentPropertyMapper : PropertyMapper {

    companion object {
        /**
         * [SystemEnvironmentPropertyMapper]的单例对象
         */
        @JvmField
        val INSTANCE = SystemEnvironmentPropertyMapper()
    }

    override fun map(configurationPropertyName: ConfigurationPropertyName): List<String> {
        return emptyList()
    }

    override fun map(name: String): ConfigurationPropertyName {
        return ConfigurationPropertyName.of(name)
    }
}