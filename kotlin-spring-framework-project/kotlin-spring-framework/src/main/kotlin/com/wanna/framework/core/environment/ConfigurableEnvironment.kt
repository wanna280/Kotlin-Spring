package com.wanna.framework.core.environment

/**
 * 提供可以被配置的环境对象, 可以对环境当中的各种信息去进行配置
 */
interface ConfigurableEnvironment : Environment, ConfigurablePropertyResolver {

    /**
     * 设置活跃的profiles
     */
    fun setActiveProfiles(vararg profiles: String)

    /**
     * 添加活跃的profiles
     */
    fun addActiveProfiles(profile: String)

    /**
     * 设置默认的profiles
     */
    fun setDefaultProfiles(vararg profiles: String)

    /**
     * 或缺PropertySource列表
     */
    fun getPropertySources(): MutablePropertySources

    /**
     * 获取系统属性
     */
    fun getSystemProperties(): Map<String, Any>

    /**
     * 获取系统环境属性信息
     */
    fun getSystemEnvironment(): Map<String, Any>
}