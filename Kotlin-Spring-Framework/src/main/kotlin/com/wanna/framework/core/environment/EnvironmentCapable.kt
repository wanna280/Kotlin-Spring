package com.wanna.framework.core.environment

/**
 * 标识这是一个可以获得Environment的类，和EnvironmentAware相反，一个提供get，另一个提供set
 */
interface EnvironmentCapable {
    fun getEnvironment(): Environment
}