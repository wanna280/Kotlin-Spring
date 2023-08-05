package com.wanna.boot.context.properties.source

/**
 * 对属性名去进行映射的[PropertyMapper]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 */
interface PropertyMapper {

    /**
     * 将[ConfigurationPropertyName]映射成为属性名, 主要根据name从PropertySource当中获取属性值时
     *
     * @param configurationPropertyName 待映射的属性名[ConfigurationPropertyName]
     * @return 映射之后得到的属性名列表, 可以根据这个属性值, 从PropertySource当中去getProperty
     */
    fun map(configurationPropertyName: ConfigurationPropertyName): List<String>

    /**
     * 将给定的属性名去映射成为[ConfigurationPropertyName], 主要用于PropertySource当中的属性名的转换
     *
     * @param name 原始的属性名name
     * @return 将原始的属性名去进行映射之后得到的[ConfigurationPropertyName]
     */
    fun map(name: String): ConfigurationPropertyName

}