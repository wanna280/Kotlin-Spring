package com.wanna.framework.aop.framework

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory

/**
 * 在AbstractAdvisingBeanPostProcessor的基础上, 新增了BeanFactoryAware的支持, 支持获取到BeanFactory
 *
 * @see BeanFactoryAware
 */
abstract class AbstractBeanFactoryAwareAdvisingPostProcessor : AbstractAdvisingBeanPostProcessor(), BeanFactoryAware {

    private var beanFactory: ConfigurableListableBeanFactory? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        if (beanFactory is ConfigurableListableBeanFactory) {
            this.beanFactory = beanFactory
        }
    }

    open fun getBeanFactory(): ConfigurableListableBeanFactory? {
        return this.beanFactory
    }
}