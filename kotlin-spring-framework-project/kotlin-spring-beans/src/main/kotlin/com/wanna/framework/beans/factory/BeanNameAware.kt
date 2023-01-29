package com.wanna.framework.beans.factory

/**
 * 自动注入当前Bean的BeanName的Aware
 *
 * @see Aware
 */
fun interface BeanNameAware : Aware {
    /**
     * 自动注入beanName
     *
     * @param beanName beanName
     */
    fun setBeanName(beanName: String)
}