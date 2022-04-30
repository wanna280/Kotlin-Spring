package com.wanna.framework.aop.creator

import com.wanna.framework.aop.Advice
import com.wanna.framework.aop.Advisor
import com.wanna.framework.aop.Pointcut
import com.wanna.framework.aop.TargetSource
import com.wanna.framework.aop.framework.AopInfrastructureBean
import com.wanna.framework.aop.framework.ProxyFactory
import com.wanna.framework.aop.framework.ProxyProcessorSupport
import com.wanna.framework.aop.framework.autoproxy.TargetSourceCreator
import com.wanna.framework.aop.target.SingletonTargetSource
import com.wanna.framework.beans.annotations.Ordered
import com.wanna.framework.context.BeanFactory
import com.wanna.framework.context.aware.BeanFactoryAware
import com.wanna.framework.context.processor.beans.SmartInstantiationAwareBeanPostProcessor
import com.wanna.framework.core.util.ClassUtils
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个用来完成代理的BeanPostProcessor，通过Creator工厂模式去完成代理的自动生成
 */
abstract class AbstractAutoProxyCreator : SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware,
    AopInfrastructureBean, ProxyProcessorSupport() {

    companion object {
        // 不进行创建代理的FLAG常量
        @JvmField
        val DO_NOT_PROXY: Array<Any>? = null
    }

    private var beanFactory: BeanFactory? = null

    // 维护已经缓存过的Bean的列表，key-cacheKey，value=true代表产生了代理，value=false代表没有产生代理，但是已经处理过了
    private val advisedBeans: ConcurrentHashMap<Any, Boolean> = ConcurrentHashMap()

    // 已经完成代理的代理的类的类型，key-cacheKey，value-代理过后的类型
    private val proxyTypes: ConcurrentHashMap<Any, Class<*>> = ConcurrentHashMap(16)

    // 已经被TargetSource进行处理过的BeanName的列表
    private val targetSourceBeans: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    // 自定义的TargetSourceCreator
    private var customTargetSourceCreators: Array<TargetSourceCreator>? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    open fun getBeanFactory(): BeanFactory? {
        return beanFactory
    }

    /**
     * 如果我已经在实例化之前就已经缓存过beanType(proxyType)，那么我就可以预测出来该Bean的类型，
     * 如果在这之前我没有缓存过beanType，那么说明我无法预测该bean的类型
     *
     * @return 如果之前缓存过，return proxyType；不然，return null
     */
    override fun predictBeanType(beanClass: Class<*>, beanName: String): Class<*>? =
        if (proxyTypes.isEmpty()) null else proxyTypes[getCacheKey(beanClass, beanName)]

    override fun postProcessBeforeInstantiation(beanName: String, bean: Any): Any? {
        val cacheKey = getCacheKey(bean::class.java, beanName)
        val customTargetSource = getCustomTargetSource(bean::class.java, beanName)
        if (customTargetSource != null) {
            if (beanName.isNotBlank()) {
                targetSourceBeans += beanName
            }
            // 为当前Bean找到合适的Advisor列表
            val specificInterceptors = getAdvicesAndAdvisorsForBean(bean::class.java, beanName, null)
            // 创建代理对象
            val proxy = createProxy(bean::class.java, beanName, specificInterceptors!!, SingletonTargetSource(bean))

            // 缓存已经完成代理的proxyType
            proxyTypes[cacheKey] = proxy::class.java
            return proxy
        }
        return null
    }

    override fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        val cacheKey = getCacheKey(bean::class.java, beanName)
        val proxy = wrapIfNecessary(bean, beanName, cacheKey)
        return proxy
    }

    /**
     * 如果必要的话，对Bean去进行包装，使用AOP去完成动态代理；
     * 1.跳过基础设施Bean；2.跳过已经缓存的Class；3.跳过TargetSource处理过的Bean(避免被重复处理)
     */
    protected open fun wrapIfNecessary(bean: Any, beanName: String, cachedKey: Any): Any {
        // 1.跳过TargetSourceBean
        if (beanName.isBlank() && targetSourceBeans.contains(beanName)) {
            return bean
        }

        // 2.如果之前已经缓存过这个Class了，并且之前没产生代理的话，那么本次也不需要完成代理
        if (advisedBeans[cachedKey] == false) {
            return bean
        }

        // 3.如果这是一个基础设施的Bean，那么不要尝试在上面去进行代理(auto-proxy)，直接pass掉
        if (isInfrastructureClass(bean::class.java)) {
            return bean
        }

        // 为当前Bean找到合适的Advisor列表
        val specificInterceptors = getAdvicesAndAdvisorsForBean(bean::class.java, beanName, null)

        // 如果没有找到合适的Advisor，那么就不创建代理；如果找到了合适的Advisor，那么就需要去创建代理
        if (!specificInterceptors.contentEquals(DO_NOT_PROXY)) {
            advisedBeans[cachedKey] = true
            // 创建代理对象
            val proxy = createProxy(bean::class.java, beanName, specificInterceptors!!, SingletonTargetSource(bean))
            // 缓存已经处理过的代理的proxyType
            proxyTypes[cachedKey] = proxy::class.java
            return proxy
        }
        advisedBeans[cachedKey] = false
        return bean
    }

    /**
     * 创建SpringAOP代理
     */
    protected open fun createProxy(
        beanClass: Class<*>, beanName: String, specificInterceptors: Array<Any>?, targetSource: TargetSource?
    ): Any {
        val proxyFactory = ProxyFactory()
        proxyFactory.setTargetSource(targetSource)
        proxyFactory.setInterfaces(*targetSource!!.getTargetClass()!!.interfaces)
        val proxy = proxyFactory.getProxy()
        return proxy
    }

    /**
     * 判断一个类是否是基础设施的类？只要它是Advice/Advisor/Pointcut/AopInfrastructureBean的子类，
     * 那么它就会被判断为一个基础设施的Class，log：不要尝试在基础设施Bean上去创建代理！
     */
    protected open fun isInfrastructureClass(beanClass: Class<*>): Boolean {
        return ClassUtils.isAssignFrom(Advice::class.java, beanClass) || ClassUtils.isAssignFrom(
            Advisor::class.java, beanClass
        ) || ClassUtils.isAssignFrom(
            Pointcut::class.java, beanClass
        ) || ClassUtils.isAssignFrom(AopInfrastructureBean::class.java, beanClass)
    }

    /**
     * 获取CacheKey，作为Map的Key
     */
    protected open fun getCacheKey(beanClass: Class<*>, beanName: String?): Any {
        return beanClass
    }

    /**
     * 从TargetSourceCreator列表当中去，获取自定义的TargetSource，如果匹配到了合适的TargetSource，那么需要在后续完成AOP代理，
     * 去为该Bean设置TargetSource，也就是为Bean的自定义来源，可以是从ThreadLocal，Prototype等别的地方来
     */
    protected open fun getCustomTargetSource(beanClass: Class<*>, beanName: String): TargetSource? {
        val customTargetSourceCreators = this.customTargetSourceCreators
        if (customTargetSourceCreators != null && this.beanFactory != null) {
            for (tsc in customTargetSourceCreators) {
                val ts = tsc.getTargetSource(beanClass, beanName)
                if (ts != null) {
                    return ts
                }
            }
        }
        return null
    }


    /**
     * 针对于给定的Bean去获取Advices和Advisors，具体怎么获取这些Advice和Advisor？交给子类去进行实现，
     * 这个类当中仅仅去提供抽象的模板方法，并不提供具体的实现方式
     *
     * @return 如果返回空，代表不进行代理；如果返回非空，则根据返回的Advisor和Advice去产生代理
     */
    abstract fun getAdvicesAndAdvisorsForBean(
        beanClass: Class<*>, beanName: String, targetSource: TargetSource?
    ): Array<Any>?
}