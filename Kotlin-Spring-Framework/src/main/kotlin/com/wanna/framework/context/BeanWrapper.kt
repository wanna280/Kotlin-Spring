package com.wanna.framework.context

interface BeanWrapper {
    fun getWrappedInstance() : Any

    fun getWrappedClass() : Class<*>
}