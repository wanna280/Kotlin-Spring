package com.wanna.framework.beans.factory.support

import com.wanna.framework.aop.TargetSource
import com.wanna.framework.aop.framework.ProxyFactory

/**
 * 这是一个支持处理Context相关的注解，比如Lazy注解的AutowireCandidateResolver
 */
open class ContextAnnotationAutowireCandidateResolver : QualifierAnnotationAutowireCandidateResolver() {

    companion object {
        @JvmField
        val INSTANCE = ContextAnnotationAutowireCandidateResolver()
    }

    /**
     * 如果必要的话，构建懒解析的代理对象(如果依赖描述符当中有@Lazy注解，那么需要产生代理对象)
     *
     * @param descriptor 依赖描述符
     * @param beanName beanName
     * @return 构建好的代理对象
     */
    override fun getLazyResolutionProxyIfNecessary(descriptor: DependencyDescriptor, beanName: String?): Any? {
        return if (isLazy(descriptor)) buildLazyResolutionProxy(descriptor, beanName) else null
    }

    /**
     * 该依赖是否是懒加载的？
     *
     * @return 如果有@Lazy注解并且value设置为true，return true；没有则return false
     */
    protected open fun isLazy(descriptor: DependencyDescriptor): Boolean {
        // 1.检查依赖描述符(字段/方法参数)上的@Lazy注解
        val lazy = descriptor.getAnnotation(com.wanna.framework.context.annotation.Lazy::class.java)
        if (lazy != null && lazy.value) {
            return true
        }

        return false
    }

    protected open fun buildLazyResolutionProxy(descriptor: DependencyDescriptor, beanName: String?): Any? {
        val beanFactory = getBeanFactory()
        if (beanFactory !is DefaultListableBeanFactory) {
            throw IllegalStateException("BeanFactory只支持[${DefaultListableBeanFactory::class.java.name}]")
        }

        /**
         * 构建TargetSource，设置getTarget获取到的对象是从BeanFactory当中去进行解析到的
         */
        val targetSource = object : TargetSource {
            override fun getTargetClass(): Class<*>? {
                return descriptor.getDependencyType()
            }

            override fun isStatic(): Boolean {
                return false
            }

            override fun getTarget(): Any? {
                val type = getTargetClass()
                val autowiredNames = if (beanName == null) null else LinkedHashSet<String>(1)
                val target =
                    beanFactory.doResolveDependency(descriptor, beanName, autowiredNames, null)
                if (target == null) {
                    if (type == Map::class.java) {
                        return emptyMap<Any?, Any?>()
                    } else if (type == List::class.java) {
                        return emptyList<Any?>()
                    } else if (type == Set::class.java || type == Collection::class.java) {
                        return emptySet<Any?>()
                    }
                }
                return target
            }

            override fun releaseTarget(target: Any?) {

            }
        }
        val proxyFactory = ProxyFactory()
        proxyFactory.setTargetSource(targetSource)
        val dependencyType = descriptor.getDependencyType()
        if (dependencyType.isInterface) {
            proxyFactory.addInterface(dependencyType)
        }
        return proxyFactory.getProxy(beanFactory.getBeanClassLoader())

    }
}