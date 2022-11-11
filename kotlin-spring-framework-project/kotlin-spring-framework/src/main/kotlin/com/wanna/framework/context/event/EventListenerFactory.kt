package com.wanna.framework.context.event

import java.lang.reflect.Method

/**
 * EventListener的工厂，负责将方法包装成为一个EventListener
 *
 * @see EventListener
 * @see DefaultEventListenerFactory
 */
interface EventListenerFactory {

    /**
     * 是否支持将给定的方法包装成为EventListener？
     *
     * @param method 你想要去包装成为EventListener的方法
     * @return 如果支持去进行包装，return true；否则return false
     */
    fun supportsMethod(method: Method): Boolean

    /**
     * 如果支持处理该方法的话，那么需要根据该方法去创建一个[ApplicationListener]
     *
     * @param beanName beanName
     * @param type beanClass
     * @param method method
     * @return 包装之后的ApplicationListener
     */
    fun <T> createApplicationListener(beanName: String, type: Class<T>, method: Method): ApplicationListener<*>
}