package com.wanna.framework.context.processor.factory

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory

/**
 * 这是一个BeanFactoryPostProcessor，可以在BeanFactory初始化过程中，对BeanFactory去进进行干预工作
 */
interface BeanFactoryPostProcessor {

    /**
     * 实现这个方法，可以对BeanFactory去完成初始化
     */
    fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory);
}