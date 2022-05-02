package com.wanna.boot.autoconfigure

/**
 * 这是一个AutoConfiguration的Metadata信息
 *
 * @see com.wanna.boot.autoconfigure.AutoConfigurationMetadataLoader.PropertiesAutoConfigurationMetadata
 */
interface AutoConfigurationMetadata {

    /**
     * 根据className和key去获取到metadata信息，并利用得到的值去转换为Set；如果找不到时，返回默认值(defaultValue)
     */
    fun getSet(className: String, key: String, defaultValue: Set<String>?): Set<String>?

    /**
     * 根据className和key去获取到metadata信息，并利用得到的值去转换为Set，如果找不到时，return null
     */
    fun getSet(className: String, key: String): Set<String>?

    /**
     * 根据className和key去获取到metadata信息去进行返回；如果找不到时返回默认值(defaultValue)
     */
    fun get(className: String, key: String, defaultValue: String?): String?

    /**
     * 根据className和key去获取到metadata信息去进行返回，如果找不到时，return null
     */
    fun get(className: String, key: String): String?
}