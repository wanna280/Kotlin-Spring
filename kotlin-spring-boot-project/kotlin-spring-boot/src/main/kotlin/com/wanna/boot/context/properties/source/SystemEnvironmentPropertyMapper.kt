package com.wanna.boot.context.properties.source

import com.wanna.framework.core.environment.SystemEnvironmentPropertySource
import java.util.*

/**
 * SystemEnvironment的[PropertyMapper], 针对系统的环境变量的属性名去进行转换,
 * 对于环境变量当中的属性名当中的不合法字符将会被移除掉, 并且被转换成为小写字母,
 * 并且将"_"去进行移除掉, 例如"SERVER_PORT"将会被转换成为"server.port",
 * 不仅如此, 数字的元素将会被转换成为indexes, 例如"HOST_0"将会被转换成为"host[0]"
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @see SystemEnvironmentPropertySource
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
        // TODO
        return emptyList()
    }

    /**
     * 将给定的name去进行转换(使用"_"去进行分割, 并将所有字母转小写), 并封装成为[ConfigurationPropertyName]
     *
     * @param name 原始的属性名
     * @return 为原始的属性名, 最终计算得到的[ConfigurationPropertyName]
     */
    override fun map(name: String): ConfigurationPropertyName = convertName(name)

    /**
     * 将给定的name去进行转换(使用"_"去进行分割, 并将所有字母转小写), 并封装成为[ConfigurationPropertyName]
     *
     * @param name 原始的属性名
     * @return 为原始的属性名, 最终计算得到的[ConfigurationPropertyName]
     */
    private fun convertName(name: String): ConfigurationPropertyName {
        return try {
            ConfigurationPropertyName.adapt(name, '_', this::processElementValue)
        } catch (ex: Exception) {
            // 转换失败, return empty
            ConfigurationPropertyName.EMPTY
        }
    }

    /**
     * 对一个属性名的元素去进行处理, 对于环境变量"JAVA_HOME_1"这种, 我们需要把它变成"java.home[1]",
     * 也就是使用"_"去作为分隔符, 并且把数字变成"[]"这种索引的格式...
     *
     * @param value 原始的属性名
     * @return 经过转换之后的属性名
     */
    private fun processElementValue(value: String): String {
        // 将字符串当中的全部字母去转为小写...
        val result = value.lowercase(Locale.ENGLISH)

        // 如果是数字, 使用"[]"去进行包围
        return if (isNumber(result)) "[$result]" else result
    }

    /**
     * 检查给定的字符串, 是否是一个数字
     *
     * @param string 待检查的字符串
     * @return 如果全部字符都是数字, return true; 否则return false
     */
    private fun isNumber(string: String): Boolean {
        return string.chars().allMatch { it.toChar().isDigit() }
    }
}