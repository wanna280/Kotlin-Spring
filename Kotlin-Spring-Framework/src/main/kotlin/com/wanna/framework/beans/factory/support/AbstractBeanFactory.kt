package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.FactoryBean
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.beans.factory.config.Scope
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.SimpleTypeConverter
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.beans.TypeConverter
import com.wanna.framework.context.exception.BeansException
import com.wanna.framework.context.exception.NoSuckBeanDefinitionException
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.processor.beans.InstantiationAwareBeanPostProcessor
import com.wanna.framework.context.processor.beans.MergedBeanDefinitionPostProcessor
import com.wanna.framework.context.processor.beans.SmartInstantiationAwareBeanPostProcessor
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.core.util.BeanFactoryUtils
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.StringUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个抽象的BeanFactory，提供了单实例Bean的注册中心，FactoryBean的注册中心，以及ConfigurableBeanFactory/ListableBeanFactory
 *
 * @see ConfigurableBeanFactory
 * @see ListableBeanFactory
 * @see FactoryBeanRegistrySupport
 * @see DefaultSingletonBeanRegistry
 */
abstract class AbstractBeanFactory() : BeanFactory, ConfigurableBeanFactory, ListableBeanFactory,
    FactoryBeanRegistrySupport() {

    // beanClassLoader
    private var beanClassLoader: ClassLoader = ClassLoader.getSystemClassLoader()

    // ConversionService
    private var conversionService: ConversionService? = null

    // 已经完成合并的BeanDefinition的Map
    private val mergedBeanDefinitions: ConcurrentHashMap<String, RootBeanDefinition> = ConcurrentHashMap()

    // 在BeanFactory当中已经完成注册的Scope列表，处于singleton/prototype的所有Scope，都会被注册到这里
    private val scopes: MutableMap<String, Scope> = LinkedHashMap()

    // 类型转换器
    private var typeConverter: TypeConverter? = null

    // 嵌入式的值解析器列表
    private val embeddedValueResolvers: MutableList<StringValueResolver> = ArrayList()

    // BeanFactory当中需要维护的BeanPostProcessor列表
    protected val beanPostProcessors = ArrayList<BeanPostProcessor>()

    // BeanPostProcessorCache
    private var beanPostProcessorCache: BeanPostProcessorCache? = null

    // applicationStartup
    private var applicationStartup: ApplicationStartup = ApplicationStartup.DEFAULT

    override fun getBeanClassLoader() = this.beanClassLoader
    override fun setBeanClassLoader(classLoader: ClassLoader?) {
        this.beanClassLoader = classLoader ?: ClassLoader.getSystemClassLoader()
    }

    /**
     * BeanFactory也得提供获取ApplicationStartup的功能
     */
    override fun getApplicationStartup(): ApplicationStartup = this.applicationStartup
    override fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    /**
     * 这是一个BeanPostProcessorCache，对各种类型的BeanPostProcessor去进行分类，每次对BeanPostProcessor列表去进行更改(添加/删除)
     */
    class BeanPostProcessorCache {
        val instantiationAwareCache = ArrayList<InstantiationAwareBeanPostProcessor>()
        val smartInstantiationAwareCache = ArrayList<SmartInstantiationAwareBeanPostProcessor>()
        val mergedDefinitions = ArrayList<MergedBeanDefinitionPostProcessor>()

        fun hasInstantiationAware(): Boolean = instantiationAwareCache.isNotEmpty()
        fun hasSmartInstantiationAware(): Boolean = smartInstantiationAwareCache.isNotEmpty()
        fun hasMergedDefinition(): Boolean = mergedDefinitions.isNotEmpty()
    }

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

    override fun getBean(beanName: String) = doGetBean(beanName)

    private fun doGetBean(name: String): Any? {
        // 转换name成为真正的beanName，去掉FactoryBean的前缀&
        val beanName = transformedBeanName(name)

        var beanInstance: Any? = null

        // 尝试从单实例Bean的注册中心当中去获取Bean
        val sharedInstance = getSingleton(beanName)

        // 这里其实还需要判断FactoryBean
        if (sharedInstance != null) {
            return getObjectForBeanInstance(sharedInstance, name, beanName, null)
        }

        // 如果从单例Bean注册中心当中获取不到Bean实例，那么需要获取MergedBeanDefinition，去完成Bean的创建
        val mbd = getMergedBeanDefinition(beanName)

        val beanCreation = this.applicationStartup.start("spring.beans.instantiate").tag("beanName", name)

        try {
            // 如果Bean是单例的
            if (mbd.isSingleton()) {
                beanInstance = getSingleton(beanName, object : ObjectFactory<Any> {
                    override fun getObject(): Any {
                        try {
                            return createBean(beanName, mbd)!!
                        } catch (ex: Exception) {
                            throw BeansException("Create bean instance of [$beanName] failed，原因是[${ex.message}]")
                        }
                    }
                })

                // 如果Bean是Prototype的
            } else if (mbd.isPrototype()) {
                beanInstance = createBean(beanName, mbd)

                // 如果Bean是来自于自定义的Scope，那么需要从自定义的Scope当中去获取Bean
            } else {
                val scopeName = mbd.getScope()
                assert(scopeName.isNotBlank()) { "[beanName=$beanName]的BeanDefinition的scopeName不能为空" }
                val scope = this.scopes[scopeName]
                checkNotNull(scope) { "容器中没有注册过这样的Scope" }
                val scopedInstance = scope.get(beanName, object : ObjectFactory<Any> {
                    override fun getObject(): Any {
                        return createBean(beanName, mbd)
                            ?: throw BeansException("Create bean instance of [$beanName] failed")
                    }
                })
                beanInstance = getObjectForBeanInstance(scopedInstance, beanName, beanName, mbd)
            }
        } catch (ex: Exception) {
            throw ex
        } finally {
            beanCreation.end()
        }

        return beanInstance
    }

    protected open fun getObjectForBeanInstance(
        beanInstance: Any, name: String, beanName: String, mbd: RootBeanDefinition?
    ): Any {
        return beanInstance
    }

    /**
     * 转换BeanName，去掉FactoryBean的前缀&
     */
    protected open fun transformedBeanName(name: String): String = BeanFactoryUtils.transformBeanName(name)

    /**
     * 获取已经注册的ScopeName列表
     */
    override fun getRegisteredScopeNames(): Array<String> = scopes.keys.toTypedArray()

    /**
     * 根据scopeName去获取已经注册的Scope，如果该scopeName没有被注册，那么return null
     */
    override fun getRegisteredScope(name: String): Scope? = this.scopes[name]

    /**
     * 注册scope到BeanFactory当中
     */
    override fun registerScope(name: String, scope: Scope) {
        this.scopes[name] = scope
    }

    /**
     * 提供创建Bean的逻辑，交给子类去进行实现
     * @see AbstractAutowireCapableBeanFactory.createBean
     */
    protected abstract fun createBean(beanName: String, mbd: RootBeanDefinition): Any?

    @Suppress("UNCHECKED_CAST")
    override fun <T> getBean(beanName: String, type: Class<T>) = getBean(beanName) as T?

    override fun <T> getBean(type: Class<T>): T? {
        val beansForType = getBeansForType(type)
        return beansForType.values.iterator().next()
    }

    override fun isSingleton(beanName: String) = getBeanDefinition(beanName).isSingleton()
    override fun isPrototype(beanName: String) = getBeanDefinition(beanName).isPrototype()

    override fun addBeanPostProcessor(processor: BeanPostProcessor) {
        beanPostProcessors -= processor  // remove
        beanPostProcessors += processor  // add
        this.beanPostProcessorCache = null  // clear
    }

    override fun removeBeanPostProcessor(type: Class<*>) {
        beanPostProcessors.removeIf { ClassUtils.isAssignFrom(type, it::class.java) }
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
        val beanClass = beanDefinition.getBeanClass()
        return if (beanClass != null) {
            ClassUtils.isAssignFrom(type, beanClass)
        } else {
            false
        }
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
        if (beanClass != null && ClassUtils.isAssignFrom(FactoryBean::class.java, beanClass)) {
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


    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> {
        TODO("Not yet implemented")
    }

    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T> {
        TODO("Not yet implemented")
    }

    /**
     * 获取BeanDefinition，一定能获取到，如果获取不到直接抛出异常；
     * 如果想要不抛出异常，请先使用containsBeanDefinition去进行判断该BeanDefinition是否存在
     *
     * @throws NoSuckBeanDefinitionException 如果没有找到这样的BeanDefinition异常
     * @see containsBeanDefinition
     */
    abstract fun getBeanDefinition(beanName: String): BeanDefinition

    /**
     * 获取合并之后的BeanDefinition(RootBeanDefinition)
     */
    override fun getMergedBeanDefinition(beanName: String): RootBeanDefinition {
        val rootBeanDefinition = mergedBeanDefinitions[beanName]
        // 先进行一次检查，避免立刻加锁进行操作
        if (rootBeanDefinition != null) {
            return rootBeanDefinition
        }
        val beanDefinition = getBeanDefinition(beanName)
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

    override fun getTypeConverter(): TypeConverter {
        if (this.typeConverter != null) {
            return this.typeConverter!!
        }
        val typeConverter = SimpleTypeConverter()
        return typeConverter
    }

    override fun setTypeConverter(typeConverter: TypeConverter) {
        this.typeConverter = typeConverter
    }

    override fun getConversionService(): ConversionService? {
        return this.conversionService
    }

    override fun setConversionService(conversionService: ConversionService?) {
        this.conversionService = conversionService
    }

    override fun addEmbeddedValueResolver(resolver: StringValueResolver) {
        this.embeddedValueResolvers += resolver
    }

    override fun hasEmbeddedValueResolver() = embeddedValueResolvers.isNotEmpty()

    override fun resolveEmbeddedValue(strVal: String?): String? {
        if (strVal == null) {
            return null
        }
        var result: String? = null
        for (resolver in embeddedValueResolvers) {
            result = resolver.resolveStringValue(strVal)

            // 如果都解析到null了，那么肯定return null，后续肯定解析不到东西
            if (result == null) {
                return null
            }
        }
        return result
    }
}