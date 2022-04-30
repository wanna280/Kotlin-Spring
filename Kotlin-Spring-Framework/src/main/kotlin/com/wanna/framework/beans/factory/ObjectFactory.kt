package com.wanna.framework.beans.factory

/**
 * 这是一个ObjectFactory，提供去获取BeanObject的方法
 */
interface ObjectFactory<T> {
    /**
     * 提供去获取BeanObject的方式
     */
    fun getObject(): T
}