package com.wanna.boot.context.config

import com.wanna.boot.SpringApplication
import com.wanna.boot.env.EnvironmentPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 它是一个配置文件的环境处理器，它负责去处理SpringApplication当中的配置文件的加载；
 * 不管是对于普通的application配置文件，bootstrap配置文件，甚至是待profile的配置文件，都会被它处理
 */
open class ConfigDataEnvironmentPostProcessor : EnvironmentPostProcessor, Ordered {

    // Order很高...一般它会被第一个执行
    private var order: Int = Ordered.ORDER_HIGHEST + 10

    override fun getOrder(): Int {
        return order
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 对环境去进行后置处理，主要就是加载相关的配置文件到环境当中
     *
     * @param environment 环境对象
     * @param application SpringApplication
     */
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        getConfigDataEnvironment(environment).processAndApply()
    }

    open fun getConfigDataEnvironment(environment: ConfigurableEnvironment): ConfigDataEnvironment {
        return ConfigDataEnvironment(environment)
    }
}