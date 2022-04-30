package com.wanna.boot.web.server

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * 这是一个对WebServerFactory去进行自定义的BeanPostProcessor
 */
open class WebServerFactoryCustomizerBeanPostProcessor : BeanPostProcessor, BeanFactoryAware {

    private val beanFactory: ListableBeanFactory? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory as ListableBeanFactory
    }

    /**
     * 在Bean初始化之前，去判断Bean是否是WebServerFactory；
     * 如果它是WebServerFactory的话，那么从容器当中拿到所有的WebServerCustomizer去对WebServer去进行自定义
     *
     * @see WebServerFactoryCustomizer
     * @see WebServerFactory
     * @see WebServer
     */
    @Suppress("UNCHECKED_CAST")
    override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any? {
        if (bean is WebServerFactory) {
            val customizers = beanFactory!!.getBeansForType(WebServerFactoryCustomizer::class.java).values
            customizers.forEach { (it as WebServerFactoryCustomizer<WebServerFactory>).customize(bean) }
        }
        return bean
    }
}