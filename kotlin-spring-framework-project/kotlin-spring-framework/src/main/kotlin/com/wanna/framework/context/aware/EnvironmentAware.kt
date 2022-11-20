package com.wanna.framework.context.aware

import com.wanna.framework.core.environment.Environment

/**
 * 这是一个注入Spring ApplicationContext环境对象的Aware接口，
 * 和[com.wanna.framework.core.environment.EnvironmentCapable]相反
 *
 * @see com.wanna.framework.core.environment.EnvironmentCapable
 */
fun interface EnvironmentAware : Aware {

    /**
     * 注入Environment
     *
     * @param environment Environment
     */
    fun setEnvironment(environment: Environment)
}