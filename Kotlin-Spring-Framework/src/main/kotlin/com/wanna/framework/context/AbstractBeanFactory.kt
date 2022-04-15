package com.wanna.framework.context

import com.wanna.framework.beans.factory.support.NullBean
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.exception.NoSuckBeanDefinitionException
import com.wanna.framework.context.exception.BeansException
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.processor.beans.InstantiationAwareBeanPostProcessor
import com.wanna.framework.context.processor.beans.MergedBeanDefinitionPostProcessor
import com.wanna.framework.context.processor.beans.SmartInstantiationAwareBeanPostProcessor
import com.wanna.framework.util.BeanFactoryUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个抽象的BeanFactory
 */
abstract class AbstractBeanFactory() : BeanFactory, ConfigurableBeanFactory, ListableBeanFactory,
    FactoryBeanRegistrySupport() {

    // beanClassLoader
    private var beanClassLoader: ClassLoader = ClassLoader.getSystemClassLoader()

    // 已经完成合并的BeanDefinition的Map
    private val mergedBeanDefinitions: ConcurrentHashMap<String, RootBeanDefinition> = ConcurrentHashMap()

    override fun getBeanClassLoader(): ClassLoader {
        return this.beanClassLoader
    }

    override fun setBeanClassLoader(classLoader: ClassLoader?) {
        this.beanClassLoader = if (classLoader == null) ClassLoader.getSystemClassLoader() else classLoader
    }

    class BeanPostProcessorCache {
        val instantiationAwareCache = ArrayList<InstantiationAwareBeanPostProcessor>()
        val smartInstantiationAwareCache = ArrayList<SmartInstantiationAwareBeanPostProcessor>()
        val mergedDefinitions = ArrayList<MergedBeanDefinitionPostProcessor>()

        fun hasInstantiationAware(): Boolean {
            return instantiationAwareCache.isEmpty()
        }

        fun hasSmartInstantiationAware(): Boolean {
            return smartInstantiationAwareCache.isEmpty()
        }

        fun hasMergedDefinition(): Boolean {
            return mergedDefinitions.isEmpty()
        }
    }

    // BeanPostProcessorCache
    private var beanPostProcessorCache: BeanPostProcessorCache? = null

    /**
     * 获取BeanPostProcessor的Cache
     */
    fun getBeanPostProcessorCache(): BeanPostProcessorCache {
        if (this.beanPostProcessorCache == null) {
            this.beanPostProcessorCache = BeanPostProcessorCache()
            beanPostProcessors.forEach {
                if (it is InstantiationAwareBeanPostProcessor) {
                    this.beanPostProcessorCache!!.instantiationAwareCache += it
                    if (it is SmartInstantiationAwareBeanPostProcessor) {
                        this.beanPostProcessorCache!!.smartInstantiationAwareCache += it
                    }
                }
                if (it is MergedBeanDefinitionPostProcessor) {
                    this.beanPostProcessorCache!!.mergedDefinitions += it
                }
            }
        }
        return beanPostProcessorCache!!
    }

    // BeanPostProcessor列表
    protected val beanPostProcessors = ArrayList<BeanPostProcessor>()

    override fun getBean(beanName: String): Any? {
        return doGetBean(beanName)
    }

    private fun doGetBean(beanName: String): Any? {
        var singleton = getSingleton(beanName, true)

        // 这里其实还需要判断FactoryBean，这里暂时不处理
        if (singleton != null) {
            return singleton
        }

        val beanDefinition = getBeanDefinition(beanName)
        if (beanDefinition == null) {
            throw NoSuckBeanDefinitionException("The bean definition of [$beanName] can't be find")
        }

        singleton = getSingleton(beanName, object : ObjectFactory<Any> {
            override fun getObject(): Any {
                return createBean(beanName, beanDefinition)
                    ?: throw BeansException("Create bean instance of [$beanName] failed")
            }
        })

        return singleton
    }

    /**
     * 提供创建Bean的逻辑，交给子类去进行实现
     */
    protected abstract fun createBean(beanName: String, bd: BeanDefinition): Any?

    override fun <T> getBean(beanName: String, type: Class<T>): T? {
        return getBean(beanName) as T?
    }

    override fun <T> getBean(type: Class<T>): T? {
        val beansForType = getBeansForType(type)
        return beansForType.values.iterator().next()
    }

    override fun isSingleton(beanName: String): Boolean {
        val beanDefinition = getBeanDefinition(beanName)
        return if (beanDefinition != null) beanDefinition.isSingleton() else throw BeansException()
    }

    override fun isPrototype(beanName: String): Boolean {
        val beanDefinition = getBeanDefinition(beanName)
        return if (beanDefinition != null) beanDefinition.isPrototype() else throw BeansException()
    }

    override fun addBeanPostProcessor(processor: BeanPostProcessor) {
        beanPostProcessors -= processor
        beanPostProcessors += processor
        this.beanPostProcessorCache = null  // clear
    }

    override fun removeBeanPostProcessor(type: Class<*>) {
        beanPostProcessors.removeIf { ClassUtils.isAssginFrom(type, it::class.java) }
        this.beanPostProcessorCache = null  // clear
    }

    override fun removeBeanPostProcessor(index: Int) {
        beanPostProcessors.removeAt(index)
        this.beanPostProcessorCache = null  // clear
    }

    /**
     * 判断一个bean是否是FactoryBean？
     */
    override fun isFactoryBean(beanName: String): Boolean {
        return false
    }

    override fun isTypeMatch(beanName: String, type: Class<*>): Boolean {
        val beanDefinition = getBeanDefinition(beanName)
        return if (beanDefinition != null) ClassUtils.isAssginFrom(
            type, beanDefinition.getBeanClass()!!
        ) else throw BeansException()
    }

    /**
     * 根据beanName获取到该Bean在容器中的类型
     */
    override fun getType(beanName: String): Class<*>? {
        // 1.从SingletonBean中去进行获取Bean的类型
        val beanInstance = getSingleton(beanName, false)
        if (beanInstance != null && beanInstance != NullBean::class.java) {
            // 如果获取到的是单例Bean是一个FactoryBean，但是beanName没有&，那么就说明需要返回FactoryBean所导入的Object的类型
            if (beanInstance is FactoryBean<*> && !BeanFactoryUtils.isFactoryDereference(beanName)) {
                return getTypeForFactoryBean(beanInstance)
            } else {
                return beanInstance::class.java
            }
        }

        // 获取到MergedBeanDefinition
        val mbd = getMergedBeanDefinition(beanName)

        val beanClass = mbd.getBeanClass()
        if (beanClass != null && ClassUtils.isAssginFrom(FactoryBean::class.java, beanClass)) {
            // 如果从名字来看，它不是一个FactoryBean，那么就获取到FactoryBean包装的Object的类型
            if (!BeanFactoryUtils.isFactoryDereference(beanName)) {
                return getTypeForFactoryBean(beanName, mbd, true)
            } else {
                return beanClass
            }
        } else {
            return null
        }
    }

    /**
     * 获取一个FactoryBean的类型，如果是单例的并且允许去进行初始化，才会尝试去进行解析
     *
     * @param allowInit 是否允许进行初始化
     * @param beanName beanName
     * @param mbd 合并的RootBeanDefinition
     */
    protected open fun getTypeForFactoryBean(beanName: String, mbd: RootBeanDefinition, allowInit: Boolean): Class<*>? {
        if (allowInit && mbd.isSingleton()) {
            val factoryBean =
                getBean(BeanFactory.FACTORY_BEAN_PREFIX + beanName, FactoryBean::class.java) as FactoryBean
            return getTypeForFactoryBean(factoryBean)
        }
        return null
    }

    override fun getBeanNamesForType(type: Class<*>): List<String> {
        return ArrayList(getBeansForType(type).keys)
    }

    override fun <T> getBeansForType(type: Class<T>): Map<String, T> {
        val beans = HashMap<String, T>()
        getBeanDefinitionNames().forEach { beanName ->
            if (isTypeMatch(beanName, type)) {
                beans[beanName] = getBean(beanName) as T
            }
        }
        return beans
    }

    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> {
        TODO("Not yet implemented")
    }

    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T> {
        TODO("Not yet implemented")
    }

    /**
     * 获取最原始的注册进容器当中的BeanDefinition
     */
    abstract fun getBeanDefinition(beanName: String): BeanDefinition?

    /**
     * 获取合并之后的BeanDefinition(RootBeanDefinition)
     */
    protected open fun getMergedBeanDefinition(beanName: String): RootBeanDefinition {
        val rootBeanDefinition = mergedBeanDefinitions[beanName]
        // 先进行一次检查，避免立刻加锁进行操作
        if (rootBeanDefinition != null) {
            return rootBeanDefinition
        }
        val beanDefinition = getBeanDefinition(beanName)
            ?: throw NoSuckBeanDefinitionException("没有找到这样的BeanDefinition-->[beanName=$beanName]")
        return getMergedBeanDefinition(beanName, beanDefinition)
    }

    /**
     * 返回合并之后的BeanDefinition(RootBeanDefinition)
     */
    protected open fun getMergedBeanDefinition(beanName: String, beanDefinition: BeanDefinition): RootBeanDefinition {
        synchronized(mergedBeanDefinitions) {
            var mbd = mergedBeanDefinitions[beanName]  // 从缓存中拿

            // 如果缓存中没有，那么需要完成RootBeanDefinition构建
            if (mbd == null) {
                mbd = RootBeanDefinition(beanDefinition)

                // 如果scope为默认的，那么修改为Singleton
                if (!StringUtils.hasText(mbd.getScope())) {
                    mbd.setScope(BeanDefinition.SCOPE_SINGLETON)
                }

                // 将合并完成的BeanDefinition放入到缓存当中
                mergedBeanDefinitions[beanName] = mbd
            }
            return mbd
        }
    }
}