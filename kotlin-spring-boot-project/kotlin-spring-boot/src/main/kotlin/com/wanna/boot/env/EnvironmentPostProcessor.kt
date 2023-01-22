package com.wanna.boot.env

import com.wanna.boot.SpringApplication
import com.wanna.boot.context.config.ConfigDataEnvironmentPostProcessor
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.PropertySource

/**
 * 这是一个对Spring的环境去进行处理的后置处理器, 它会被EnvironmentPostProcessorApplicationListener所自动回调
 *
 * @see EnvironmentPostProcessorApplicationListener
 * @see ConfigDataEnvironmentPostProcessor
 */
fun interface EnvironmentPostProcessor {
    /**
     * 对SpringApplication的Environment(环境)去进行后置处理,
     * 主要作用是提供对于配置文件的加载, 将配置文件加载成为[PropertySource]并添加到[ConfigurableEnvironment]当中
     *
     * @param application SpringApplication
     * @param environment SpringApplication的Environment
     */
    fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication)
}