package com.wanna.boot.autoconfigure

import com.wanna.framework.lang.Nullable

/**
 * 维护SpringBoot的自动配置(AutoConfiguration)的Metadata信息
 *
 * @see com.wanna.boot.autoconfigure.AutoConfigurationMetadataLoader.PropertiesAutoConfigurationMetadata
 */
interface AutoConfigurationMetadata {

    /**
     * 根据className和key去获取到metadata信息，并利用得到的值去转换为Set；如果找不到时，返回默认值(defaultValue)
     *
     * @param className className
     * @param key key
     * @param defaultValue 默认值
     */
    fun getSet(className: String, key: String, defaultValue: Set<String>): Set<String>

    /**
     * 根据className和key去获取到metadata信息，并利用得到的值去转换为Set，如果找不到时，return null
     *
     * @param className className
     * @param key key
     */
    @Nullable
    fun getSet(className: String, key: String): Set<String>?


    /**
     * 根据ClassName和Key去获取到对应的Metadata信息去进行返回, 如果找不到返回默认值(defaultValue)
     *
     * @param className className
     * @param key key
     * @param defaultValue defaultValue
     * @return
     */
    fun getInt(className: String, key: String, defaultValue: Int): Int

    /**
     * 根据ClassName和Key去获取到对应的Metadata信息去进行返回, 如果找不到返回null
     *
     * @param className className
     * @param key key
     * @return 获取到的Metadata属性值(不存在的话return null)
     */
    @Nullable
    fun getInt(className: String, key: String): Int?

    /**
     * 根据className和key去获取到metadata信息去进行返回；如果找不到时返回默认值(defaultValue)
     *
     * @param className className
     * @param key key
     * @param defaultValue 默认值
     */
    fun get(className: String, key: String, defaultValue: String?): String?

    /**
     * 根据className和key去获取到metadata信息去进行返回，如果找不到时，return null
     *
     * @param className className
     * @param key key
     */
    @Nullable
    fun get(className: String, key: String): String?
}