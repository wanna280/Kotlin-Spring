package com.wanna.framework.beans

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.aware.Aware

/**
 * 为Spring Bean提供注入BeanFactory的Aware接口
 */
interface BeanFactoryAware : Aware {
    fun setBeanFactory(beanFactory: BeanFactory)
}