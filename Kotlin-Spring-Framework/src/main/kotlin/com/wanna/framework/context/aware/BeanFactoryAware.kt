package com.wanna.framework.context.aware

import com.wanna.framework.context.BeanFactory

/**
 * 设置BeanFactory1的Aware
 */
interface BeanFactoryAware : Aware {
    fun setBeanFactory(beanFactory: BeanFactory)
}