package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.aware.ApplicationContextAware
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.BeanFactoryAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * 这是一个ApplicationContext的处理器，注册负责完成一些Aware接口的回调
 */
open class ApplicationContextAwareProcessor(private var applicationContext: ApplicationContext) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any? {
        invokeAwareInterfaces(bean)
        return bean
    }

    /**
     * 执行所有的Aware接口，完成容器对象的注入
     */
    open fun invokeAwareInterfaces(bean: Any) {
        if (bean is EnvironmentAware) {
            bean.setEnvironment(applicationContext.getEnvironment())
        }
        if (bean is BeanClassLoaderAware) {
            val beanClassLoader =
                (applicationContext as ConfigurableApplicationContext).getBeanFactory().getBeanClassLoader()
            bean.setBeanClassLoader(beanClassLoader)
        }
        if (bean is ApplicationContextAware) {
            bean.setApplicationContext(this.applicationContext)
        }
    }
}