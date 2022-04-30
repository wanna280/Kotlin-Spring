package com.wanna.framework.beans

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.aware.Aware

/**
 * 设置BeanFactory1的Aware
 */
interface BeanFactoryAware : Aware {
    fun setBeanFactory(beanFactory: BeanFactory)
}