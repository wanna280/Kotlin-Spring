package com.wanna.boot.context.config

import com.wanna.boot.SpringApplication
import com.wanna.boot.env.EnvironmentPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.lang.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 它是一个配置文件的环境处理器，它负责去处理SpringApplication当中的配置文件的加载；
 * 不管是对于普通的application配置文件，bootstrap配置文件，甚至是待profile的配置文件，都会被它处理
 */
open class ConfigDataEnvironmentPostProcessor : EnvironmentPostProcessor, Ordered {

    /**
     * Logger
     */
    private val logger: Logger = LoggerFactory.getLogger(ConfigDataEnvironmentPostProcessor::class.java)


    /**
     * Order很高...一般它会被第一个执行
     */
    private var order = Ordered.ORDER_HIGHEST + 10

    override fun getOrder(): Int = this.order

    /**
     * 设置当前EnvironmentPostProcessor的优先级
     *
     * @param order 优先级数值(数值越小, 优先级越高)
     */
    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 对SpringApplication的环境去进行后置处理，主要就是加载SpringBoot相关的配置文件到环境当中
     *
     * @param environment 环境对象
     * @param application SpringApplication
     */
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        postProcessEnvironment(environment, application.getResourceLoader(), application.getAdditionalProfiles())
    }


    private fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        @Nullable resourceLoader: ResourceLoader?,
        additionalProfiles: Collection<String>
    ) {
        // 执行对于Environment的postProcess
        logger.trace("Post-processing environment to add config data")
        val resourceLoaderToUse = resourceLoader ?: DefaultResourceLoader()
        getConfigDataEnvironment(environment, resourceLoaderToUse, additionalProfiles).processAndApply()
    }

    open fun getConfigDataEnvironment(
        environment: ConfigurableEnvironment,
        resourceLoader: ResourceLoader,
        additionalProfiles: Collection<String>
    ): ConfigDataEnvironment {
        return ConfigDataEnvironment(environment, resourceLoader, additionalProfiles)
    }
}