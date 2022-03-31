package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.context.BeanFactory
import com.wanna.framework.context.aware.BeanFactoryAware
import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * 这是一个用来处理配置属性的绑定的PostProcessor，主要是处理@onfigurationProperties注解
 */
class ConfigurationPropertiesBindingPostProcessor :BeanPostProcessor,BeanFactoryAware {
    override fun setBeanFactory(beanFactory: BeanFactory) {
        TODO("Not yet implemented")
    }
}