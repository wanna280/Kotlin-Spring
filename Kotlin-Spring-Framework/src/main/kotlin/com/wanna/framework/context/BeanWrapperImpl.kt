package com.wanna.framework.context

class BeanWrapperImpl(_beanInstance: Any) : BeanWrapper {

    private val beanInstance  = _beanInstance

    override fun getWrappedInstance(): Any {
        return beanInstance
    }

    override fun getWrappedClass(): Class<*> {
        return beanInstance::class.java
    }
}