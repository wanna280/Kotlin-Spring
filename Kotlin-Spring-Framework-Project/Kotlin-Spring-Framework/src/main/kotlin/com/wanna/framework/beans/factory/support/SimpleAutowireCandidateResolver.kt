package com.wanna.framework.beans.factory.support

/**
 * 这是一个简单的Autowire的候选Bean的解析器，是AutowireCandidateResolver接口的最简单实现。
 *
 * @see AutowireCandidateResolver
 */
open class SimpleAutowireCandidateResolver : AutowireCandidateResolver {

    companion object {
        @JvmStatic
        val INSTANCE = SimpleAutowireCandidateResolver()
    }


    override fun isAutowireCandidate(bdHolder: BeanDefinitionHolder, descriptor: DependencyDescriptor): Boolean =
        bdHolder.beanDefinition.isAutowireCandidate()

    override fun isRequired(descriptor: DependencyDescriptor): Boolean = descriptor.isRequired()

    override fun hasQualifier(descriptor: DependencyDescriptor) = false

    override fun getSuggestedValue(descriptor: DependencyDescriptor): Any? = null
}