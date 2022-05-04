package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.beans.factory.config.EmbeddedValueResolver
import com.wanna.framework.context.*
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * 这是一个ApplicationContext的处理器，注册负责完成一些Aware接口的回调
 *
 * @see EnvironmentAware
 * @see BeanClassLoaderAware
 * @see ApplicationContextAware
 * @see ApplicationEventPublisherAware
 * @see EmbeddedValueResolverAware
 */
open class ApplicationContextAwareProcessor(private var applicationContext: ConfigurableApplicationContext) :
    BeanPostProcessor {

    // 嵌入式值解析器
    private val embeddedValueResolver = EmbeddedValueResolver(this.applicationContext.getBeanFactory())

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
            bean.setBeanClassLoader(applicationContext.getBeanFactory().getBeanClassLoader())
        }
        if (bean is ApplicationContextAware) {
            bean.setApplicationContext(this.applicationContext)
        }
        if (bean is ApplicationEventPublisherAware) {
            bean.setApplicationEventPublisher(this.applicationContext)
        }
        if (bean is EmbeddedValueResolverAware) {
            bean.setEmbeddedValueResolver(this.embeddedValueResolver)
        }
        if (bean is ApplicationStartupAware) {
            bean.setApplicationStartup(this.applicationContext.getApplicationStartup())
        }
    }
}