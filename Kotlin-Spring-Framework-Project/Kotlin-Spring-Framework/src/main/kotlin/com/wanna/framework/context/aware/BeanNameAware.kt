package com.wanna.framework.context.aware

/**
 * 设置BeanName的Aware
 */
interface BeanNameAware : Aware {
    fun setBeanName(beanName: String)
}