package com.wanna.framework.context.processor.beans

import com.wanna.framework.beans.PropertyValues

/**
 * 这是一个可以干预Bean去进行实例化的BeanPostProcessor
 */
interface InstantiationAwareBeanPostProcessor : BeanPostProcessor {

    /**
     * 对实例化之前的干预函数
     */
    fun postProcessBeforeInstantiation(beanName: String, bean: Any): Any? {
        return null
    }

    /**
     * 对实例化之后的干预函数
     */
    fun postProcessAfterInstantiation(beanName: String, bean: Any): Boolean {
        return true
    }

    /**
     * 处理Properties属性注入
     */
    fun postProcessProperties(pvs: PropertyValues?, bean: Any, beanName: String): PropertyValues? {
        return null
    }
}