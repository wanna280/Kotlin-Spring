package com.wanna.framework.aop.support

import com.wanna.framework.aop.Advice
import com.wanna.framework.context.BeanFactory
import com.wanna.framework.context.ConfigurableBeanFactory
import com.wanna.framework.context.aware.BeanFactoryAware

/**
 * 这是一个基于BeanFactory的PointAdvisor，它拥有了从BeanFactory中获取Advisor的能力
 *
 * (1)如果指定的advice，那么将会采用指定的advice
 * (2)如果指定了adviceBeanName时，那么将会从容器中获取Advice
 */
abstract class AbstractBeanFactoryPointcutAdvisor : AbstractPointcutAdvisor(), BeanFactoryAware {

    private var advice: Advice? = null

    private var beanFactory: BeanFactory? = null

    private var adviceBeanName: String? = null

    private var advisorLock: Any = Any()

    fun getBeanFactory(): BeanFactory? {
        return beanFactory
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
        resetAdvisorLock()
    }

    private fun resetAdvisorLock() {
        checkNotNull(beanFactory != null) { "beanFactory不能为空" }
        if (beanFactory is ConfigurableBeanFactory) {
            this.advisorLock = (beanFactory as ConfigurableBeanFactory).getSingletonMutex()
        } else {
            this.advisorLock = Any()
        }
    }

    fun setAdvice(advice: Advice) {
        synchronized(advisorLock) {
            this.advice = advice
        }
    }

    fun setAdviceBeanName(adviceBeanName: String) {
        this.adviceBeanName = adviceBeanName
    }

    override fun getAdvice(): Advice {
        if (advice != null) {
            return advice!!
        }
        checkNotNull(adviceBeanName) { "adviceBeanName和advice不能同时为空" }
        checkNotNull(beanFactory) { "beanFactory不能为空" }
        if (beanFactory!!.isSingleton(adviceBeanName!!)) {
            val advice = beanFactory!!.getBean(adviceBeanName!!, Advice::class.java)!!
            this.advice = advice
            return advice
        } else {
            synchronized(advisorLock) {
                var advice = this.advice
                if (advice == null) {
                    advice = beanFactory!!.getBean(adviceBeanName!!, Advice::class.java)!!
                }
                this.advice = advice
                return advice
            }
        }
    }
}