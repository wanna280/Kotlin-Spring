package com.wanna.boot.logging

import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * [LoggingSystem]的初始化时需要使用到的一些上下文参数信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @param environment Environment
 */
data class LoggingInitializationContext(val environment: ConfigurableEnvironment)