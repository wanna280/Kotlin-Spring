package com.wanna.boot

import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 这是一个SpringApplication的运行监听器，负责监听SpringApplication应用启动过程当中的各个阶段的事件
 */
interface SpringApplicationRunListener {

    /**
     * SpringApplication正在启动的过程当中
     */
    fun starting(bootstrapContext: ConfigurableBootstrapContext) {

    }

    /**
     * SpringApplication的环境已经准备好，可以对环境去进行后置处理
     */
    fun environmentPrepared(
        bootstrapContext: ConfigurableBootstrapContext,
        configurableEnvironment: ConfigurableEnvironment
    ) {

    }

    /**
     * SpringApplication的ApplicationContext已经准备好
     */
    fun contextPrepared(context: ConfigurableApplicationContext) {

    }

    /**
     * SpringApplication的ApplicationContext已经刷新完成
     */
    fun contextLoaded(context: ConfigurableApplicationContext) {

    }

    /**
     * SpringApplication已经启动完成
     */
    fun started(context: ConfigurableApplicationContext) {

    }

    /**
     * SpringApplication正处于运行当中
     */
    fun running(context: ConfigurableApplicationContext) {

    }

    /**
     * SpringApplication启动失败
     */
    fun failed(context: ConfigurableApplicationContext?, ex: Throwable) {

    }
}