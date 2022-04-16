package com.wanna.framework.beans.factory.support

/**
 * 这是Spring中的Autowire的候选Bean的解析器，主要作用是，判断一个依赖和要进行注入的类型是否匹配
 */
interface AutowireCandidateResolver {

    /**
     * 判断一个Bean是否是符合进行注入的逻辑，默认只从BeanDefinition中进行判断
     */
    fun isAutowireCandidate(bdHolder: BeanDefinitionHolder, descriptor: DependencyDescriptor): Boolean {
        return bdHolder.beanDefinition.isAutowireCandidate()
    }

    /**
     * 判断这个依赖是否是必须进行注入的
     */
    fun isRequired(descriptor: DependencyDescriptor): Boolean {
        return descriptor.isRequired()
    }

    /**
     * 判断是否有Qualifier限定符
     */
    fun hasQualifier(descriptor: DependencyDescriptor): Boolean {
        return false
    }

    /**
     * 决定是否有一个建议去进行设置的默认值
     */
    fun getSuggestedValue(descriptor: DependencyDescriptor): Any? {
        return null
    }

    /**
     * 如果必要的话，创建一个完成解析懒加载的注入依赖的代理对象
     */
    fun getLazyResolutionProxyIfNecessary(descriptor: DependencyDescriptor, beanName: String): Any? {
        return null
    }
}