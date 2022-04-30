package com.wanna.framework.beans

/**
 * 这是一个BeanWrapper，提供了对属性值的访问
 */
interface BeanWrapper : PropertyAccessor {
    fun getWrappedInstance(): Any
    fun getWrappedClass(): Class<*>
}