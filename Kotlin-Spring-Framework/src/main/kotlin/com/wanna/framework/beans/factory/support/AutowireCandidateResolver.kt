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
     * 判断这个依赖是否是必须进行注入的，根据DependencyDescriptor去进行决定
     */
    fun isRequired(descriptor: DependencyDescriptor): Boolean {
        return descriptor.isRequired()
    }

    /**
     * 判断是否有Qualifier限定符，不仅包括Spring自家的@Qualifier注解，也包括Javax.inject当中的Qualifier注解
     *
     * @param descriptor 依赖描述符
     * @return 是否有Qualifier？
     */
    fun hasQualifier(descriptor: DependencyDescriptor): Boolean {
        return false
    }

    /**
     * 决定是否有一个建议去进行设置的默认值，用来处理字段或者方法参数上标注的@Value注解
     *
     * @param descriptor 要去进行注入的依赖描述符
     * @return 是否有默认的建议值？如果没有return null，有建议返回值的默认值则返回
     */
    fun getSuggestedValue(descriptor: DependencyDescriptor): Any? {
        return null
    }

    /**
     * 如果必要的话，创建一个完成解析懒加载的注入依赖的代理对象(@Autowired时，可以使用@Lazy注解去进行懒加载就是这个原理)；
     * 可以自行去构建一个TargetSource，去完成真实对象的注入，每次获取对象时，都能在TargetSource当中去获取真正的对象来执行目标方法
     *
     * @param descriptor 依赖描述符
     * @param beanName beanName
     * @return 构建好的代理对象
     */
    fun getLazyResolutionProxyIfNecessary(descriptor: DependencyDescriptor, beanName: String?): Any? {
        return null
    }
}