package com.wanna.framework.context.processor.beans

/**
 * 这是一个BeanPostProcessor，可以在创建Bean和实例化Bean的各个时机去对Bean进行干预
 *
 * @see MergedBeanDefinitionPostProcessor
 * @see InstantiationAwareBeanPostProcessor
 * @see SmartInstantiationAwareBeanPostProcessor
 */
interface BeanPostProcessor {
    /**
     * Bean初始化之前的回调，可以通过这个方法去干涉Bean的初始化操作
     *
     * @param bean bean
     * @param beanName beanName
     * @return 经过自定义操作处理之后的bean(return null代表终止之后的BeanPostProcessor的操作)
     */
    fun postProcessBeforeInitialization(beanName: String, bean: Any): Any? {
        return bean
    }

    /**
     * Bean初始化之后的回调，可以通过这个方法去干涉Bean的初始化操作
     *
     * @param bean bean
     * @param beanName beanName
     * @return 经过自定义操作处理之后的bean(return null代表终止之后的BeanPostProcessor的操作)
     */
    fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        return bean
    }
}