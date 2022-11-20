package com.wanna.framework.beans.factory

/**
 * 这是一个更加智能的FactoryBean，它能决定一个FactoryBean是否应该被懒加载
 *
 * @see FactoryBean
 */
interface SmartFactoryBean<T> : FactoryBean<T> {

    /**
     * 该FactoryBean是否是渴望被加载的？
     *
     * @return eagerInit? return true则在SpringBeanFactory启动时就完成初始化; return false则是懒加载的
     */
    fun isEagerInit(): Boolean
}