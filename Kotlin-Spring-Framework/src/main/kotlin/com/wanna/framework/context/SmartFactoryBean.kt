package com.wanna.framework.context

/**
 * 这是一个更加只能的FactoryBean，它能决定FactoryBean是否应该被懒加载
 */
interface SmartFactoryBean<T> : FactoryBean<T> {

    /**
     * 是否是渴望被加载的？如果是，初始化容器时，就完成FactoryBeanObject的导入
     */
    fun isEagerInit(): Boolean
}