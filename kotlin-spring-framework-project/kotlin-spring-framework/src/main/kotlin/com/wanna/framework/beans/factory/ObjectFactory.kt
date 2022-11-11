package com.wanna.framework.beans.factory

/**
 * 这是一个ObjectFactory，提供去获取BeanObject的方法
 *
 * @param T 要获取的Bean的类型
 */
@FunctionalInterface
interface ObjectFactory<T> {
    /**
     * 提供去获取BeanObject的方式
     *
     * @return Bean
     */
    fun getObject(): T
}