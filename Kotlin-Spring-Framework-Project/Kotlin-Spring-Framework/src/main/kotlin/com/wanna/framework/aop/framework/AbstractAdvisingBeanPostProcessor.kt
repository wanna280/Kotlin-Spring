package com.wanna.framework.aop.framework

import com.wanna.framework.aop.Advisor
import com.wanna.framework.aop.support.AopUtils
import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * 支持将指定Spring Aop当中的Advisor去应用给某些特定Bean的BeanPostProcessor
 */
abstract class AbstractAdvisingBeanPostProcessor : ProxyProcessorSupport(), BeanPostProcessor {
    // 要去进行apply的Advisor
    protected var advisor: Advisor? = null

    /**
     * 在完成Bean的后置处理时，需要检验该Bean是否应该创建代理呢？
     *
     * @param bean bean
     * @param beanName beanName
     * @return 如果需要创建代理，那么return 代理对象；如果不需要代理的话，那么return bean
     */
    override fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        val advisor = advisor
        // 如果没有设置Advisor，或者该Bean是否一个Aop的基础设置Bean的话
        if (advisor == null || bean is AopInfrastructureBean) {
            return bean
        }
        // 如果它是一个合格的Bean的话，则需要去创建代理
        if (isEligible(bean, beanName)) {
            val proxyFactory = prepareProxyFactory(bean, beanName)  // prepare ProxyFactory
            proxyFactory.addAdvisor(advisor)  // add Advisor
            customizeProxyFactory(proxyFactory)  // customize for subclasses
            return proxyFactory.getProxy(getProxyClassLoader())
        }
        return bean
    }

    /**
     * 判断当前Bean是否需要去应用代理，我们主要匹配Advisor是否可以应用给当前Bean
     *
     * @param bean bean
     * @param beanName beanName
     */
    protected open fun isEligible(bean: Any, beanName: String): Boolean {
        if (this.advisor == null) {
            return false
        }
        // 使用AopUtil，去匹配这个BeanPostProcessor当中的Advisor，能否应用给当前的Bean
        return AopUtils.canApply(this.advisor!!, bean::class.java)
    }

    /**
     * 准备ProxyFactory
     *
     * @param bean bean
     * @param beanName beanName
     * @return 准备好的ProxyFactory
     */
    protected open fun prepareProxyFactory(bean: Any, beanName: String): ProxyFactory {
        val proxyFactory = ProxyFactory()
        proxyFactory.copyFrom(this)  // copy代理属性
        proxyFactory.setTarget(bean)  // setTarget
        return proxyFactory
    }

    /**
     * 在ProxyFactory彻底准备好了之后，如果必要的话，可以去自定义ProxyFactory；
     * 模板方法，交给子类方法去实现
     *
     * @param proxyFactory proxyFactory
     */
    protected open fun customizeProxyFactory(proxyFactory: ProxyFactory) {

    }
}