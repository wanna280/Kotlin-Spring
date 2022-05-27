package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.*
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.FactoryBean
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.beans.factory.config.Scope
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.context.exception.BeansException
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.processor.beans.*
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.core.util.BeanFactoryUtils
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.StringUtils
import org.slf4j.LoggerFactory
import java.beans.PropertyEditor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 这是一个抽象的BeanFactory，提供了单实例Bean的注册中心，FactoryBean的注册中心，以及ConfigurableBeanFactory/ListableBeanFactory
 *
 * @see ConfigurableBeanFactory
 * @see ListableBeanFactory
 * @see FactoryBeanRegistrySupport
 * @see DefaultSingletonBeanRegistry
 */
abstract class AbstractBeanFactory(private var parentBeanFactory: BeanFactory?) : BeanFactory, ConfigurableBeanFactory,
    ListableBeanFactory, FactoryBeanRegistrySupport() {

    // beanClassLoader
    private var beanClassLoader: ClassLoader = ClassLoader.getSystemClassLoader()

    // 在BeanFactory当中已经完成注册的Scope列表，除了(except)singleton/prototype的所有Scope，都会被注册到这里
    private val scopes: MutableMap<String, Scope> = LinkedHashMap()

    // ConversionService，提供Spring BeanFactory的类型的转换
    private var conversionService: ConversionService? = DefaultConversionService.getSharedInstance()

    // 自定义的类型转换器，可以组合ConversionService和PropertyEditor去完成类型的转换
    private var typeConverter: TypeConverter? = null

    // 自定义的PropertyEditor，会被注册到TypeConverter当中去提供类型的转换...
    private val customEditors = LinkedHashMap<Class<*>, Class<out PropertyEditor>>()

    // PropertyEditor的注册器，会自动回调，去将自定义的PropertyEditor注册到TypeConverter当中去提供类型的转换...
    private val propertyEditorRegistrars = LinkedHashSet<PropertyEditorRegistrar>()

    // 存放BeanFactory当中的所有嵌入式的值解析器
    private val embeddedValueResolvers: MutableList<StringValueResolver> = ArrayList()

    // BeanFactory当中需要维护的BeanPostProcessor列表
    protected val beanPostProcessors = CopyOnWriteArrayList<BeanPostProcessor>()

    // BeanPostProcessor Cache
    private var beanPostProcessorCache: BeanPostProcessorCache? = null

    // applicationStartup，默认情况下什么都不做
    // 如果用户想要获取到Application启动当中的相关信息，只需要将ApplicationStartup替换为自定义的即可
    private var applicationStartup: ApplicationStartup = ApplicationStartup.DEFAULT

    // 已经完成合并的BeanDefinition的Map
    private val mergedBeanDefinitions: ConcurrentHashMap<String, RootBeanDefinition> = ConcurrentHashMap()

    // Logger
    private val logger = LoggerFactory.getLogger(AbstractBeanFactory::class.java)

    override fun getBeanClassLoader() = this.beanClassLoader
    override fun setBeanClassLoader(classLoader: ClassLoader?) {
        this.beanClassLoader = classLoader ?: ClassLoader.getSystemClassLoader()
    }

    /**
     * 提供获取和设置parentBeanFactory的途径
     */
    override fun getParentBeanFactory() = this.parentBeanFactory
    open fun setParentBeanFactory(parent: BeanFactory?) {
        // 如果之前设置过parentBeanFactory，现在又想设置新的parent去进行替换？那么肯定是不行...
        if (this.parentBeanFactory != null && parent != parentBeanFactory) {
            throw IllegalStateException("之前已经设置过parentBeanFactory[$parentBeanFactory]，不能设置新的parent[$parent]")
        }
        // 如果parent==this？肯定不允许发生这种情况...不然处理parent时，直接StackOverFlow...
        if (this.parentBeanFactory == this) {
            throw IllegalStateException("parentBeanFactory不能为自身")
        }
        this.parentBeanFactory = parent
    }

    /**
     * BeanFactory也得提供获取ApplicationStartup的监测功能
     */
    override fun getApplicationStartup(): ApplicationStartup = this.applicationStartup
    override fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    /**
     * 按照指定的name和type去进行getBean
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getBean(beanName: String, type: Class<T>) = doGetBean(beanName, type)

    /**
     * 按照指定的name去进行getBean，type直接使用Object就行
     */
    override fun getBean(beanName: String) = doGetBean<Any>(beanName, null)

    /**
     * doGetBean，对多种情况的getBean的方式提供模板方法的实现
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T> doGetBean(name: String, type: Class<T>?): T? {
        // 转换name成为真正的beanName，去掉FactoryBean的前缀&
        val beanName = transformedBeanName(name)

        val beanInstance: Any

        // 尝试从单实例Bean的注册中心当中去获取Bean
        val sharedInstance = getSingleton(beanName)

        // TODO: 这里其实还需要判断FactoryBean...
        if (sharedInstance != null) {
            beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, null)
        } else {
            val parentBeanFactory = this.parentBeanFactory
            // 如果有parentBeanFactory，并且当前的BeanFactory当中确实是**没有**该BeanDefinition，那么就从parent去进行寻找...
            if (parentBeanFactory != null && !containsBeanDefinition(name)) {
                return parentBeanFactory.getBean(name) as T?
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
                                return createBean(beanName, mbd)
                            } catch (ex: Exception) {
                                throw BeansException("创建Bean失败", ex, beanName)
                            }
                        }
                    })!!
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
                        }
                    })
                    beanInstance = getObjectForBeanInstance(scopedInstance, beanName, beanName, mbd)
                }
            } catch (ex: Exception) {
                throw ex
            } finally {
                beanCreation.end()
            }
        }
        return adaptBeanInstance(beanInstance, name, type)  // 使用TypeConverter去完成类型转换
    }

    /**
     * 如果必要的话，需要去进行类型的转换
     *
     * @param beanInstance beanInstance
     * @param name beanName
     * @param requiredType 需要进行转换的类型(可以为null)
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T> adaptBeanInstance(beanInstance: Any, name: String, requiredType: Class<T>?): T {
        if (requiredType != null && requiredType.isInstance(beanInstance)) {
            return getTypeConverter().convertIfNecessary(beanInstance, requiredType)!!
        }
        return beanInstance as T
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
     * 注册一个指定的Scope到BeanFactory当中
     *
     * @throws IllegalArgumentException 如果尝试去注册Singleton/Prototype
     */
    override fun registerScope(name: String, scope: Scope) {
        if (name == ConfigurableBeanFactory.SCOPE_SINGLETON || name == ConfigurableBeanFactory.SCOPE_PROTOTYPE) {
            throw IllegalArgumentException("不能去替代容器当中默认的Single/Prototype这两个Scope")
        }
        val previous = this.scopes.put(name, scope)
        if (previous != null && scope != previous) {
            if (logger.isDebugEnabled) {
                logger.debug("容器当中的Scope[scopeName=$name]发生了替换, 之前是[$previous], 现在是[$scope]")
            }
        } else {
            if (logger.isTraceEnabled) {
                logger.trace("容器当中新注册了Scope[scopeName=$name, scope=$scope]")
            }
        }
    }

    /**
     * 提供创建Bean的逻辑，调用这个方法即可完成Bean的创建工作，这是一个抽象模板方法，交给子类去进行实现
     *
     * @see AbstractAutowireCapableBeanFactory.createBean
     * @param beanName
     * @param mbd MergedBeanDefinition
     */
    protected abstract fun createBean(beanName: String, mbd: RootBeanDefinition): Any

    /**
     * 按照type去进行getBean
     */
    override fun <T> getBean(type: Class<T>): T? {
        val beansForType = getBeansForType(type)
        if (beansForType.isEmpty()) {
            throw NoSuchBeanDefinitionException("没有这样的BeanDefinition", type)
        }
        return beansForType.values.iterator().next()
    }

    /**
     * 给定一个beanName，从容器当中去获取BeanDefinition，去判断是否是单例的？
     *
     * @throws NoSuchBeanDefinitionException 如果容器当中不存在这样的BeanDefinition
     */
    override fun isSingleton(beanName: String) = getBeanDefinition(beanName).isSingleton()

    /**
     * 给定一个beanName，从容器当中去获取BeanDefinition，去判断是否是原型的？
     *
     * @throws NoSuchBeanDefinitionException 如果容器当中不存在这样的BeanDefinition
     */
    override fun isPrototype(beanName: String) = getBeanDefinition(beanName).isPrototype()


    /**
     * 判断容器当中的beanName对应的类型是否和type匹配？
     *
     * @param beanName beanName
     * @param type beanName应该匹配的类型？
     */
    override fun isTypeMatch(beanName: String, type: Class<*>): Boolean {
        val beanDefinition = getMergedBeanDefinition(beanName)
        val beanClass = beanDefinition.getBeanClass()
        /**
         * 如果有beanClass的话，那么直接使用beanClass去进行匹配
         */
        if (beanClass != null) {
            return ClassUtils.isAssignFrom(type, beanClass)
        }
        /**
         * 如果有FactoryMethod的话，那么直接使用FactoryMethod的返回值类型去进行匹配
         */
        if (beanDefinition.getFactoryMethodName() != null) {
            return ClassUtils.isAssignFrom(type, beanDefinition.getResolvedFactoryMethod()!!.returnType)
        }
        return false
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


    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, type).toList()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T> {
        val beans = HashMap<String, T>()
        val beanNamesForTypeIncludingAncestors = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, type)
        beanNamesForTypeIncludingAncestors.forEach { beans[it] = getBean(it) as T }
        return beans
    }

    /**
     * 获取BeanDefinition，一定能获取到，如果获取不到直接抛出异常；
     * 如果想要不抛出异常，请先使用containsBeanDefinition去进行判断该BeanDefinition是否存在
     *
     * @throws NoSuchBeanDefinitionException 如果没有找到这样的BeanDefinition异常
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
        // 如果确实是没有，那么必须得加锁去进行Merged了
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

    /**
     * 是否需要去注册destory的回调？
     * (1)支持AutoCloseable/DisposableBean以及BeanDefinition当中的destoryMethod等？
     * (2)遍历所有的DestructionAwareBeanPostProcessor来判断是否需要注册注册？
     *
     * @param bean bean
     * @param mbd
     */
    protected open fun requiresDestruction(bean: Any, mbd: RootBeanDefinition): Boolean {
        if (mbd.getBeanClass() == NullBean::class.java) {  // except NullBean
            return false
        }
        // 判断它是否有destory方法，如果有return true(支持AutoCloseable/DisposableBean以及BeanDefinition当中的destoryMethod等)
        if (DisposableBeanAdapter.hasDestroyMethod(bean, mbd)) {
            return true
        }
        // 判断是否有DestructionAwareBeanPostProcessor可以应用给当前的Bean，如果有的话，return true
        if (getBeanPostProcessorCache().hasDestructionAwareCache() && DisposableBeanAdapter.hasApplicableProcessors(
                bean,
                getBeanPostProcessorCache().destructionAwareCache
            )
        ) {
            return true
        }
        return false
    }

    /**
     * 如果必要的话，给Bean去注册DisposableBean的回调，根据Bean的作用域的不同，走Scope对应的注册回调的逻辑
     *
     * @param beanName beanName
     * @param bean bean
     * @param mbd MergedBeanDefinition
     */
    protected open fun registerDisposableBeanIfNecessary(beanName: String, bean: Any, mbd: RootBeanDefinition) {
        // 如果不是prototype的，并且需要去进行Destruction(destroy)，那么需要去注册Callback
        if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
            val destructionAwareCache = getBeanPostProcessorCache().destructionAwareCache
            val disposableBeanAdapter = DisposableBeanAdapter(bean, beanName, mbd, destructionAwareCache)

            // 如果它是一个单例的Bean，那么，把它注册给SingletonBeanRegistry的DisposableBean当中
            if (mbd.isSingleton()) {
                registerDisposableBean(beanName, disposableBeanAdapter)

                // 如果它是一个来自于Scope的Bean，那么需要把DisposableBean注册给Scope当中，让Scope去管理destory
            } else {
                val scope = this.scopes[mbd.getScope()]
                    ?: throw IllegalStateException("BeanFactory当中没有注册Scope[name=${mbd.getScope()}，但是BeanDefinition[name=$beanName]当中设置了该Scope]")
                scope.registerDestructionCallback(beanName, disposableBeanAdapter)
            }
        }
    }

    //-----------------------------------------------------------为Spring BeanFactory提供类型转换的支持-----------------------------------------------------

    /**
     * 获取自定义的TypeConverter
     *
     * @return 自定义的TypeConverter(如果没有去进行自定义BeanFactory的TypeConverter，那么return null)
     */
    protected open fun getCustomTypeConverter(): TypeConverter? {
        return this.typeConverter
    }

    /**
     * 获取TypeConverter
     * * 1.如果BeanFactory当中有自定义TypeConverter的话，那么使用自定义的去进行返回；
     * * 2.如果没有默认的，那么将会构建出来一个默认的SimpleTypeConverter去进行返回；
     *
     * @return SpringBeanFactory的类型转换器TypeConverter(自定义的/默认的)
     */
    override fun getTypeConverter(): TypeConverter {
        // 1.获取用户自定义的TypeConverter
        val customTypeConverter = getCustomTypeConverter()
        if (customTypeConverter != null) {
            return customTypeConverter
        }

        // 2.构建默认的TypeConverter，apply ConversionService和PropertyEditor
        val typeConverter = SimpleTypeConverter()
        typeConverter.setConversionService(this.conversionService)
        registerCustomEditors(typeConverter)
        return typeConverter
    }

    /**
     * 对BeanWrapper去完成初始化工作，为类型转换提供帮助
     *
     * @param bw 想要去进行初始化的BeanWrapper
     */
    open fun initBeanWrapper(bw: BeanWrapper) {
        // setConversionService...
        bw.setConversionService(this.conversionService)

        // registerCustomEditors...
        registerCustomEditors(bw)
    }

    /**
     * 注册BeanFactory当中配置的所有的自定义的PropertyEditor到给定的PropertyEditorRegistry当中
     *
     * @param registry 想要把PropertyEditor注册到哪个PropertyEditorRegistry当中？
     */
    protected open fun registerCustomEditors(registry: PropertyEditorRegistry) {
        // apply所有的PropertyEditor的注册器到PropertyEditorRegistry当中
        if (this.propertyEditorRegistrars.isNotEmpty()) {
            this.propertyEditorRegistrars.forEach { it.registerPropertyEditor(registry) }
        }

        // apply所有的自定义的PropertyEditor到PropertyEditorRegistry当中
        if (this.customEditors.isNotEmpty()) {
            this.customEditors.forEach { (k, v) -> registry.registerCustomEditor(k, BeanUtils.instantiateClass(v)) }
        }
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

    //-----------------------------------为Spring BeanFactory的BeanPostProcessor的分类缓存提供支持-----------------------------------------------------

    /**
     * 这是一个BeanPostProcessorCache，对各种类型的BeanPostProcessor去进行分类，每次对BeanPostProcessor列表去进行更改(添加/删除)
     * 都需要**自行**地将BeanPostProcessorCache去进行clear掉(引用设置为null，变相clear)
     */
    class BeanPostProcessorCache {
        // 干涉实例化的BeanPostProcessor(实例化之前、实例化之后、填充属性)
        val instantiationAwareCache = ArrayList<InstantiationAwareBeanPostProcessor>()

        // 干涉智能的实例化的BeanPostProcessor(获取早期类型、推断构造器、预测beanType)
        val smartInstantiationAwareCache = ArrayList<SmartInstantiationAwareBeanPostProcessor>()

        // 处理MergeBeanDefinition的PostProcessor
        val mergedDefinitions = ArrayList<MergedBeanDefinitionPostProcessor>()

        // 处理destory的BeanPostProcessor
        val destructionAwareCache = ArrayList<DestructionAwareBeanPostProcessor>()

        fun hasInstantiationAware(): Boolean = instantiationAwareCache.isNotEmpty()
        fun hasSmartInstantiationAware(): Boolean = smartInstantiationAwareCache.isNotEmpty()
        fun hasMergedDefinition(): Boolean = mergedDefinitions.isNotEmpty()
        fun hasDestructionAwareCache(): Boolean = destructionAwareCache.isNotEmpty()
    }


    /**
     * 获取BeanPostProcessor的Cache，它主要的作用是将各种类型的BeanPostProcessor去进行分类；
     * 如果之前已经构建好了，那么直接进行return即可，如果之前没有构建好？需要重新对各个类型的BeanPostProcessor去进行分类，
     * 从而构建出来一个新的BeanPostProcessorCache去进行返回；
     *
     * Note: 在每次对BeanPostProcessor的列表去进行操作时，应该reset BeanPostProcessorCache(把引用设置为null)
     *
     * @return 构建好的BeanPostProcessorCache
     */
    protected fun getBeanPostProcessorCache(): BeanPostProcessorCache {
        var beanPostProcessorCache = this.beanPostProcessorCache
        if (beanPostProcessorCache == null) {
            beanPostProcessorCache = BeanPostProcessorCache()
            beanPostProcessors.forEach {
                if (it is InstantiationAwareBeanPostProcessor) {
                    beanPostProcessorCache.instantiationAwareCache += it
                    if (it is SmartInstantiationAwareBeanPostProcessor) {
                        beanPostProcessorCache.smartInstantiationAwareCache += it
                    }
                }
                if (it is MergedBeanDefinitionPostProcessor) {
                    beanPostProcessorCache.mergedDefinitions += it
                }
                if (it is DestructionAwareBeanPostProcessor) {
                    beanPostProcessorCache.destructionAwareCache += it
                }
            }
            this.beanPostProcessorCache = beanPostProcessorCache
        }
        return beanPostProcessorCache
    }

    override fun addBeanPostProcessor(processor: BeanPostProcessor) {
        beanPostProcessors -= processor  // remove if any
        beanPostProcessors += processor  // add
        this.beanPostProcessorCache = null  // clear and reset
    }

    override fun removeBeanPostProcessor(type: Class<*>) {
        beanPostProcessors.removeIf { ClassUtils.isAssignFrom(type, it::class.java) }
        this.beanPostProcessorCache = null  // clear
    }

    override fun removeBeanPostProcessor(index: Int) {
        beanPostProcessors.removeAt(index)
        this.beanPostProcessorCache = null  // clear
    }
}