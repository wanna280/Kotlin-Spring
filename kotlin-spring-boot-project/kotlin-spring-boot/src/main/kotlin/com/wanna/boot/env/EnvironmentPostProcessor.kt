package com.wanna.boot.env

import com.wanna.boot.SpringApplication
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 这是一个对Spring的环境去进行处理的后置处理器, 它会被EnvironmentPostProcessorApplicationListener所自动回调
 *
 * @see EnvironmentPostProcessorApplicationListener
 */
interface EnvironmentPostProcessor {
    /**
     * 对SpringApplication的Environment(环境)去进行后置处理
     *
     * @param application SpringApplication
     * @param environment SpringApplication的Environment
     */
    fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication)
}