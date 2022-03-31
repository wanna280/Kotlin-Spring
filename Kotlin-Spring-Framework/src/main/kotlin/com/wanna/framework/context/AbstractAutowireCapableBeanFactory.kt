package com.wanna.framework.context

import com.wanna.framework.beans.BeanDefinition

abstract class AbstractAutowireCapableBeanFactory : AbstractBeanFactory() {

    override fun createBean(beanName: String, bd: BeanDefinition): Any? {
        return doCreateBean(beanName, bd)
    }

    protected fun doCreateBean(beanName: String, bd: BeanDefinition): Any? {
        return bd.beanClass.getDeclaredConstructor().newInstance()
    }
}