package com.wanna.framework.context

import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * 这是一个BeanFactory，提供Bean的管理
 */
interface BeanFactory {

    companion object {
        const val FACTORY_BEAN_PREFIX = "&"  // FactoryBean的前缀，static final变量
    }

    /**
     * 通过name去容器当中去获取Bean，允许返回null
     */
    fun getBean(beanName: String): Any?

    /**
     * 通过name和type去进行获取Bean，允许返回null
     */
    fun <T> getBean(beanName: String, type: Class<T>): T?

    /**
     * 通过type去进行获取Bean，允许返回null
     */
    fun <T> getBean(type: Class<T>): T?;

    /**
     * 根据beanName去判断该Bean是否是单例的？
     */
    fun isSingleton(beanName: String): Boolean

    /**
     * 根据beanName去判断该Bean是否是原型的？
     */
    fun isPrototype(beanName: String): Boolean

    /**
     * 添加BeanPostProcessor
     */
    fun addBeanPostProcessor(processor: BeanPostProcessor)

    /**
     * 移除BeanPostProcessor
     */
    fun removeBeanPostProcessor(type: Class<*>)

    /**
     * 根据index去移除BeanPostProcessor
     */
    fun removeBeanPostProcessor(index: Int)

    /**
     * 根据beanName去判断该Bean是否是一个FactoryBean
     */
    fun isFactoryBean(beanName: String): Boolean

    /**
     * beanName对应的Bean的类型是否匹配type？
     */
    fun isTypeMatch(beanName: String, type: Class<*>): Boolean

    /**
     * 根据beanName去匹配beanType
     */
    fun getType(beanName: String): Class<*>?
}