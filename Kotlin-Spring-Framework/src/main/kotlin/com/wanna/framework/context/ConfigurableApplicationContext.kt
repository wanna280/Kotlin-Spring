package com.wanna.framework.context

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.metrics.ApplicationStartup

/**
 * 这是一个可以被配置的ApplicationContext
 */
interface ConfigurableApplicationContext : ApplicationContext {

    companion object {
        const val CONVERSION_SERVICE_BEAN_NAME = "conversionService"  // ConversionService beanName
        const val ENVIRONMENT_BEAN_NAME = "environment"  // Environment beanName
        const val SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties"  // SystemProperties beanName
        const val SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment"  // SystemEnvironment beanName
        const val APPLICATION_STARTUP_BEAN_NAME = "applicationStartup"  // ApplicationStartup beanName
        const val LOAD_TIME_WEAVER_BEAN_NAME = "loadTimeWeaver"  // LoadTimeWeaver beanName
    }

    /**
     * 获取ApplicationStartup
     */
    fun getApplicationStartup() : ApplicationStartup

    /**
     * 设置ApplicationStartup
     */
    fun setApplicationStartup(applicationStartup: ApplicationStartup)

    /**
     * 刷新容器，完成所有Bean的实例化和初始化
     */
    fun refresh();

    /**
     * 往ApplicationContext中添加BeanFactoryPostProcessor
     */
    fun addBeanFactoryPostProcessor(processor: BeanFactoryPostProcessor)

    /**
     * 获取ApplicationContext中的BeanFactory，这里可以获取到的类型是ConfigurableListableBeanFactory
     */
    fun getBeanFactory(): ConfigurableListableBeanFactory

    /**
     * 设置Environment
     */
    fun setEnvironment(environment: ConfigurableEnvironment)

    /**
     * 重写子类中的getEnvironment方法，让返回值为ConfigurableEnvironment
     */
    override fun getEnvironment(): ConfigurableEnvironment

    /**
     * 添加ApplicationListener到ApplicationContext当中
     */
    fun addApplicationListener(listener: ApplicationListener<*>)

    /**
     * 设置parent ApplicationContext
     */
    fun setParent(parent: ApplicationContext)
}