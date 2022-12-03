package com.wanna.boot.context.properties.source

import com.wanna.framework.lang.Nullable

/**
 * 针对Configuration属性配置情况下的PropertySource, 提供根据name去获取具体的属性值的API
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 * @see ConfigurationPropertyName
 * @see ConfigurationProperty
 */
interface ConfigurationPropertySource {

    /**
     * 根据属性名, 去获取到对应的属性值[ConfigurationProperty]
     *
     * @param name 属性名
     * @return 根据属性名去找到的找到的属性值[ConfigurationProperty], 获取不到return null
     */
    @Nullable
    fun getConfigurationProperty(name: ConfigurationPropertyName): ConfigurationProperty?

    /**
     * 检查是否存在有给定的属性名的配置信息
     *
     * @param name 属性名
     * @return 如果存在return PRESENT, 如果不存在, return ABSENT
     */
    fun containsDescendantOf(name: ConfigurationPropertyName): ConfigurationPropertyState =
        ConfigurationPropertyState.UNKNOWN

}