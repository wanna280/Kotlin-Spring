package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.BeanFactoryAware

/**
 * 它是一个支持泛型的检查的AutowireCandidateResolver
 */
open class GenericTypeAwareAutowireCandidateResolver : BeanFactoryAware, SimpleAutowireCandidateResolver() {

    private var beanFactory: BeanFactory? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    fun getBeanFactory() = this.beanFactory

    override fun isAutowireCandidate(bdHolder: BeanDefinitionHolder, descriptor: DependencyDescriptor): Boolean {
        // 检查BeanDefinition
        if (!super.isAutowireCandidate(bdHolder, descriptor)) {
            return false
        }
        // 检查泛型类型是否匹配
        return checkGenericTypeMatch(bdHolder, descriptor)
    }

    private fun checkGenericTypeMatch(bdHolder: BeanDefinitionHolder, descriptor: DependencyDescriptor): Boolean {
        return true
    }
}