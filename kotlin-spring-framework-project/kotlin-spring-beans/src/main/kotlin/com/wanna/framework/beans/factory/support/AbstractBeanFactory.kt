package com.wanna.framework.beans.factory.support

import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.beans.*
import com.wanna.framework.beans.factory.*
import com.wanna.framework.beans.factory.BeanFactory.Companion.FACTORY_BEAN_PREFIX
import com.wanna.framework.beans.factory.config.*
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.core.NamedThreadLocal
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import java.beans.PropertyEditor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 这是一个抽象的BeanFactory, 提供了单实例Bean的注册中心, FactoryBean的注册中心, 以及ConfigurableBeanFactory/ListableBeanFactory
 *
 * @see ConfigurableBeanFactory
 * @see ListableBeanFactory
 * @see FactoryBeanRegistrySupport
 * @see DefaultSingletonBeanRegistry
 */
abstract class AbstractBeanFactory(private var parentBeanFactory: BeanFactory? = null) : BeanFactory,
    ConfigurableBeanFactory, ListableBeanFactory, FactoryBeanRegistrySupport() {

    /**
     * beanClassLoader, 提供对于BeanClass的类加载功能
     */
    private var beanClassLoader: ClassLoader = ClassUtils.getDefaultClassLoader()

    /**
     * 在BeanFactory当中已经完成注册的Scope列表, 除了(except)singleton/prototype的所有Scope, 都会被注册到这里
     */
    private val scopes = LinkedHashMap<String, Scope>()

    /**
     * ConversionService, 提供Spring BeanFactory的类型的转换
     */
    private var conversionService: ConversionService? = DefaultConversionService.getSharedInstance()

    /**
     * 自定义的类型转换器, 可以组合ConversionService和PropertyEditor去完成类型的转换
     */
    @Nullable
    private var typeConverter: TypeConverter? = null

    /**
     * 自定义的PropertyEditor, 会被注册到TypeConverter当中去提供类型的转换...
     */
    private val customEditors = LinkedHashMap<Class<*>, Class<out PropertyEditor>>()

    /**
     * PropertyEditor的注册器, 会自动回调, 去将自定义的PropertyEditor注册到TypeConverter当中去提供类型的转换...
     *
     * @see typeConverter
     */
    private val propertyEditorRegistrars = LinkedHashSet<PropertyEditorRegistrar>()

    /**
     * 存放BeanFactory当中的所有嵌入式的值解析器, 提供对于占位符的解析等工作
     */
    private val embeddedValueResolvers = ArrayList<StringValueResolver>()

    /**
     * BeanFactory当中需要维护的BeanPostProcessor列表, 它们有资格去对Bean去进行自定义的干涉
     */
    protected val beanPostProcessors = CopyOnWriteArrayList<BeanPostProcessor>()

    /**
     * BeanPostProcessor Cache
     */
    private var beanPostProcessorCache: BeanPostProcessorCache? = null

    /**
     * applicationStartup, 默认情况下什么都不做
     * 如果用户想要获取到Application启动当中的相关信息, 只需要将ApplicationStartup替换为自定义的即可
     */
    private var applicationStartup: ApplicationStartup = ApplicationStartup.DEFAULT

    /**
     * 已经完成合并的BeanDefinition的缓存, 对所有的BeanDefinition, 最终都会被Merge成为一个RootBeanDefinition
     */
    private val mergedBeanDefinitions = ConcurrentHashMap<String, RootBeanDefinition>()

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(AbstractBeanFactory::class.java)

    /**
     * 当前正在创建当中的Bean, 用来排查原型Bean的注入的情况
     */
    private val prototypesCurrentlyInCreation = NamedThreadLocal<Any>("Prototype Beans Current In Creation")

    override fun getBeanClassLoader() = this.beanClassLoader

    override fun setBeanClassLoader(@Nullable classLoader: ClassLoader?) {
        this.beanClassLoader = classLoader ?: ClassUtils.getDefaultClassLoader()
    }

    /**
     * 获取parentBeanFactory
     *
     * @return parent BeanFactory(如果不存在的话, 那么return null)
     */
    override fun getParentBeanFactory() = this.parentBeanFactory

    /**
     * 设置parent BeanFactory
     *
     * @param parentBeanFactory parentBeanFactory
     */
    override fun setParentBeanFactory(parentBeanFactory: BeanFactory?) {
        // 如果之前设置过parentBeanFactory，现在又想设置新的parent去进行替换？那么肯定是不行...
        if (this.parentBeanFactory != null && this.parentBeanFactory != parentBeanFactory) {
            throw IllegalStateException("之前已经设置过parentBeanFactory[$parentBeanFactory], 不能设置新的parent[$parentBeanFactory]")
        }
        // 如果parent==this? 肯定不允许发生这种情况...不然处理parent时, 直接StackOverflow...
        if (this.parentBeanFactory == this) {
            throw IllegalStateException("parentBeanFactory不能为自身")
        }
        this.parentBeanFactory = parentBeanFactory
    }

    /**
     * BeanFactory也得提供获取ApplicationStartup的监测功能
     *
     * @return ApplicationStartup
     */
    override fun getApplicationStartup(): ApplicationStartup = this.applicationStartup

    /**
     * 自定义ApplicationStartup
     *
     * @param applicationStartup 你想要使用的ApplicationStartup
     */
    override fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    /**
     * 按照指定的name和type去进行getBean
     *
     * @param beanName beanName
     * @param type beanType
     * @param T beanType
     * @return 获取到的Bean对象
     */
    @Throws(NoSuchBeanDefinitionException::class)
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getBean(beanName: String, type: Class<T>) = doGetBean(beanName, type, null)

    /**
     * 按照指定的name去进行getBean, type直接使用Object就行
     *
     * @param beanName beanName
     * @return 获取到的Bean对象
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun getBean(beanName: String) = doGetBean<Any>(beanName, null, null)

    /**
     * 按照指定的name去进行getBean, 并指定参数列表
     *
     * @param beanName beanName
     * @param args 获取Bean时需要用到的更多参数列表
     * @return 获取到的Bean对象
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun getBean(beanName: String, vararg args: Any?) = doGetBean<Any>(beanName, null, arrayOf(*args))

    /**
     * doGetBean, 对多种情况的getBean的方式提供模板方法的实现
     *
     * @param name beanName
     * @param requiredType requiredType
     * @param args 更多参数列表
     * @param T beanType
     * @return 获取到的Bean对象
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T : Any> doGetBean(name: String, requiredType: Class<T>?, args: Array<Any?>?): T {
        // 转换name成为真正的beanName, 去掉FactoryBean的前缀"&"去进行解引用
        val beanName = transformedBeanName(name)

        var beanInstance: Any

        // 尝试从单实例Bean的注册中心当中去获取Bean
        val sharedInstance = getSingleton(beanName)
        // 如果获取到了SingletonBean的话, 那么需要尝试去获取FactoryBeanObject(如果是FactoryBean的话)
        if (sharedInstance != null) {
            if (logger.isTraceEnabled) {
                if (isSingletonCurrentlyInCreation(beanName)) {
                    logger.trace("提前去获取了早期实例的引用[beanName=$beanName], 它并未完成彻底的初始化工作, 因为发生了循环依赖")
                } else {
                    logger.trace("从缓存当中获取单实例Bean[beanName=$beanName]")
                }
            }
            beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, null)
        } else {
            // 快速地去检查当前原型Bean是否已经正在创建当中了? 只要已经在创建当中了, 那么我们就可以认为已经发生了循环依赖了
            // 但是对于原型Bean的循环依赖, 无法解决, 因此我们在这里直接抛出BeanCurrentlyInCreationException异常...
            if (isPrototypeCurrentlyInCreation(beanName)) {
                throw BeanCurrentlyInCreationException("原型Bean[$beanName]当前正在创建当中", null, beanName)
            }

            val parentBeanFactory = this.parentBeanFactory
            // 如果有parentBeanFactory, 并且当前的BeanFactory当中确实是**没有**该BeanDefinition, 那么就从parent去进行寻找...
            if (parentBeanFactory != null && !containsBeanDefinition(name)) {
                return parentBeanFactory.getBean(name) as T
            }

            // 如果从单例Bean注册中心当中获取不到Bean实例, 那么需要获取MergedBeanDefinition, 去完成Bean的创建
            val mbd = getMergedLocalBeanDefinition(beanName)

            val beanCreation = this.applicationStartup.start("spring.beans.instantiate").tag("beanName", name)
            try {
                // tag requiredType
                if (requiredType != null) {
                    beanCreation.tag("requiredType", requiredType::class.java.toString())
                }

                // 将它依赖的Bean先去进行实例化工作...
                val dependsOn = mbd.getDependsOn()
                dependsOn.forEach { getBean(it) }

                // 如果Bean是单例(Singleton)的
                if (mbd.isSingleton()) {
                    beanInstance = getSingleton(beanName, ObjectFactory {
                        try {
                            return@ObjectFactory createBean(beanName, mbd, args)
                        } catch (ex: Exception) {
                            destroySingleton(beanName)  // destroyBean
                            throw ex   // rethrow
                        }
                    })!!

                    // fixed: getObjectForBeanInstance
                    beanInstance = getObjectForBeanInstance(beanInstance, name, beanName, mbd)
                    // 如果Bean是Prototype的
                } else if (mbd.isPrototype()) {
                    val prototypeInstance: Any
                    beforePrototypeCreation(beanName)
                    try {
                        prototypeInstance = createBean(beanName, mbd, args)
                    } finally {
                        afterPrototypeCreation(beanName)
                    }
                    beanInstance = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd)

                    // 如果Bean是来自于自定义的Scope, 那么需要从自定义的Scope当中去获取Bean
                } else {
                    val scopeName = mbd.getScope()
                    assert(scopeName.isNotBlank()) { "[beanName=$beanName]的BeanDefinition的scopeName不能为空" }
                    val scope = this.scopes[scopeName]
                    checkNotNull(scope) { "容器中没有注册过这样的Scope" }

                    // 从自定义的Scope内获取Bean
                    val scopedInstance = scope.get(beanName, object : ObjectFactory<Any> {
                        override fun getObject(): Any {
                            beforePrototypeCreation(beanName)
                            try {
                                return createBean(beanName, mbd, args)
                            } finally {
                                afterPrototypeCreation(beanName)
                            }
                        }
                    })
                    beanInstance = getObjectForBeanInstance(scopedInstance, name, beanName, mbd)
                }
            } catch (ex: BeansException) {
                beanCreation.tag("exception", ex::class.java.toString())
                beanCreation.tag("message", ex.message ?: "")
                throw ex
            } finally {
                beanCreation.end()  // end
            }
        }

        // 如果必要的话, 应该去使用TypeConverter去完成类型转换, 让它和返回的目标类型匹配
        return adaptBeanInstance(beanInstance, name, requiredType)
    }

    /**
     * 如果必要的话, 需要去进行类型的转换, 保证返回的对象类型是目标类型(requiredType)匹配的
     *
     * @param beanInstance beanInstance
     * @param name beanName
     * @param requiredType 需要进行转换的类型(可以为null)
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T : Any> adaptBeanInstance(beanInstance: Any, name: String, requiredType: Class<T>?): T {

        // 如果requiredType和beanInstance的类型本身不匹配, 那么就需要使用到TypeConverter去进行转换
        if (requiredType != null && !requiredType.isInstance(beanInstance)) {
            try {
                return getTypeConverter().convertIfNecessary(beanInstance, requiredType) ?: throw IllegalStateException(
                    "beanName=[$name]的Bean去转换成为[${ClassUtils.getQualifiedName(requiredType)}]的转换结果为空"
                )
            } catch (ex: TypeMismatchException) {
                if (logger.isDebugEnabled) {
                    logger.debug("无法去将beanName=[$name]的Bean去转换成为[${ClassUtils.getQualifiedName(requiredType)}]")
                }
                throw BeanNotOfRequiredTypeException(name, requiredType, beanInstance::class.java)
            }
        }

        // 类型匹配的话, 直接强转返回即可
        return beanInstance as T
    }

    /**
     * 在原型Bean创建之前应该执行的操作, 把它加入到当前正在执行的原型Bean的列表当中
     *
     * @param beanName 要标记为当前正在创建当中的原型Bean的beanName
     */
    protected open fun beforePrototypeCreation(beanName: String) {
        val current = prototypesCurrentlyInCreation.get()
        // 如果current==null, 说明之前没有放入过, 直接设置beanName
        if (current == null) {
            prototypesCurrentlyInCreation.set(beanName)

            // 如果current is String, 说明之前已经设置过了, 那么需要使用HashSet去添加beanName
        } else if (current is String) {
            val beanNameSet = HashSet<String>(2)
            beanNameSet += current
            beanNameSet += beanName
            prototypesCurrentlyInCreation.set(beanNameSet)

            // 如果当前是HashSet, 说明之前已经放入过两个以上的元素了, 这里直接往HashSet当中添加即可
        } else {
            @Suppress("UNCHECKED_CAST") val beanNameSet = current as HashSet<String>
            beanNameSet += beanName
        }
    }

    /**
     * 在原型Bean创建之后, 应该执行的操作, 把它从正在执行的原型Bean当中移除掉
     *
     * @param beanName 想要去进行移除的原型Bean的beanName
     */
    protected open fun afterPrototypeCreation(beanName: String) {
        val current = prototypesCurrentlyInCreation.get()
        // 如果之前是String, 直接remove
        if (current is String) {
            prototypesCurrentlyInCreation.remove()

            // 如果之前是HashSet, 那么需要移除一个元素
        } else {
            @Suppress("UNCHECKED_CAST") val beanNameSet = current as HashSet<String>
            beanNameSet -= beanName
            // 如果最后一个元素都被删掉了, 那么直接remove...
            if (beanNameSet.isEmpty()) {
                prototypesCurrentlyInCreation.remove()
            }
        }
    }

    /**
     * 当前原型Bean是否正在创建当中?
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun isPrototypeCurrentlyInCreation(beanName: String): Boolean {
        val current = prototypesCurrentlyInCreation.get()
        return current != null && (current == beanName || (current is Set<*> && (current as Set<String>).contains(
            beanName
        )))
    }

    /**
     * 摧毁一个在具体的[Scope]内的Bean
     *
     * @param beanName beanName
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中没有这样的BeanDefinition
     * @throws IllegalArgumentException 如果给定的Bean的Scope为Singleton/Prototype
     * @throws IllegalStateException 如果该BeanDefinition的Scope并未注册到BeanFactory当中
     */
    override fun destroyScopedBean(beanName: String) {
        val mbd = getMergedLocalBeanDefinition(beanName)
        if (mbd.isSingleton() || mbd.isPrototype()) {
            throw IllegalArgumentException("给定的beanName=[$beanName]并不在一个具体的Scope内, 可能是Singleton/Prototype的")
        }
        val scopeName = mbd.getScope()
        val scope =
            scopes[scopeName] ?: throw IllegalStateException("在BeanFactory当中并未注册有[$scopeName]这样的Scope")
        val bean = scope.remove(beanName) ?: return

        // 摧毁Scope内获取到的Bean
        destroyBean(beanName, bean, mbd)
    }

    /**
     * 摧毁一个Bean
     *
     * @param beanName beanName
     * @param bean bean
     */
    override fun destroyBean(beanName: String, bean: Any) {
        destroyBean(beanName, bean, getMergedLocalBeanDefinition(beanName))
    }

    /**
     * 摧毁一个实例Bean(通常是用于Prototype的Bean的摧毁), 回调它的destroy方法
     *
     * @param beanName beanName
     * @param bean bean
     * @param mbd MergedBeanDefinition
     */
    protected open fun destroyBean(beanName: String, bean: Any, mbd: RootBeanDefinition) {
        DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessorCache().destructionAwareCache).destroy()
    }

    /**
     * 如果必要的话, 返回FactoryBean导入的FactoryBeanObject; 如果是普通的Bean, 直接return;
     *
     * @param beanName beanName
     * @param name 原始的beanName(带有前缀"&")
     * @param mbd MergedBeanDefinition
     */
    protected open fun getObjectForBeanInstance(
        beanInstance: Any, name: String, beanName: String, mbd: RootBeanDefinition?
    ): Any {
        // 如果给定的name确实是"&"开头, 那么说明想要返回的是真正的FactoryBean...
        if (BeanFactoryUtils.isFactoryDereference(name)) {
            if (beanInstance is NullBean) {
                return beanInstance
            }
            if (beanInstance !is FactoryBean<*>) {
                throw IllegalStateException("name=[$name]以'&'作为前缀, 但是它的类型并不匹配FactoryBean")
            }
            mbd?.setFactoryBean(true)  // setFactoryBean to true
            return beanInstance
        }

        // 如果给定的name不是以"&"开头的话, 说明它想获取到的是FactoryBeanObject(或者正常非FactoryBean的普通Bean)
        if (beanInstance !is FactoryBean<*>) {  // 正常的Bean, 直接return
            return beanInstance
        }
        mbd?.setFactoryBean(true)  // setFactoryBean to true

        // 先尝试直接从FactoryBeanObject缓存去进行获取, 如果缓存无法获取的话, 那么使用FactoryBean.getObject去进行获取...
        return getCachedFactoryBeanForObject(name) ?: getObjectFromFactoryBean(beanInstance, beanName, true)
    }

    /**
     * 转换BeanName, 去掉FactoryBean的前缀&
     */
    protected open fun transformedBeanName(name: String): String = BeanFactoryUtils.transformBeanName(name)

    /**
     * 获取已经注册的ScopeName列表
     */
    override fun getRegisteredScopeNames(): Array<String> = scopes.keys.toTypedArray()

    /**
     * 根据scopeName去获取已经注册的Scope, 如果该scopeName没有被注册, 那么return null
     */
    override fun getRegisteredScope(name: String): Scope? = this.scopes[name]

    /**
     * 注册一个指定的Scope到BeanFactory当中
     *
     * @param name scopeName
     * @param scope 要去进行注册的Scope
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
     * 提供创建Bean的逻辑, 调用这个方法即可完成Bean的创建工作, 这是一个抽象模板方法, 交给子类去进行实现
     *
     * @see AbstractAutowireCapableBeanFactory.createBean
     * @param beanName
     * @param mbd MergedBeanDefinition
     */
    protected abstract fun createBean(beanName: String, mbd: RootBeanDefinition, args: Array<Any?>?): Any

    /**
     * 按照type去进行getBean
     */
    override fun <T : Any> getBean(type: Class<T>): T {
        val result = getBeanNamesForType(type).map { getBean(it, type) }
        if (result.isEmpty()) {
            throw NoSuchBeanDefinitionException(
                "BeanFactory当中没有type=[${ClassUtils.getQualifiedName(type)}]的BeanDefinition", null, null, type
            )
        }
        return result.iterator().next()
    }

    /**
     * 检查当前BeanFactory当中是否包含有这样的beanName的Bean?
     *
     * @param name name
     * @return 如果BeanFactory当中包含有这样的beanName的话Bean的话, 那么return true; 如果不包含的话, 那么return false
     */
    override fun containsBean(name: String): Boolean {
        return containsBeanDefinition(name)
    }

    /**
     * 给定一个beanName, 从容器当中去获取BeanDefinition, 去判断是否是单例的?
     *
     * @throws NoSuchBeanDefinitionException 如果容器当中不存在这样的BeanDefinition
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun isSingleton(beanName: String) = getBeanDefinition(beanName).isSingleton()

    /**
     * 给定一个beanName, 从容器当中去获取BeanDefinition, 去判断是否是原型的?
     *
     * @throws NoSuchBeanDefinitionException 如果容器当中不存在这样的BeanDefinition
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun isPrototype(beanName: String) = getBeanDefinition(beanName).isPrototype()


    /**
     * 判断容器当中的beanName对应的类型是否和type匹配? (支持去匹配FactoryBeanObject)
     *
     * @param name beanName
     * @param type beanName应该匹配的类型?
     * @return 是否类型匹配?
     */
    override fun isTypeMatch(name: String, type: Class<*>): Boolean {
        return isTypeMatch(name, ResolvableType.forClass(type))
    }

    /**
     * 判断容器当中的beanName对应的类型是否和type匹配? (支持去匹配FactoryBeanObject)
     *
     * @param name beanName
     * @param type beanName应该匹配的类型?
     * @return 是否类型匹配?
     */
    override fun isTypeMatch(name: String, type: ResolvableType): Boolean {
        return isTypeMatch(name, type, false)
    }

    /**
     * 判断容器当中的beanName对应的类型是否和type匹配? (支持去匹配FactoryBeanObject)
     *
     * 这个方法实现巨复杂(目前并未实现完全), 应该研究研究！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
     *
     * @param name beanName
     * @param type beanName应该匹配的类型?
     * @param allowFactoryBeanInit 是否允许FactoryBean去进行初始化?
     * @return 是否类型匹配?
     */
    @Deprecated(message = "use new api to replace")
    protected open fun isTypeMatch(name: String, type: Class<*>, allowFactoryBeanInit: Boolean): Boolean {
        val beanName = transformedBeanName(name)
        val isFactoryDereference = BeanFactoryUtils.isFactoryDereference(name)

        // 1, 先尝试去检验一波已经有SingletonBean的情况, 这种情况下, 肯定是可以直接根据类型去进行匹配
        val singleton = getSingleton(beanName, false)
        if (singleton != null && singleton::class.java != NullBean::class.java) {
            // 如果获取到的SingletonBean是FactoryBean的话, 那么需要判断你是不是想要FactoryBean来进行决定匹配什么类型
            return if (singleton is FactoryBean<*>) {
                // 如果给的name没有以"&"开头, 说明你需要FactoryBeanObject, 就需要需要匹配FactoryBeanObject
                if (!isFactoryDereference) {
                    val typeForFactoryBean = getTypeForFactoryBean(singleton)
                    ClassUtils.isAssignFrom(type, typeForFactoryBean)

                    // 如果给的name含有"&", 那么说明你想要的是FactoryBean, 而不是FactoryBeanObject, 直接匹配FactoryBean的类型
                } else {
                    type.isInstance(singleton)
                }
                // 如果Singleton根本就不是FactoryBean的话, 那么直接匹配类型就行...
            } else {
                type.isInstance(singleton)
            }
        }

        // 2.如果当前BeanFactory当中连BeanDefinition都没有, 那么尝试去parent当中去找...
        if (!containsBeanDefinition(beanName) && getParentBeanFactory() is ConfigurableBeanFactory) {
            return getParentBeanFactory()!!.isTypeMatch(name, type)
        }

        // 3.如果从SingletonBean当中都无法获取到的话, 那么也只能从BeanDefinition当中去进行判断了...
        val beanDefinition = getMergedLocalBeanDefinition(beanName)
        val beanClass = beanDefinition.getBeanClass()

        // 如果有beanClass的话, 那么直接使用beanClass去进行匹配
        if (beanClass != null) {
            if (isFactoryBean(beanName, beanDefinition)) {
                // 如果是FactoryBean, 那么有可能需要匹配FactoryBean/FactoryBeanObject
                // 1.如果name以"&"开头, 那么需要的是FactoryBean, 直接使用isAssignFrom去进行匹配就行
                // 2.如果name没有以"&"开头, 那么需要的是FactoryBeanObject(有可能需要提前去完成FactoryBean的实例化)
                if (!isFactoryDereference) {
                    val factoryBeanType =
                        getTypeForFactoryBean(FACTORY_BEAN_PREFIX + beanName, beanDefinition, allowFactoryBeanInit)
                    return ClassUtils.isAssignFrom(type, factoryBeanType)
                } else {
                    return ClassUtils.isAssignFrom(type, beanClass)
                }
            } else {
                return ClassUtils.isAssignFrom(type, beanClass)
            }
        }

        // 如果有FactoryMethod的话, 那么直接使用FactoryMethod的返回值类型去进行匹配
        if (beanDefinition.getFactoryMethodName() != null && beanDefinition.getResolvedFactoryMethod() != null) {
            return ClassUtils.isAssignFrom(type, beanDefinition.getResolvedFactoryMethod()?.returnType)
        }

        val beanClassName = beanDefinition.getBeanClassName()
        if (beanClassName != null) {
            return ClassUtils.isAssignFrom(type, beanClassLoader.loadClass(beanClassName))
        }

        return false
    }

    /**
     * 检查给定的name对应的Bean, 是否和预期的type匹配得上?
     *
     * @param name name
     * @param typeToMatch 预期beanType
     * @param allowEagerInit 是否允许eagerInit?
     * @return 给定的name对应的Bean的类型, 是否和预期的type匹配得上?
     */
    protected open fun isTypeMatch(
        name: String,
        typeToMatch: ResolvableType,
        allowEagerInit: Boolean
    ): Boolean {
        // 转换beanName(如果有&的话, 那么去掉&)
        val beanName = transformedBeanName(name)
        // 根据name是否以&作为开头, 去检查是否是FactoryBean引用
        val factoryDereference = BeanFactoryUtils.isFactoryDereference(name)

        // 1.检查已经注册到缓存当中的Bean实例对象
        val beanInstance = getSingleton(beanName, false)
        if (beanInstance != null && beanInstance != NullBean::class.java) {

            // 如果拿到的beanInstance本身就是FactoryBean的话...
            if (beanInstance is FactoryBean<*>) {
                // 如果name是以"&"作为开头, 那么说明应该参与匹配的就是FactoryBean, 也就是beanInstance
                if (factoryDereference) {
                    return typeToMatch.isInstance(beanInstance)

                    // 如果name不是以"&"作为开头, 那么说明应该匹配的是FactoryBeanObject
                } else {
                    val typeForFactoryBean = getTypeForFactoryBean(beanInstance)
                    return typeForFactoryBean != null && typeToMatch.isAssignableFrom(typeForFactoryBean)
                }

                // 如果beanInstance不是FactoryBean的话
            } else {
                // 如果不是FactoryBean, 但是name以"&"作为开头
                // 例如根据"&aaa"去获取到了一个非FactoryBean的这种情况
                // 在这种情况下, 一律认为是不匹配
                if (factoryDereference) {
                    return false

                    // 如果beanInstance不是FactoryBean, name也不是以"&"作为开头, 也就是最正常的情况下
                } else {
                    // 最开始的情况下, 先根据beanInstance直接去进行匹配...
                    if (typeToMatch.isInstance(beanInstance)) {
                        return true
                    }
                }
            }

            // 如果包含了beanName的单例对象, 但是不包含这样的BeanDefinition
        } else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            // null instance registered
            return false
        }

        // 2.如果确定当前BeanFactory当中不包含这样的BeanDefinition, 那么交给parentBeanFactory去进行检查
        val parentBeanFactory = getParentBeanFactory()
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            return parentBeanFactory.isTypeMatch(name, typeToMatch);
        }

        // 3.根据BeanInstance无法check成功, 那么尝试去检查BeanDefinition
        val mbd = getMergedLocalBeanDefinition(beanName)

        // 要去进行匹配的类型
        val classToMatch = typeToMatch.resolve() ?: FactoryBean::class.java

        // 要去进行匹配的类型列表, 如果classToMatch==FactoryBean.class, 那么只匹配FactoryBean;
        // 如果classToMatch!=FactoryBean.class, 那么需要同时匹配FactoryBean和classToMatch
        val typesToMatch: Array<Class<*>> =
            if (classToMatch == FactoryBean::class.java) arrayOf(classToMatch)
            else arrayOf(FactoryBean::class.java, classToMatch)


        // 预测得到的beanType
        var predictedType: Class<*>? = null

        // TODO check decorated BeanDefinition

        if (predictedType == null) {

            // 预测beanType
            predictedType = predictBeanType(beanName, mbd, *typesToMatch)

            // predictedType==null, 说明预测不到合适的beanType的话, 那么直接return false
            if (predictedType == null) {
                return false;
            }
        }

        if (ClassUtils.isAssignable(FactoryBean::class.java, predictedType)) {
            // 如果根据beanName去拿到的BeanDefinition的类型是FactoryBean
            // 但是预期的并不是FactoryBean, 我们需要获取到FactoryBeanObject的类型...
            if (beanInstance == null && !factoryDereference) {
                predictedType = getTypeForFactoryBean(beanName, mbd, allowEagerInit)

                // 如果FactoryBeanObject类型无法获取到, 那么return false
                if (predictedType == null) {
                    return false
                }
            }
        } else if (factoryDereference) {
            predictedType = predictBeanType(beanName, mbd, FactoryBean::class.java)
            if (predictedType == null || !ClassUtils.isAssignable(FactoryBean::class.java, predictedType)) {
                return false
            }
        }

        // TODO check generics

        // 检查predictedType和预期type是否能匹配成功?
        return typeToMatch.isAssignableFrom(predictedType)
    }

    /**
     * 从MergedBeanDefinition当中去判断, 它是否是一个FactoryBean?
     *
     * @param name name
     * @param mbd MergedBeanDefinition
     */
    protected open fun isFactoryBean(name: String, mbd: RootBeanDefinition): Boolean {
        var result = mbd.isFactoryBean()  // 从缓存当中先去进行判断...
        // 缓存当中没有, 就得去预测一些BeanType再去进行匹配...
        if (result == null) {
            // 预测beanType时, 我们只去进行FactoryBean类型的预测...
            val beanClass = predictBeanType(name, mbd, FactoryBean::class.java)
            result = beanClass != null && ClassUtils.isAssignable(FactoryBean::class.java, beanClass)
            mbd.setFactoryBean(result)
        }
        return result
    }

    /**
     * 根据BeanDefinition, 去解析出来合适的beanClass
     *
     * @param mbd BeanDefinition
     * @param beanName beanName
     * @param typeToMatch 要去进行匹配的类型
     * @return 从BeanDefinition当中去解析到的beanClass
     */
    @Nullable
    protected open fun resolveBeanClass(
        mbd: RootBeanDefinition, beanName: String, vararg typeToMatch: Class<*>
    ): Class<*>? {
        try {
            // 1.如果已经存在有beanClass, 那么直接return beanClass
            if (mbd.hasBeanClass()) {
                return mbd.getBeanClass()!!
            }
            // 2.如果没有beanClass的话, 那么尝试去进行解析, 使用classLoader去进行类加载
            return doResolveBeanClass(mbd, typeToMatch = typeToMatch)
        } catch (ex: Throwable) {
            throw CannotLoadBeanClassException("Cannot load bean class", ex)
        }
    }

    /**
     * 执行真正的beanClass的解析
     *
     * @param mbd BeanDefinition
     * @return 解析到的beanClass(没有解析到return null)
     */
    @Nullable
    private fun doResolveBeanClass(mbd: RootBeanDefinition, vararg typeToMatch: Class<*>): Class<*>? {
        val beanClassName = mbd.getBeanClassName()
        val beanClassLoader = getBeanClassLoader()
        return mbd.resolveBeanClass(beanClassLoader)
    }


    /**
     * 根据beanName获取到该Bean在SpringBeanFactory中的类型
     *
     * @param beanName beanName
     * @return beanType
     */
    override fun getType(beanName: String): Class<*>? {
        // 1.从SingletonBean中去进行获取Bean的类型
        val beanInstance = getSingleton(beanName, false)
        if (beanInstance != null && beanInstance != NullBean::class.java) {
            // 如果获取到的是单例Bean是一个FactoryBean, 但是beanName没有"&", 那么就说明需要返回FactoryBean所导入的Object的类型
            if (beanInstance is FactoryBean<*> && !BeanFactoryUtils.isFactoryDereference(beanName)) {
                return getTypeForFactoryBean(beanInstance)
            } else {
                return beanInstance::class.java
            }
        }

        // 获取到MergedBeanDefinition
        val mbd = getMergedLocalBeanDefinition(beanName)

        val beanClass = predictBeanType(beanName, mbd)
        if (beanClass != null && ClassUtils.isAssignFrom(FactoryBean::class.java, beanClass)) {
            // 如果从名字来看, 它不是一个FactoryBean, 那么就获取到FactoryBean包装的Object的类型
            if (!BeanFactoryUtils.isFactoryDereference(beanName)) {
                return getTypeForFactoryBean(beanName, mbd, true)
            } else {
                return beanClass
            }
        } else {
            return beanClass  // fixed: use beanClass to return
        }
    }

    /**
     * 根据给定的Merged BeanDefinition, 去决定得到合适的beanType
     * (当前类的实现方式, 只支持去对已经存在有beanClassName, 但是没有解析成为beanClass的情况去进行解析;
     * 对于factoryMethod, 也就是@Bean方法的情况, 当前类的实现方式不支持去进行解析, 详情交给子类去进行实现)
     *
     * @param beanName beanName
     * @param mbd Merged BeanDefinition
     * @param typeToMatch typeToMatch
     * @return 预测得到的beanType
     */
    @Nullable
    protected open fun predictBeanType(
        beanName: String,
        mbd: RootBeanDefinition,
        vararg typeToMatch: Class<*>
    ): Class<*>? {
        // 如果MergedBeanDefinition已经存在有beanClass的话, 那么直接返回beanClass即可...
        if (mbd.hasBeanClass()) {
            return mbd.getBeanClass()
        }

        // 如果有factoryMethodName的话, 说明该Bean是@Bean方法导入的, 这里beanClass返回null
        // 子类会去进行重写这个方法, 进行另外的beanClass的寻找
        if (mbd.getFactoryMethodName() != null) {
            return null
        }

        // 如果有beanClassName, 并且没有factoryMethodName, 那么走这里去进行解析, 进行类加载...
        return resolveBeanClass(mbd, beanName, *typeToMatch)
    }

    /**
     * 根据给定的BeanDefinition, 去进行获取一个FactoryBean的类型
     *
     * @param allowInit 是否允许进行初始化
     * @param beanName beanName
     * @param mbd 合并的RootBeanDefinition
     */
    protected open fun getTypeForFactoryBean(beanName: String, mbd: RootBeanDefinition, allowInit: Boolean): Class<*>? {

        // 1.尝试从BeanDefinitionAttribute当中去进行解析
        val type = mbd.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE) as Class<*>?
        if (type != null) {
            return type;
        }

        // 2.如果允许去进行初始化, 那么走到这里去进行类型的匹配
        if (allowInit && mbd.isSingleton()) {
            val factoryBean = getBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean::class.java)
            return getTypeForFactoryBean(factoryBean)
        }
        return null
    }

    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> =
        getBeanNamesForTypeIncludingAncestors(type, true, true)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T> {
        val beans = HashMap<String, T>()
        val beanNamesForTypeIncludingAncestors = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, type)
        beanNamesForTypeIncludingAncestors.forEach { beans[it] = getBean(it) as T }
        return beans
    }

    override fun getBeanNamesForTypeIncludingAncestors(
        type: Class<*>, includeNonSingletons: Boolean, allowEagerInit: Boolean
    ): List<String> {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, type, includeNonSingletons, allowEagerInit)
            .toList()
    }

    /**
     * 获取BeanDefinition, 一定能获取到, 如果获取不到直接抛出异常;
     * 如果想要不抛出异常, 请先使用containsBeanDefinition去进行判断该BeanDefinition是否存在
     *
     * @throws NoSuchBeanDefinitionException 如果没有找到这样的BeanDefinition异常
     * @see containsBeanDefinition
     */
    abstract fun getBeanDefinition(beanName: String): BeanDefinition

    /**
     * 获取合并之后的BeanDefinition(RootBeanDefinition), 支持从parentBeanFactory当中去进行递归搜索
     *
     * @param name 要去进行合并的beanName
     * @return 完成合并的BeanDefinition(一般为RootBeanDefinition)
     */
    override fun getMergedBeanDefinition(name: String): BeanDefinition {
        val beanName = transformedBeanName(name)
        // 如果当前的BeanFactory当中没有包含的话, 那么直接尝试去parent去进行检查(fast check)
        if (!containsBeanDefinition(name) && getParentBeanFactory() is ConfigurableBeanFactory) {
            return (getParentBeanFactory()!! as ConfigurableBeanFactory).getMergedBeanDefinition(beanName)
        }
        // 如果当前BeanFactory当中有包含的话, 那么从当前的BeanFactory当中去进行寻找
        return getMergedLocalBeanDefinition(beanName)
    }

    /**
     * 获取**当前BeanFactory**当中完成合并的BeanDefinition, 不会从parent当中去进行搜索
     *
     * @param beanName 要去进行合并的BeanDefinition的beanName
     * @return 完成合并的RootBeanDefinition
     */
    protected open fun getMergedLocalBeanDefinition(beanName: String): RootBeanDefinition {
        val rootBeanDefinition = mergedBeanDefinitions[beanName]
        // 先进行一次检查(fast check), 避免立刻加锁进行操作
        // 检查该BeanDefinition是否已经过期/是否还没完成Merge?
        if (rootBeanDefinition != null && !rootBeanDefinition.stale) {
            return rootBeanDefinition
        }
        // 如果确实是没有(或者已经失效), 那么必须得加锁(slow check)去进行Merged了
        return getMergedBeanDefinition(beanName, getBeanDefinition(beanName))
    }

    /**
     * clear掉一个MergedBeanDefinition, 将RootBeanDefinition1的stale标志位设置为true,
     * 下次需要获取MergedBeanDefinition时, 需要重写去进行Merge, 不能直接从缓存当中去进行获取
     *
     * @param beanName beanName
     */
    protected open fun clearMergedBeanDefinition(beanName: String) {
        this.mergedBeanDefinitions[beanName]?.stale = true
    }


    /**
     * 返回合并之后的BeanDefinition(RootBeanDefinition)
     *
     * @param beanName beanName
     * @param beanDefinition originBeanDefinition(待去进行Merge的BeanDefinition)
     * @return 合并之后的RootBeanDefinition
     */
    protected open fun getMergedBeanDefinition(beanName: String, beanDefinition: BeanDefinition): RootBeanDefinition {
        synchronized(mergedBeanDefinitions) {
            var mbd = mergedBeanDefinitions[beanName]  // 从缓存中拿

            // 如果缓存中没有(或者之前Merged的已经过期了), 那么需要完成RootBeanDefinition构建
            if (mbd == null || mbd.stale) {
                mbd = RootBeanDefinition(beanDefinition)

                // 如果scope为默认的, 那么修改为Singleton
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

            // 如果都解析到null了, 那么肯定return null, 后续肯定解析不到东西
            if (result == null) {
                return null
            }
        }
        return result
    }

    /**
     * 是否需要去注册destroy的回调?
     * (1)支持AutoCloseable/DisposableBean以及BeanDefinition当中的destroyMethod等?
     * (2)遍历所有的DestructionAwareBeanPostProcessor来判断是否需要注册注册?
     *
     * @param bean bean
     * @param mbd
     */
    protected open fun requiresDestruction(bean: Any, mbd: RootBeanDefinition): Boolean {
        if (mbd.getBeanClass() == NullBean::class.java) {  // except NullBean
            return false
        }
        // 判断它是否有destroy方法, 如果有return true(支持AutoCloseable/DisposableBean以及BeanDefinition当中的destroyMethod等)
        if (DisposableBeanAdapter.hasDestroyMethod(bean, mbd)) {
            return true
        }
        // 判断是否有DestructionAwareBeanPostProcessor可以应用给当前的Bean, 如果有的话, return true
        if (getBeanPostProcessorCache().hasDestructionAwareCache() && DisposableBeanAdapter.hasApplicableProcessors(
                bean, getBeanPostProcessorCache().destructionAwareCache
            )
        ) {
            return true
        }
        return false
    }

    override fun setCurrentlyInCreation(beanName: String, inCreation: Boolean) {
        super.setCurrentlyInCreation(beanName, inCreation)
    }

    override fun isCurrentlyInCreation(beanName: String): Boolean {
        return super.isCurrentlyInCreation(beanName)
    }

    /**
     * 如果必要的话, 给Bean去注册DisposableBean的回调, 根据Bean的作用域的不同, 走Scope对应的注册回调的逻辑
     *
     * @param beanName beanName
     * @param bean bean
     * @param mbd MergedBeanDefinition
     */
    protected open fun registerDisposableBeanIfNecessary(beanName: String, bean: Any, mbd: RootBeanDefinition) {
        // 如果不是prototype的, 并且需要去进行Destruction(destroy), 那么需要去注册Callback
        if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
            val destructionAwareCache = getBeanPostProcessorCache().destructionAwareCache
            val disposableBeanAdapter = DisposableBeanAdapter(bean, beanName, mbd, destructionAwareCache)

            // 如果它是一个单例的Bean, 那么, 把它注册给SingletonBeanRegistry的DisposableBean当中
            if (mbd.isSingleton()) {
                registerDisposableBean(beanName, disposableBeanAdapter)

                // 如果它是一个来自于Scope的Bean, 那么需要把DisposableBean注册给Scope当中, 让Scope去管理destroy
            } else {
                val scope = this.scopes[mbd.getScope()]
                    ?: throw IllegalStateException("BeanFactory当中没有注册Scope[name=${mbd.getScope()}, 但是BeanDefinition[name=$beanName]当中设置了该Scope]")
                scope.registerDestructionCallback(beanName, disposableBeanAdapter)
            }
        }
    }

    //-----------------------------------------------------------为Spring BeanFactory提供类型转换的支持-----------------------------------------------------

    /**
     * 获取自定义的TypeConverter
     *
     * @return 自定义的TypeConverter(如果没有去进行自定义BeanFactory的TypeConverter, 那么return null)
     */
    protected open fun getCustomTypeConverter(): TypeConverter? {
        return this.typeConverter
    }

    /**
     * 获取TypeConverter
     * * 1.如果BeanFactory当中有自定义TypeConverter的话, 那么使用自定义的去进行返回;
     * * 2.如果没有默认的, 那么将会构建出来一个默认的SimpleTypeConverter去进行返回;
     *
     * @return SpringBeanFactory的类型转换器TypeConverter(自定义的/默认的)
     */
    override fun getTypeConverter(): TypeConverter {
        // 1.获取用户自定义的TypeConverter
        val customTypeConverter = getCustomTypeConverter()
        if (customTypeConverter != null) {
            return customTypeConverter
        }

        // 2.构建默认的TypeConverter, apply ConversionService和PropertyEditor
        val typeConverter = SimpleTypeConverter()
        typeConverter.setConversionService(this.conversionService)
        registerCustomEditors(typeConverter)
        return typeConverter
    }

    /**
     * 对BeanWrapper去完成初始化工作, 为类型转换提供帮助
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
     * @param registry 想要把PropertyEditor注册到哪个PropertyEditorRegistry当中?
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

    override fun setTypeConverter(typeConverter: TypeConverter?) {
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
     * 这是一个BeanPostProcessorCache, 对各种类型的BeanPostProcessor去进行分类, 每次对BeanPostProcessor列表去进行更改(添加/删除)
     * 都需要**自行**地将BeanPostProcessorCache去进行clear掉(引用设置为null, 变相clear)
     */
    class BeanPostProcessorCache {
        // 干涉实例化的BeanPostProcessor(实例化之前、实例化之后、填充属性)
        val instantiationAwareCache = ArrayList<InstantiationAwareBeanPostProcessor>()

        // 干涉智能的实例化的BeanPostProcessor(获取早期类型、推断构造器、预测beanType)
        val smartInstantiationAwareCache = ArrayList<SmartInstantiationAwareBeanPostProcessor>()

        // 处理MergeBeanDefinition的PostProcessor
        val mergedDefinitions = ArrayList<MergedBeanDefinitionPostProcessor>()

        // 处理destroy的BeanPostProcessor
        val destructionAwareCache = ArrayList<DestructionAwareBeanPostProcessor>()

        fun hasInstantiationAware(): Boolean = instantiationAwareCache.isNotEmpty()
        fun hasSmartInstantiationAware(): Boolean = smartInstantiationAwareCache.isNotEmpty()
        fun hasMergedDefinition(): Boolean = mergedDefinitions.isNotEmpty()
        fun hasDestructionAwareCache(): Boolean = destructionAwareCache.isNotEmpty()
    }


    /**
     * 获取BeanPostProcessor的Cache, 它主要的作用是将各种类型的BeanPostProcessor去进行分类;
     * 如果之前已经构建好了, 那么直接进行return即可, 如果之前没有构建好? 需要重新对各个类型的BeanPostProcessor去进行分类,
     * 从而构建出来一个新的BeanPostProcessorCache去进行返回;
     *
     * Note: 在每次对BeanPostProcessor的列表去进行操作时, 应该reset BeanPostProcessorCache(把引用设置为null)
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

    /**
     * 获取当前BeanFactory当中的BeanPostProcessor的数量
     *
     * @return Count of BeanPostProcessor
     */
    override fun getBeanPostProcessorCount(): Int = this.beanPostProcessors.size
}