package com.wanna.framework.context

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.metrics.ApplicationStartup
import java.io.Closeable

/**
 * 这是一个可以被配置的[ApplicationContext]，提供了对于[ApplicationContext]的各种自定义操作的方法；
 * 实现了JDK当中提供的[Closeable]接口，提供了`close`方法实现对于[ApplicationContext]的关闭;
 * 为所有的[ApplicationContext]的子类的实现提供了相关的支持
 *
 * @see ApplicationContext
 * @see Closeable
 * @see com.wanna.framework.context.support.AbstractApplicationContext
 * @see Lifecycle
 * @see LifecycleProcessor
 */
interface ConfigurableApplicationContext : ApplicationContext, Closeable, Lifecycle {

    companion object {
        /**
         * ConversionService beanName
         */
        const val CONVERSION_SERVICE_BEAN_NAME = "conversionService"

        /**
         * Environment beanName
         */
        const val ENVIRONMENT_BEAN_NAME = "environment"

        /**
         * SystemProperties beanName
         */
        const val SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties"

        /**
         * SystemEnvironment beanName
         */
        const val SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment"

        /**
         * ApplicationStartup beanName
         */
        const val APPLICATION_STARTUP_BEAN_NAME = "applicationStartup"

        /**
         * LoadTimeWeaver beanName
         */
        const val LOAD_TIME_WEAVER_BEAN_NAME = "loadTimeWeaver"

        /**
         * ShutdownHook的线程名
         */
        const val SHUTDOWN_HOOK_THREAD_NAME = "SpringContextShutdownHook"
    }

    /**
     * 获取当前ApplicationContext需要使用到的ApplicationStartup
     *
     * @return ApplicationStartup
     */
    fun getApplicationStartup(): ApplicationStartup

    /**
     * 设置当前ApplicationContext要使用的ApplicationStartup，
     * 通过ApplicationStartup，可以提供Spring应用启动的相关的性能监测工作
     *
     * @param applicationStartup ApplicationStartup
     */
    fun setApplicationStartup(applicationStartup: ApplicationStartup)

    /**
     * 刷新当前的ApplicationContext，完成所有Bean的实例化和初始化工作
     */
    fun refresh();

    /**
     * 当前的ApplicationContext是否还是存活的？
     *
     * @return 如果当前ApplicationContext没被关闭，return true，被关闭了则return false
     */
    fun isActive(): Boolean

    /**
     * 关闭ApplicationContext并释放掉相关的所有资源
     */
    override fun close()

    /**
     * 为当前[ApplicationContext]去注册一个ShutdownHook
     */
    fun registerShutdownHook()

    /**
     * 往ApplicationContext中添加BeanFactoryPostProcessor
     *
     * @param processor 你想要添加的BeanFactoryPostProcessor
     */
    fun addBeanFactoryPostProcessor(processor: BeanFactoryPostProcessor)

    /**
     * 获取Spring ApplicationContext中的BeanFactory，这里可以获取到的类型是ConfigurableListableBeanFactory
     *
     * @return 获取到ApplicationContext内部使用的BeanFactory，类型是ConfigurableListableBeanFactory
     */
    fun getBeanFactory(): ConfigurableListableBeanFactory

    /**
     * 设置当前Spring ApplicationContext的Environment
     *
     * @param environment 当前的ApplicationContext想要使用的Environment
     */
    fun setEnvironment(environment: ConfigurableEnvironment)

    /**
     * 重写子类中的getEnvironment方法，让返回值为ConfigurableEnvironment
     *
     * @return 当前Spring ApplicationContext对应的ConfigurableEnvironment
     */
    override fun getEnvironment(): ConfigurableEnvironment

    /**
     * 添加ApplicationListener到当前的ApplicationContext当中
     *
     * @param listener 想要添加到ApplicationContext当中的ApplicationListener
     */
    fun addApplicationListener(listener: ApplicationListener<*>)

    /**
     * 设置parent ApplicationContext
     *
     * @param parent 你想使用的parentApplicationContext(可以为null)
     */
    fun setParent(parent: ApplicationContext?)

    /**
     * 设置加载BeanClass的ClassLoader
     *
     * @param beanClassLoader 你想要使用的ClassLoader
     */
    fun setBeanClassLoader(beanClassLoader: ClassLoader)
}