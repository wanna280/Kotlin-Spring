package com.wanna.framework.context.processor.factory.internal

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.event.EventListenerFactory
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor

/**
 * 这是一个EventListener的方法处理器
 */
open class EventListenerMethodProcessor : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val eventListenerFactoryNames = beanFactory.getBeanNamesForType(EventListenerFactory::class.java, false, false)
        val factories = ArrayList<EventListenerFactory>()
        eventListenerFactoryNames.forEach { factories.add(beanFactory.getBean(it, EventListenerFactory::class.java)) }
    }
}