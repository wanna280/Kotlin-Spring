package com.wanna.framework.aop.framework.autoproxy

import com.wanna.framework.aop.Advisor
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory

/**
 * 这个组件用来协助去BeanFactory当中去寻找所有的候选的Advisor的Bean的列表
 */
open class BeanFactoryAdvisorRetrievalHelper(private val beanFactory: ConfigurableListableBeanFactory) {

    private var cachedAdvisorNames: List<String>? = null

    open fun findAdvisorBeans(): MutableList<Advisor> {
        val advisors = ArrayList<Advisor>()
        var advisorNames = this.cachedAdvisorNames
        if (this.cachedAdvisorNames == null) {
            advisorNames = beanFactory.getBeanNamesForType(Advisor::class.java)
            this.cachedAdvisorNames = advisorNames
        }
        advisorNames!!.forEach { beanName ->
            if (isEligibleBean(beanName)) {
                advisors += beanFactory.getBean(beanName, Advisor::class.java)!!
            }
        }
        return advisors
    }

    protected open fun isEligibleBean(name: String): Boolean {
        return true
    }
}