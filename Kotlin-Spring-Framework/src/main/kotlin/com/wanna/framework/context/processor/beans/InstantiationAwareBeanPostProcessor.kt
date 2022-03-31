package com.wanna.framework.context.processor.beans

/**
 * 这是一个可以干预Bean去进行实例化的BeanPostProcessor
 */
interface InstantiationAwareBeanPostProcessor : BeanPostProcessor {

    /**
     * 对实例化之前的干预函数
     */
    fun postProcessBeforeInstantiation(beanName: String, bean: Any) {

    }

    /**
     * 对实例化之后的干预函数
     */
    fun postProcessAfterInstantiation(beanName: String, bean: Any) {

    }
}