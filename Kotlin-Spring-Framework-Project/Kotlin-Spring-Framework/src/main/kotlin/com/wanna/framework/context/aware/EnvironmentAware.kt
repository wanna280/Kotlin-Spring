package com.wanna.framework.context.aware

import com.wanna.framework.core.environment.Environment

/**
 * 这是一个注入环境对象的Aware接口
 */
interface EnvironmentAware : Aware {
    fun setEnvironment(environment: Environment)
}