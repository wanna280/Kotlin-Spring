package com.wanna.framework.beans.factory.support

/**
 * 标识这是一个NullBean, 在InstanceSupplier/FactoryMethod时,
 * 如果方法的返回值返回了一个Null, 那么就会使用NullBean去进行标识
 */
class NullBean {
    override fun equals(other: Any?): Boolean = other == null
    override fun hashCode(): Int = javaClass.hashCode()
}