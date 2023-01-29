package com.wanna.framework.context.annotation

import com.wanna.framework.aop.TargetSource
import com.wanna.framework.aop.framework.ProxyFactory
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException
import com.wanna.framework.beans.factory.support.AutowireCandidateResolver
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.beans.factory.support.DependencyDescriptor

/**
 * 这是一个新增了支持处理Context相关的注解的AutowireCandidateResolver, 比如Lazy注解的AutowireCandidateResolver;
 * 在它的父类QualifierAnnotationAutowireCandidateResolver当中支持了@Qualifier/@Value等注解的处理;
 * 在Spring的注解版IOC容器当中, ContextAnnotationAutowireCandidateResolver将会作为beanFactory的默认的AutowireCandidateResolver
 *
 * @see QualifierAnnotationAutowireCandidateResolver
 * @see AutowireCandidateResolver
 */
open class ContextAnnotationAutowireCandidateResolver : QualifierAnnotationAutowireCandidateResolver() {

    companion object {
        @JvmField
        val INSTANCE = ContextAnnotationAutowireCandidateResolver() // shared instance
    }

    /**
     * 如果必要的话, 构建懒解析的代理对象(如果依赖描述符当中有@Lazy注解, 那么需要产生代理对象)
     *
     * @param descriptor 依赖描述符
     * @param beanName beanName
     * @return 构建好的代理对象(如果不是Lazy的, 那么return null)
     */
    override fun getLazyResolutionProxyIfNecessary(descriptor: DependencyDescriptor, beanName: String?): Any? {
        return if (isLazy(descriptor)) buildLazyResolutionProxy(descriptor, beanName) else null
    }

    /**
     * 该依赖是否是懒加载的?
     *
     * @param descriptor 依赖描述符
     * @return 如果有@Lazy注解并且value设置为true, return true; 没有则return false
     */
    protected open fun isLazy(descriptor: DependencyDescriptor): Boolean {
        // 1.检查依赖描述符(字段/方法参数)上的@Lazy注解
        val lazy = descriptor.getAnnotation(com.wanna.framework.context.annotation.Lazy::class.java)
        if (lazy != null && lazy.value) {
            return true
        }

        return false
    }

    /**
     * 构建懒加载解析的代理, 通过TargetSource代理一层, 在运行时获取对象时, 使用从容器当中doResolveDependency的方式;
     * 可以支持多个Bean的情况去进行解析, 因为使用的是BeanFactory.doResolveDependency去进行解析, 而不是使用的getBean的方式去进行获取;
     * 因此@Lazy支持标注在Set/List/Collection/Map类型的字段上
     *
     * @param descriptor 依赖描述符
     * @param beanName 请求注入方的beanName, A想要注入B, 那么beanName=A, dependencyDescriptor=B
     * @return 使用ProxyFactory去构建懒加载代理
     */
    protected open fun buildLazyResolutionProxy(descriptor: DependencyDescriptor, beanName: String?): Any? {
        val beanFactory = getBeanFactory()

        // 如果不是DefaultListableBeanFactory...
        if (beanFactory !is DefaultListableBeanFactory) {
            throw IllegalStateException("BeanFactory只支持[${DefaultListableBeanFactory::class.java.name}]")
        }

        // 构建TargetSource, 设置getTarget获取到的对象是从BeanFactory当中去进行解析到的
        val targetSource = object : TargetSource {
            override fun getTarget(): Any? {
                val type = getTargetClass()
                val autowiredNames = if (beanName == null) null else LinkedHashSet<String>(1)
                // Note: 这里应该调用的是, doResolveDependency方法, 而不是直接去调用resolveDependency方法
                // 不然又会找@Lazy注解, 又创建一层代理, 继续执行方法...继续创建代理, 产生StackOverflow的情况
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
                    throw NoSuchBeanDefinitionException("没有在BeanFactory当中找到合适的Bean", null, beanName, type)
                }
                return target
            }

            override fun releaseTarget(target: Any?) {}
            override fun getTargetClass() = descriptor.getDependencyType()
            override fun isStatic(): Boolean = false
        }

        // proxyFactory
        val proxyFactory = ProxyFactory()
        proxyFactory.setTargetSource(targetSource)
        val dependencyType = descriptor.getDependencyType()
        if (dependencyType.isInterface) {
            proxyFactory.addInterface(dependencyType)
        }

        // createProxy
        return proxyFactory.getProxy(beanFactory.getBeanClassLoader())

    }
}