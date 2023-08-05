package com.wanna.framework.aop.creator

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory

/**
 * 这是一个基础设施的Advisor的自动代理的创建器, 它会针对于是Role为基础设施(ROLE_INFRASTRUCTURE)的Bean的Advisor去进行创建代理
 *
 * @see BeanDefinition.ROLE_INFRASTRUCTURE
 */
open class InfrastructureAdvisorAutoProxyCreator : AbstractAdvisorAutoProxyCreator() {

    private var beanFactory: ConfigurableListableBeanFactory? = null

    override fun initBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        super.initBeanFactory(beanFactory)
        this.beanFactory = beanFactory
    }

    /**
     * 只要从BeanDefinition当中去判断它是一个基础设施Bean, 那么它就是一个符合条件的Advisor的Bean
     *
     * @param name beanName
     * @return 从BeanDefinition去判断它是否是一个基础设施Bean
     */
    override fun isEligibleBean(name: String): Boolean {
        val beanFactory = beanFactory ?: return false
        return beanFactory.containsBeanDefinition(name) && beanFactory.getBeanDefinition(name)
            .getRole() == BeanDefinition.ROLE_INFRASTRUCTURE
    }
}