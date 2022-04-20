package com.wanna.framework.aop.creator

import com.wanna.framework.aop.framework.ProxyFactory
import com.wanna.framework.context.processor.beans.SmartInstantiationAwareBeanPostProcessor

/**
 * 这是一个用来完成代理的BeanPostProcessor
 */
abstract class AbstractAutoProxyCreator : SmartInstantiationAwareBeanPostProcessor {
    override fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        if (beanName == "iTFImpl") {
            val proxyFactory = ProxyFactory(bean)
            val proxy = proxyFactory.getProxy()
            return proxy
        }
        return super.postProcessAfterInitialization(beanName, bean)
    }
}