package com.wanna.framework.beans.factory

/**
 * 这是一个FactoryBean，用来给容器中导入组件
 */
interface FactoryBean<T> {

    companion object {
        const val OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";
    }

    /**
     * 获取当前的FactoryBean要导入的组件的类型
     */
    fun getObjectType(): Class<out T>

    /**
     * 获取要给容器中导入的组件对象(FactoryBeanObject)
     */
    fun getObject(): T

    /**
     * 要导入的FactoryBeanObject是否是单例的？
     */
    fun isSingleton(): Boolean

    /**
     * 要导入的FactoryBeanObject是否是原型的？
     */
    fun isPrototype(): Boolean
}