package com.wanna.framework.aop.framework.autoproxy

import com.wanna.framework.aop.Advisor
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import org.slf4j.LoggerFactory

/**
 * 这个组件用来协助去BeanFactory当中去寻找所有的候选的Advisor的Bean的列表
 *
 * @param beanFactory 目标BeanFactory
 */
open class BeanFactoryAdvisorRetrievalHelper(private val beanFactory: ConfigurableListableBeanFactory) {

    companion object {
        private val logger = LoggerFactory.getLogger(BeanFactoryAdvisorRetrievalHelper::class.java)
    }

    private var cachedAdvisorNames: List<String>? = null

    /**
     * 从beanFactory当中去进行寻找Advisor的Bean的列表
     *
     * @return Advisor列表
     */
    open fun findAdvisorBeans(): List<Advisor> {
        val advisors = ArrayList<Advisor>()
        var advisorNames = this.cachedAdvisorNames
        if (advisorNames == null) {
            advisorNames = beanFactory.getBeanNamesForType(Advisor::class.java)
            this.cachedAdvisorNames = advisorNames
        }
        advisorNames.forEach { beanName ->
            if (isEligibleBean(beanName)) {
                /**
                 * 这里要忽略skip正在创建的Advisor，是必须要去进行做的，不然就会出现循环依赖的情况；
                 *
                 * 比如下面这样的代码当中：
                 *
                 * ```kotlin
                 * @Configuration(proxyBeanMethods = false)
                 * class MyConfiguration {
                 *     @Bean
                 *     open fun advisor() : Advisor {
                 *        return MyAdvisor()
                 *     }
                 * }
                 * ```
                 *
                 * 我们想要创建MyAdvisor，就得创建MyConfiguration，而MyConfiguration创建过程当中，就会来到这里去寻找候选的Advisor；
                 * 就会触发getBean(MyAdvisor)，但是因为MyAdvisor之前已经在创建当中了，因此正常情况下的话有可能会触发循环依赖的异常；
                 * 因此我们在这里需要去排除掉正在创建当中的Advisor，也就是说MyAdvisor不能应用给MyConfiguration，不然就会出现循环依赖；
                 * 但是在Spring当中，这样的代码其实用的非常多，比如SpringTransaction，SpringCache都会用到，因此我们在这里采用skip掉的方式，去进行解决该问题。
                 */
                if (beanFactory.isCurrentlyInCreation(beanName)) {  // skip current in creation...
                    if (logger.isTraceEnabled) {
                        logger.trace("忽略正在创建的Advisor[beanName=$beanName]")
                    }
                } else {
                    advisors += beanFactory.getBean(beanName, Advisor::class.java)
                }
            }
        }
        return advisors
    }

    protected open fun isEligibleBean(name: String): Boolean {
        return true
    }
}