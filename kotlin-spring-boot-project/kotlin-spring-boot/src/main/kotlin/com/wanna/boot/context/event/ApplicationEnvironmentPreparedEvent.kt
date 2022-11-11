package com.wanna.boot.context.event

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.boot.SpringApplication
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 在SpringApplication的环境对象已经准备好时会自动回调
 */
class ApplicationEnvironmentPreparedEvent(
    val bootstrapContext: ConfigurableBootstrapContext,
    application: SpringApplication,
    args: Array<String>,
    val environment: ConfigurableEnvironment
) : SpringApplicationEvent(application, args) {

}