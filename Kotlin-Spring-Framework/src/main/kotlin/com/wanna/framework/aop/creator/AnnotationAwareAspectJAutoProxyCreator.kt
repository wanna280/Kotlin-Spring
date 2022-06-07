package com.wanna.framework.aop.creator

import com.wanna.framework.aop.Advisor

/**
 * 这是一个支持注解版的AspectJ的代理自动创建器，包括@Before/@After等AspectJ相关的注解，
 * 它会将AspectJ相关的注解标注的方法，封装成为一个PointcutAdvisor
 *
 * @see AspectJAwareAdvisorAutoProxyCreator
 * @see AbstractAutoProxyCreator
 * @see AbstractAdvisorAutoProxyCreator
 */
class AnnotationAwareAspectJAutoProxyCreator : AspectJAwareAdvisorAutoProxyCreator() {

    /**
     * 寻找候选的Advisor，这里需要新增，从AspectJ的注解当中去进行寻找的方式
     *
     * // TODO
     */
    override fun findCandidateAdvisors(): MutableList<Advisor> {
        val candidateAdvisors = super.findCandidateAdvisors()
        return candidateAdvisors
    }
}