package com.wanna.framework.context.processor.factory.internal

import com.wanna.framework.context.BeanFactory
import com.wanna.framework.context.ConfigurableListableBeanFactory
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor

/**
 * 这是一个EventListener的方法处理器
 */
open class EventListenerMethodProcessor : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

    }
}