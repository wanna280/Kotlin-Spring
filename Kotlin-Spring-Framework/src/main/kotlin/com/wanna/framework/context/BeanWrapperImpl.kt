package com.wanna.framework.context

class BeanWrapperImpl(_beanInstance: Any) : BeanWrapper {

    private val beanInstance  = _beanInstance

    override fun getBeanInstance(): Any {
        return beanInstance
    }
}