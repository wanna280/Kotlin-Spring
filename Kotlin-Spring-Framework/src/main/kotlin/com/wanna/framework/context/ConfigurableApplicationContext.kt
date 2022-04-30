package com.wanna.framework.context

import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 这是一个可以被配置的ApplicationContext
 */
interface ConfigurableApplicationContext : ApplicationContext {

    companion object {
        const val CONVERSION_SERVICE_BEAN_NAME = "conversionService"  // ConversionService beanName
    }

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
    override fun getEnvironment() : ConfigurableEnvironment
}