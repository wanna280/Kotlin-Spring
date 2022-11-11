package com.wanna.framework.beans

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.aware.Aware

/**
 * 为Spring Bean提供注入BeanFactory的Aware接口
 *
 * @see Aware
 */
interface BeanFactoryAware : Aware {

    /**
     * 自动回调，给Bean去进行设置BeanFactory
     *
     * @param beanFactory BeanFactory
     */
    fun setBeanFactory(beanFactory: BeanFactory)
}