package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.util.ClassUtils

/**
 * 它是一个支持泛型的检查的AutowireCandidateResolver
 */
open class GenericTypeAwareAutowireCandidateResolver : BeanFactoryAware, SimpleAutowireCandidateResolver() {

    private lateinit var beanFactory: BeanFactory

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

    /**
     * 检查泛型的类型
     */
    private fun checkGenericTypeMatch(bdHolder: BeanDefinitionHolder, descriptor: DependencyDescriptor): Boolean {
        val beanClass = bdHolder.beanDefinition.getBeanClass() ?: throw IllegalStateException("无法获取到BeanClass")
        val dependencyType = descriptor.getDependencyType()

        // 如果需要的是数组的话...
        if (dependencyType.isArray) {
            return if (beanClass.isArray) {
                ClassUtils.isAssignFrom(dependencyType.componentType, beanClass.componentType)
            } else {
                ClassUtils.isAssignFrom(dependencyType.componentType, beanClass)
            }

            // 如果需要的是Collection的话...那么需要匹配泛型
        } else if (ClassUtils.isAssignFrom(Collection::class.java, dependencyType)) {
            val resolvableType = descriptor.getResolvableType().asCollection()

            // 如果两种都是Collection的话，那么直接比较泛型类型是否相等！需要注意的是，不能比较继承，
            // 比如List<String>并不是List<CharSequence>的子类...
            if (ClassUtils.isAssignFrom(Collection::class.java, dependencyType)) {
                val beanResolveType = ResolvableType.forClass(beanClass).asCollection()
                return beanResolveType.getGenerics()[0].resolveType() == resolvableType.getGenerics()[0].resolveType()
            } else {
                // 如果bean的类型不是Collection，但是要解析的依赖是泛型的话，那么匹配泛型的类型和beanClass
                return ClassUtils.isAssignFrom(resolvableType.getGenerics()[0].resolve(), beanClass)
            }
        } else if (ClassUtils.isAssignFrom(Map::class.java, dependencyType)) {

        } else {
            return true
        }
        return false
    }
}