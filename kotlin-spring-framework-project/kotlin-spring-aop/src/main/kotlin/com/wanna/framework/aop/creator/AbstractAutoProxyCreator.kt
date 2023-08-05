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
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.BeanFactoryAware
import  com.wanna.framework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor
import com.wanna.framework.util.ClassUtils
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个用来完成SpringAOP代理的BeanPostProcessor, 通过Creator工厂模式去完成代理的自动生成
 *
 * @see AbstractAdvisorAutoProxyCreator
 */
abstract class AbstractAutoProxyCreator : SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware,
    AopInfrastructureBean, ProxyProcessorSupport() {
    companion object {
        // 不进行创建代理的FLAG常量
        @JvmField
        val DO_NOT_PROXY: Array<Any> = emptyArray()
    }

    private lateinit var beanFactory: BeanFactory

    // 维护已经缓存过的Bean的列表, key-cacheKey, value=true代表产生了代理, value=false代表没有产生代理, 但是已经处理过了
    private val advisedBeans: ConcurrentHashMap<Any, Boolean> = ConcurrentHashMap()

    // 已经完成代理的代理的类的类型, key-cacheKey, value-代理过后的类型
    private val proxyTypes: ConcurrentHashMap<Any, Class<*>> = ConcurrentHashMap(16)

    // 已经被TargetSource进行处理过的BeanName的列表
    private val targetSourceBeans: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    // 自定义的TargetSourceCreator
    private var customTargetSourceCreators: Array<TargetSourceCreator>? = null

    // 早期引用的Bean列表
    private var earlyReferences = ConcurrentHashMap<Any, Any>()

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    open fun getBeanFactory(): BeanFactory? {
        return beanFactory
    }

    /**
     * 如果我已经在实例化之前就已经缓存过beanType(proxyType), 那么我就可以预测出来该Bean的类型,
     * 如果在这之前我没有缓存过beanType, 那么说明我无法预测该bean的类型
     *
     * @return 如果之前缓存过, return proxyType; 不然, return null
     */
    override fun predictBeanType(beanClass: Class<*>, beanName: String): Class<*>? =
        if (proxyTypes.isEmpty()) null else proxyTypes[getCacheKey(beanClass, beanName)]

    /**
     * 获取早期引用, 如果必要的话, 提前生成代理对象
     *
     * @param bean bean
     * @param beanName beanName
     * @return 如果必要的话, 返回代理对象; 不然, 返回Bean
     */
    override fun getEarlyReference(bean: Any, beanName: String): Any {
        val cacheKey = getCacheKey(bean::class.java, beanName)
        this.earlyReferences[cacheKey] = bean
        return wrapIfNecessary(bean, beanName, cacheKey)
    }

    override fun postProcessBeforeInstantiation(beanName: String, beanClass: Class<*>): Any? {
        val cacheKey = getCacheKey(beanClass, beanName)

        // 尝试去获取自定义的TargetSource, 如果针对该Bean获取到了合适的TargetSource的话, 那么需要创建代理
        val customTargetSource = getCustomTargetSource(beanClass, beanName)
        if (customTargetSource != null) {
            if (beanName.isNotBlank()) {
                targetSourceBeans += beanName
            }
            // 为当前Bean找到合适的Advisor列表
            val specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, null)
            if (specificInterceptors !== DO_NOT_PROXY) {
                // 创建代理对象
                val proxy = createProxy(beanClass, beanName, specificInterceptors, customTargetSource)

                // 缓存已经完成代理的proxyType
                proxyTypes[cacheKey] = proxy::class.java
                return proxy
            }
        }
        return null
    }

    override fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        val cacheKey = getCacheKey(bean::class.java, beanName)
        // 如果这个Bean, 没有被引用过的话, 那么需要去检查是否生成代理
        if (earlyReferences.remove(cacheKey) != bean) {
            return wrapIfNecessary(bean, beanName, cacheKey)
        }
        return bean
    }

    /**
     * 如果必要的话, 对Bean去进行包装, 使用AOP去完成动态代理;
     * 1.跳过基础设施Bean; 2.跳过已经缓存的Class; 3.跳过TargetSource处理过的Bean(避免被重复处理)
     */
    protected open fun wrapIfNecessary(bean: Any, beanName: String, cachedKey: Any): Any {
        // 1.跳过TargetSourceBean
        if (beanName.isBlank() && targetSourceBeans.contains(beanName)) {
            return bean
        }

        // 2.如果之前已经缓存过这个Class了, 并且之前没产生代理的话, 那么本次也不需要完成代理
        if (advisedBeans[cachedKey] == false) {
            return bean
        }

        // 3.如果这是一个基础设施的Bean, 那么不要尝试在上面去进行代理(auto-proxy), 直接pass掉
        if (isInfrastructureClass(bean::class.java)) {
            return bean
        }

        // 为当前Bean找到应该去进行apply的所有的合适的Advisor列表(交给子类去进行实现)
        val specificInterceptors = getAdvicesAndAdvisorsForBean(bean::class.java, beanName, null)

        // 如果没有找到合适的Advisor, 那么就不创建代理; 如果找到了合适的Advisor, 那么就需要去创建代理
        if (specificInterceptors !== DO_NOT_PROXY) {
            advisedBeans[cachedKey] = true
            // 根据给定Advisor列表去创建Aop代理对象
            val proxy = createProxy(bean::class.java, beanName, specificInterceptors, SingletonTargetSource(bean))
            // 缓存已经处理过的代理的proxyType
            proxyTypes[cachedKey] = proxy::class.java
            return proxy
        }
        advisedBeans[cachedKey] = false
        return bean
    }

    /**
     * 根据给定的beanClass/specificInterceptors/targetSource, 去创建SpringAOP代理对象
     *
     * @param beanClass beanClass
     * @param beanName beanName
     * @param specificInterceptors 候选的Advisor列表
     * @param targetSource targetSource
     * @return 创建完成代理之后的Bean
     */
    protected open fun createProxy(
        beanClass: Class<*>, beanName: String, specificInterceptors: Array<Any>?, targetSource: TargetSource
    ): Any {
        val proxyFactory = ProxyFactory()
        // fixed: Copy Aop属性到ProxyFactory当中
        proxyFactory.copyFrom(this)

        // fixed: 构建Advisor列表, 设置到ProxyFactory当中
        proxyFactory.addAdvisors(buildAdvisors(beanName, specificInterceptors))
        // 设置TargetSource
        proxyFactory.setTargetSource(targetSource)
        // 设置代理对象应该拥有的接口列表...
        proxyFactory.setInterfaces(*ClassUtils.getAllInterfacesForClassAsSet(beanClass).toTypedArray())

        // add: 交给子类去自定义ProxyFactory
        customizeProxyFactory(proxyFactory)
        return proxyFactory.getProxy(getProxyClassLoader())
    }

    /**
     * 判断一个类是否是基础设施的类? 只要它是Advice/Advisor/Pointcut/AopInfrastructureBean的子类,
     * 那么它就会被判断为一个基础设施的Class, log：不要尝试在基础设施Bean上去创建代理！
     *
     * @param beanClass beanClass
     */
    protected open fun isInfrastructureClass(beanClass: Class<*>): Boolean {
        return ClassUtils.isAssignFrom(Advice::class.java, beanClass)
                || ClassUtils.isAssignFrom(Advisor::class.java, beanClass)
                || ClassUtils.isAssignFrom(Pointcut::class.java, beanClass)
                || ClassUtils.isAssignFrom(AopInfrastructureBean::class.java, beanClass)
    }

    /**
     * 获取CacheKey, 作为Map的Key
     */
    protected open fun getCacheKey(beanClass: Class<*>, beanName: String?): Any {
        return beanClass
    }

    /**
     * 从TargetSourceCreator列表当中去, 获取自定义的TargetSource, 如果匹配到了合适的TargetSource, 那么需要在后续完成AOP代理,
     * 去为该Bean设置TargetSource, 也就是为Bean的自定义来源, 可以是从ThreadLocal, Prototype等别的地方来
     *
     * @param beanClass beanClass
     * @param beanName beanName
     * @return 如果有合适的TargetSource, 那么return TargetSource; 不然return null
     */
    protected open fun getCustomTargetSource(beanClass: Class<*>, beanName: String): TargetSource? {
        val customTargetSourceCreators = this.customTargetSourceCreators
        if (customTargetSourceCreators != null) {
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
     * 根据给定的specificInterceptors, 去构建Advisor列表
     *
     * @param specificInterceptors 候选的Advisor列表
     * @return 构建好的Advisor列表
     */
    protected open fun buildAdvisors(beanName: String?, specificInterceptors: Array<Any>?): Array<Advisor> {
        val result = ArrayList<Advisor>()
        specificInterceptors?.forEach {
            if (it is Advisor) {
                result += it
            }
        }
        return result.toTypedArray()
    }

    /**
     * 自定义ProxyFactory的逻辑(模板方法, 交给子类去进行实现)
     *
     * @param proxyFactory ProxyFactory
     */
    protected open fun customizeProxyFactory(proxyFactory: ProxyFactory) {

    }


    /**
     * 针对于给定的Bean去获取Advices和Advisors, 具体怎么获取这些Advice和Advisor? 交给子类去进行实现,
     * 这个类当中仅仅去提供抽象的模板方法, 并不提供具体的实现方式
     *
     * @return 如果返回空, 代表不进行代理; 如果返回非空, 则根据返回的Advisor和Advice去产生代理
     */
    abstract fun getAdvicesAndAdvisorsForBean(
        beanClass: Class<*>, beanName: String, targetSource: TargetSource?
    ): Array<Any>
}