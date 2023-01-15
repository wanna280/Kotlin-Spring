package com.wanna.boot.context.config

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.boot.DefaultBootstrapContext
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
 * 它是一个配置文件的环境处理器, 它负责去处理SpringApplication当中的配置文件的加载;
 * 不管是对于普通的application配置文件, bootstrap配置文件, 甚至是一些额外的profile的配置文件, 都会被它处理
 *
 * @see EnvironmentPostProcessor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 *
 * @param bootstrapContext BootstrapContext
 * @param environmentUpdateListener 监听Environment发生变更的Listener
 */
open class ConfigDataEnvironmentPostProcessor(
    private val bootstrapContext: ConfigurableBootstrapContext = DefaultBootstrapContext(),
    @Nullable private val environmentUpdateListener: ConfigDataEnvironmentUpdateListener? = null
) : EnvironmentPostProcessor, Ordered {

    /**
     * Logger
     */
    private val logger: Logger = LoggerFactory.getLogger(ConfigDataEnvironmentPostProcessor::class.java)

    /**
     * Order很高...一般它会被第一个执行
     */
    private var order = Ordered.ORDER_HIGHEST + 10

    /**
     * 暂时提供一个无参数构造器, 后面需要干掉, 使用SpringApplication当中的配置 TODO
     */
    constructor() : this(DefaultBootstrapContext(), null)

    /**
     * 对SpringApplication的环境去进行后置处理, 主要就是加载SpringBoot相关的配置文件到环境当中
     *
     * @param environment Spring的Environment环境对象
     * @param application SpringApplication
     */
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        postProcessEnvironment(environment, application.getResourceLoader(), application.getAdditionalProfiles())
    }

    /**
     * 利用ConfigDataEnvironment当中的Contributor去对Environment去进行后置处理
     *
     * @param environment Environment
     * @param resourceLoader ResourceLoader
     * @param additionalProfiles SpringApplication额外要使用的Profiles
     */
    private fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        @Nullable resourceLoader: ResourceLoader?,
        additionalProfiles: Collection<String>
    ) {
        // 执行对于Environment的postProcess
        logger.trace("Post-processing environment to add config data")
        val resourceLoaderToUse = resourceLoader ?: DefaultResourceLoader()
        // 让所有的Contributor去对Environment去进行后置处理...并应用到Environment当中去...
        getConfigDataEnvironment(environment, resourceLoaderToUse, additionalProfiles).processAndApply()
    }

    /**
     * 创建出来一个新的ConfigDataEnvironment实例
     *
     * @param environment Environment
     * @param resourceLoader ResourceLoader
     * @param additionalProfiles SpringApplication当中指定的额外要使用的Profiles
     * @return ConfigDataEnvironment
     */
    open fun getConfigDataEnvironment(
        environment: ConfigurableEnvironment,
        resourceLoader: ResourceLoader,
        additionalProfiles: Collection<String>
    ): ConfigDataEnvironment {
        return ConfigDataEnvironment(
            environment, resourceLoader, additionalProfiles,
            this.bootstrapContext, this.environmentUpdateListener
        )
    }

    /**
     * 获取到当前[EnvironmentPostProcessor]的优先级
     *
     * @return order
     */
    override fun getOrder(): Int = this.order

    /**
     * 设置当前EnvironmentPostProcessor的优先级
     *
     * @param order 优先级数值(数值越小, 优先级越高)
     */
    open fun setOrder(order: Int) {
        this.order = order
    }


    companion object {

        /**
         * 对外提供静态工厂方法, 去将配置文件信息应用到给定的[ConfigurableEnvironment]当中来
         *
         * @param environment 要去进行应用的Environment
         */
        @JvmStatic
        fun applyTo(environment: ConfigurableEnvironment) {
            applyTo(environment, null, null)
        }

        /**
         * 对外提供静态工厂方法, 去将配置文件信息应用到给定的[ConfigurableEnvironment]当中来
         *
         * @param environment 要去进行应用的Environment
         * @param bootstrapContext BootstrapContext(可以为null)
         * @param resourceLoader ResourceLoader(可以为null)
         * @param additionalProfiles 额外要去进行应用的Profiles
         */
        @JvmStatic
        fun applyTo(
            environment: ConfigurableEnvironment,
            @Nullable bootstrapContext: ConfigurableBootstrapContext?,
            @Nullable resourceLoader: ResourceLoader?,
            vararg additionalProfiles: String
        ) {
            applyTo(environment, bootstrapContext, resourceLoader, additionalProfiles.toList(), null)
        }

        /**
         * 对外提供静态工厂方法, 去将配置文件信息应用到给定的[ConfigurableEnvironment]当中来
         *
         * @param environment 要去进行应用的Environment
         * @param bootstrapContext BootstrapContext(可以为null)
         * @param resourceLoader ResourceLoader(可以为null)
         * @param additionalProfiles 额外要去进行应用的Profiles
         * @param environmentUpdateListener 监听Environment发生变更的Listener(可以为null)
         */
        @JvmStatic
        fun applyTo(
            environment: ConfigurableEnvironment,
            @Nullable bootstrapContext: ConfigurableBootstrapContext?,
            @Nullable resourceLoader: ResourceLoader?,
            additionalProfiles: Collection<String>,
            @Nullable environmentUpdateListener: ConfigDataEnvironmentUpdateListener?
        ) {
            val bootstrapContextToUse = bootstrapContext ?: DefaultBootstrapContext()
            val postProcessor = ConfigDataEnvironmentPostProcessor(bootstrapContextToUse, environmentUpdateListener)
            postProcessor.postProcessEnvironment(environment, resourceLoader, additionalProfiles)
        }
    }
}