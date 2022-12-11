package com.wanna.boot.context.properties.source

/**
 * 对属性名去进行转换的[PropertyMapper]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
interface PropertyMapper {

    /**
     * 将[ConfigurationPropertyName]转换成为属性名
     *
     * @param configurationPropertyName 待映射的属性名[ConfigurationPropertyName]
     * @return 映射之后得到的属性名列表
     */
    fun map(configurationPropertyName: ConfigurationPropertyName): List<String>

    /**
     * 将name去映射成为[ConfigurationPropertyName]
     *
     * @param name name
     * @return 映射之后的[ConfigurationPropertyName]
     */
    fun map(name: String): ConfigurationPropertyName

}