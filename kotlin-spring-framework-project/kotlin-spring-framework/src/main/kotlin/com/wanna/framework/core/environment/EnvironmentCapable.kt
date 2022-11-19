package com.wanna.framework.core.environment

/**
 * 标识这是一个可以获得Spring应用的Environment的对象，和EnvironmentAware相反
 *
 * @see com.wanna.framework.context.aware.EnvironmentAware
 */
fun interface EnvironmentCapable {

    /**
     * 获取Environment
     *
     * @return Environment
     */
    fun getEnvironment(): Environment
}