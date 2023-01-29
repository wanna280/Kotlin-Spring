package com.wanna.framework.core.environment

/**
 * 这是一个标准的Environment的实现, 它本身是一个可以被配置的环境对象; 它自带了系统当中的环境信息和系统当中的属性信息
 */
open class StandardEnvironment : AbstractEnvironment() {

    companion object {

        /**
         * 系统属性的PropertySourceName
         */
        const val SYSTEM_PROPERTY_PROPERTY_SOURCE_NAME = "systemProperties"

        /**
         * 系统环境的PropertySourceName
         */
        const val SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment"
    }

    /**
     * 执行PropertySource的自定义时, 将系统属性和系统环境变量信息添加到PropertySources列表当中
     *
     * @param propertySources 待进行自定义的PropertySources
     */
    override fun customizePropertySources(propertySources: MutablePropertySources) {
        propertySources.addLast(PropertiesPropertySource(SYSTEM_PROPERTY_PROPERTY_SOURCE_NAME, getSystemProperties()))
        propertySources.addLast(
            SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, getSystemEnvironment())
        )
    }

}