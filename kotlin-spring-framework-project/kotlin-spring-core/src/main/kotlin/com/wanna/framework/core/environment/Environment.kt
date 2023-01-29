package com.wanna.framework.core.environment

/**
 * 这是一个环境对象, 维护容器当中需要用到的所有属性信息, 比如来自各个地方的配置文件信息;
 * 它本身也是一个PropertyResolver, 支持去对属性去进行获取和解析
 *
 * @see PropertyResolver
 * @see StandardEnvironment
 * @see AbstractEnvironment
 * @see ConfigurableEnvironment
 * @see EnvironmentCapable
 */
interface Environment : PropertyResolver {

    /**
     * 获取默认的profile列表
     */
    fun getDefaultProfiles(): Array<String>

    /**
     * 获取活跃的profile列表
     */
    fun getActiveProfiles(): Array<String>
}