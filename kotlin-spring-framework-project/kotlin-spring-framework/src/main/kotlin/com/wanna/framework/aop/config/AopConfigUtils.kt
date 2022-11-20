package com.wanna.framework.aop.config

import com.wanna.framework.aop.creator.AnnotationAwareAspectJAutoProxyCreator
import com.wanna.framework.aop.creator.AspectJAwareAdvisorAutoProxyCreator
import com.wanna.framework.aop.creator.InfrastructureAdvisorAutoProxyCreator
import com.wanna.framework.aop.support.AopUtils
import com.wanna.framework.core.Ordered
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry

/**
 * 这是一个AopConfig的工具类，可以对AopProxyCreator去进行注册
 */
object AopConfigUtils {

    /**
     * AopProxyCreator的beanName
     */
    private const val AOP_PROXY_CREATOR_BEAN_NAME = "internalAopProxyCreator"

    /**
     * 优先级别列表
     */
    private val APC_PRIORITY_LIST = ArrayList<Class<*>>()

    init {
        APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator::class.java)
        APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator::class.java)
        APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator::class.java)
    }

    /**
     * 根据clazz去获取到优先级
     *
     * @param clazz clazz
     * @return 该clazz对应的优先级
     */
    @JvmStatic
    private fun findPriorityForClass(clazz: Class<*>): Int {
        return APC_PRIORITY_LIST.indexOf(clazz)
    }

    /**
     * 根据clazzName去获取到优先级
     *
     * @param clazzName clazzName
     * @return 优先级
     */
    @JvmStatic
    private fun findPriorityForClass(clazzName: String): Int {
        for (index in 0 until APC_PRIORITY_LIST.size) {
            if (APC_PRIORITY_LIST[index].name == clazzName) {
                return index
            }
        }
        return -1
    }

    /**
     * 如果必要的话给容器中注册一个基础设施的AutoProxy的Creator
     *
     * @param registry BeanDefinitionRegistry
     */
    @JvmStatic
    fun registerAspectJAutoProxyCreatorIfNecessary(registry: BeanDefinitionRegistry): BeanDefinition? {
        return registerAspectJAutoProxyCreatorIfNecessary(registry, null)
    }

    /**
     * 如果必要的话给容器中注册一个基础设施的AutoProxy的Creator
     *
     * @param registry BeanDefinitionRegistry
     */
    @JvmStatic
    fun registerAspectJAutoProxyCreatorIfNecessary(
        registry: BeanDefinitionRegistry,
        source: Any?
    ): BeanDefinition? {
        return registerOrEscalateApcAsRequired(AspectJAwareAdvisorAutoProxyCreator::class.java, registry, source)
    }

    /**
     * 如果必要的话给容器中注册一个AspectJ的AutoProxy的Creator
     *
     * @param registry BeanDefinitionRegistry
     */
    @JvmStatic
    fun registerAutoProxyCreatorIfNecessary(registry: BeanDefinitionRegistry): BeanDefinition? {
        return registerAutoProxyCreatorIfNecessary(registry, null)
    }

    /**
     * 如果必要的话给容器中注册一个AspectJ的AutoProxy的Creator
     *
     * @param registry BeanDefinitionRegistry
     */
    @JvmStatic
    fun registerAutoProxyCreatorIfNecessary(registry: BeanDefinitionRegistry, source: Any?): BeanDefinition? {
        return registerOrEscalateApcAsRequired(InfrastructureAdvisorAutoProxyCreator::class.java, registry, source)
    }


    /**
     * 如果必要的话给容器中注册一个AspectJ的注解AutoProxy的Creator
     *
     * @param registry BeanDefinitionRegistry
     */
    @JvmStatic
    fun registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry: BeanDefinitionRegistry): BeanDefinition? {
        return registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null)
    }

    /**
     * 如果必要的话给容器中注册一个AspectJ的注解AutoProxy的Creator
     *
     * @param registry BeanDefinitionRegistry
     * @param source source
     */
    @JvmStatic
    fun registerAspectJAnnotationAutoProxyCreatorIfNecessary(
        registry: BeanDefinitionRegistry,
        source: Any?
    ): BeanDefinition? {
        return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator::class.java, registry, source)
    }

    /**
     * 强制性使用类代理(CGLIB)的方式
     *
     * @param registry BeanDefinitionRegistry
     */
    @JvmStatic
    fun forceAutoProxyCreatorToUseClassProxying(registry: BeanDefinitionRegistry) {
        if (registry.containsBeanDefinition(AOP_PROXY_CREATOR_BEAN_NAME)) {
            val beanDefinition = registry.getBeanDefinition(AOP_PROXY_CREATOR_BEAN_NAME)
            beanDefinition.getPropertyValues().addPropertyValue("proxyTargetClass", true)
        }
    }

    /**
     * 强制性地去保留代理给AopUtil
     *
     * @param registry BeanDefinitionRegistry
     */
    @JvmStatic
    fun forceAutoProxyCreatorToExposeProxy(registry: BeanDefinitionRegistry) {
        if (registry.containsBeanDefinition(AOP_PROXY_CREATOR_BEAN_NAME)) {
            val beanDefinition = registry.getBeanDefinition(AOP_PROXY_CREATOR_BEAN_NAME)
            beanDefinition.getPropertyValues().addPropertyValue("exposeProxy", true)
        }
    }

    /**
     * 如果必要的话，注册或者升级一个AopProxyCreator；
     * (1)如果之前容器当中没有AopProxyCreator的话，注册一个新的AopProxyCreator到容器当中
     * (2)如果现在要进行注册的优先级比已经有的优先级更高，那么替换掉之前的AopProxyCreator的beanClass
     *
     * @see InfrastructureAdvisorAutoProxyCreator 优先级最低
     * @see AspectJAwareAdvisorAutoProxyCreator 优先级其次
     * @see AnnotationAwareAspectJAutoProxyCreator 优先级最高
     */
    @JvmStatic
    private fun registerOrEscalateApcAsRequired(
        clazz: Class<*>,
        beanDefinitionRegistry: BeanDefinitionRegistry,
        source: Any?
    ): BeanDefinition? {
        if (beanDefinitionRegistry.containsBeanDefinition(AOP_PROXY_CREATOR_BEAN_NAME)) {
            val apcDefinition = beanDefinitionRegistry.getBeanDefinition(AOP_PROXY_CREATOR_BEAN_NAME)
            if (apcDefinition.getBeanClass() != clazz) {
                val oldPriority = findPriorityForClass(apcDefinition.getBeanClass()!!)
                val newPriority = findPriorityForClass(clazz)
                // 如果要注册的新的类的优先级更高，那么需要替换beanClass；如果要注册的新的类的优先级反而更低，就保留原来的
                if (newPriority > oldPriority) {
                    apcDefinition.setBeanClass(clazz)
                }
            }
            return null
        }

        val definition = RootBeanDefinition(clazz)
        definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
        // 设置优先级为最高的
        definition.getPropertyValues().addPropertyValue("order", Ordered.ORDER_HIGHEST)
        definition.setSource(source)

        beanDefinitionRegistry.registerBeanDefinition(AOP_PROXY_CREATOR_BEAN_NAME, definition)
        return definition
    }
}