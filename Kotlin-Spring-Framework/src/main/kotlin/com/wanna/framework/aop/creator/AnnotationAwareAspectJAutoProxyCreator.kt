package com.wanna.framework.aop.creator

import com.wanna.framework.aop.Advisor

/**
 * 这是一个支持注解版的AspectJ的代理自动创建器，包括@Before/@After等AspectJ相关的注解，
 * 它会将AspectJ相关的注解标注的方法，封装成为一个PointcutAdvisor
 */
class AnnotationAwareAspectJAutoProxyCreator : AspectJAwareAdvisorAutoProxyCreator() {

    override fun findCandidateAdvisors(): MutableList<Advisor> {
        val candidateAdvisors = super.findCandidateAdvisors()
        return candidateAdvisors
    }
}