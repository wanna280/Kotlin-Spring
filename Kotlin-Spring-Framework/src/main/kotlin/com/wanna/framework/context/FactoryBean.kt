package com.wanna.framework.context

/**
 * 这是一个FactoryBean，用来给容器中导入组件
 */
interface FactoryBean<T> {

    /**
     * 获取要导入的组件的类型
     */
    fun getObjectType(): Class<T>

    /**
     * 获取要给容器中导入的组件对象
     */
    fun getObject(): T

    /**
     * 是否是单例的？
     */
    fun isSingleton(): Boolean

    /**
     * 是否是原型的？
     */
    fun isPrototype(): Boolean
}