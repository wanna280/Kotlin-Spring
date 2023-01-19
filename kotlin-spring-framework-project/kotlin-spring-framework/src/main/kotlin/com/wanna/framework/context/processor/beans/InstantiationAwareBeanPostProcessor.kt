package com.wanna.framework.context.processor.beans

import com.wanna.framework.beans.PropertyValues

/**
 * 这是一个可以干预Bean去进行实例化的BeanPostProcessor, 主要提供如下的功能：
 * * 1.在实例化之前, 尝试对Bean去进行干预, 支持去自定义创建Bean的逻辑
 * * 2.在完成Bean的实例化之后, 可以尝试去对Bean去进行干预, 完成自定义的处理逻辑
 * * 3.支持去对Bean去进行属性的填充(Autowire)的回调callback
 *
 * 它的子类SmartInstantiationAwareBeanPostProcessor可以实现更多的功能
 *
 * @see SmartInstantiationAwareBeanPostProcessor
 */
interface InstantiationAwareBeanPostProcessor : BeanPostProcessor {

    /**
     * 对实例化之前的干预函数, 可以在这里去干涉一个Bean的创建, 也可以去在这里去对Bean的创建流程去完成自定义, 创建自己的自定义Bean
     *
     * @param beanName beanName
     * @param beanClass beanClass
     * @return 干涉的结果, 如果这里返回了一个非空的Bean, 那么将会停止Bean的创建流程, 直接apply afterInitialization的回调方法了
     * @see postProcessAfterInitialization
     */
    fun postProcessBeforeInstantiation(beanName: String, beanClass: Class<*>): Any? {
        return null
    }

    /**
     * 对实例化之后的干预函数, 可以在这里对Bean去相关的进行自定义操作
     *
     * @param bean bean
     * @param beanName beanName
     * @return 是否还要继续下一个BeanPostProcessor, 让它来干涉该Bean的创建? 如果return false, 停止后续的BeanPostProcessor; return true则继续
     */
    fun postProcessAfterInstantiation(beanName: String, bean: Any): Boolean {
        return true
    }

    /**
     * 处理Properties属性注入, 完成Autowire自动注入
     *
     * @param pvs 属性值列表(有可能为null)
     * @param beanName beanName
     * @param bean bean
     * @return PropertyValues
     */
    fun postProcessProperties(pvs: PropertyValues?, bean: Any, beanName: String): PropertyValues? {
        return pvs
    }
}