package com.wanna.framework.core.environment

import com.wanna.framework.core.convert.ConversionService

/**
 * 这是一个标准的Environment的实现，它本身是一个可以被配置的环境对象；它自带了系统当中的环境信息和系统当中的属性信息
 */
open class StandardEnvironment : AbstractEnvironment() {

    companion object {
        const val SYSTEM_PROPERTY_PROPERTY_SOURCE_NAME = "systemProperties"  // 系统属性的PropertySourceName
        const val SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment"  // 系统环境的PropertySourceName
    }

    override fun customizePropertySources(propertySources: MutablePropertySources) {
        propertySources.addLast(PropertiesPropertySource(SYSTEM_PROPERTY_PROPERTY_SOURCE_NAME, getSystemProperties()))
        propertySources.addLast(MapPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, getSystemEnvironment()))
    }

}