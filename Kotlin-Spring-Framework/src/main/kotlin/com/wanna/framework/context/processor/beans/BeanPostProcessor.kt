package com.wanna.framework.context.processor.beans

/**
 * 这是一个BeanPostProcessor，可以在创建Bean和实例化Bean的各个时机去对Bean进行干预
 */
interface BeanPostProcessor {
    /**
     * Bean初始化之前的回调
     */
    fun postProcessBeforeInitialization(beanName: String, bean: Any) : Any? {
        return bean
    }

    /**
     * Bean初始化之后的回调
     */
    fun postProcessAfterInitialization(beanName: String,bean: Any) : Any? {
        return bean
    }
}