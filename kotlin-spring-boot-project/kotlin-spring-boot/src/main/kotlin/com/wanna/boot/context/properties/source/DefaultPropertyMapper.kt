package com.wanna.boot.context.properties.source

import com.wanna.framework.lang.Nullable

/**
 * 默认的[PropertyMapper]的实现, 映射将会移除掉不合法的字符, 并且将字母去转换成为小写,
 * 例如对于"my.server_name.PORT"将会被转换成为"my.servername.port"
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


    @Nullable
    private var lastMappedConfigurationPropertyName: LastMapping<ConfigurationPropertyName, List<String>>? = null

    @Nullable
    private var lastMappedPropertyName: LastMapping<String, ConfigurationPropertyName>? = null

    /**
     * 根据[ConfigurationPropertyName]去转换成为字符串形式的属性名
     *
     * @param configurationPropertyName 分隔之后得到的[ConfigurationPropertyName]
     * @return 转换得到的字符串形式的属性名
     */
    override fun map(configurationPropertyName: ConfigurationPropertyName): List<String> {
        // Use a local copy in case another thread changes things
        val last = lastMappedConfigurationPropertyName
        if (last != null && last.isFrom(configurationPropertyName)) {
            return last.getMapping()
        }
        val name = configurationPropertyName.toString()
        val mapping = listOf(name)
        this.lastMappedConfigurationPropertyName = LastMapping(configurationPropertyName, mapping)
        return mapping
    }

    /**
     * 将给定的属性名[name]去转换成为[ConfigurationPropertyName]
     *
     * @param name 属性名name
     * @return 根据属性名, 使用"."去进行分割得到多个段, 得到的[ConfigurationPropertyName]
     */
    override fun map(name: String): ConfigurationPropertyName {
        // Use a local copy in case another thread changes things
        val last = this.lastMappedPropertyName
        if (last != null && last.isFrom(name)) {
            return last.getMapping()
        }
        val mapping = tryMap(name)
        this.lastMappedPropertyName = LastMapping(name, mapping)
        return mapping
    }

    private fun tryMap(name: String): ConfigurationPropertyName {
        try {
            val convertedName = ConfigurationPropertyName.adapt(name, '.')
            if (!convertedName.isEmpty()) {
                return convertedName
            }
        } catch (ex: Exception) {
            // ignore
        }
        return ConfigurationPropertyName.EMPTY
    }

    private class LastMapping<T, M>(private val from: T, private val mapping: M) {
        fun isFrom(from: T): Boolean = this.from == from

        fun getMapping(): M = this.mapping
    }
}