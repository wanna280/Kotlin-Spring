package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.beans.TypeConverter
import com.wanna.framework.beans.factory.*
import com.wanna.framework.beans.factory.BeanFactory.Companion.FACTORY_BEAN_PREFIX
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.exception.NoUniqueBeanDefinitionException
import com.wanna.framework.core.ParameterNameDiscoverer
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.comparator.OrderComparator
import com.wanna.framework.util.BeanFactoryUtils
import com.wanna.framework.util.ClassUtils
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Predicate
import javax.inject.Provider


/**
 * 这是一个DefaultListableBeanFactory。
 * (1)它本身是一个ConfigurableListableBeanFactory, 提供对BeanFactory的配置功能以及列举BeanFactory中的Bean相关信息的功能；
 * (2)它本身还是一个BeanDefinitionRegistry, 主要提供BeanDefinition的注册中心的功能
 * (3)它本身还是一个AutowireCapableBeanFactory(有Autowire能力的BeanFactory), 可以从容器中获取到要进行注入的依赖
 */
open class DefaultListableBeanFactory : ConfigurableListableBeanFactory, BeanDefinitionRegistry,
    AbstractAutowireCapableBeanFactory() {

    companion object {
        /**
         * javax.inject.Provider--->对应于Spring家的ObjectProvider
         */
        @JvmStatic
        private var javaxInjectProviderClass: Class<*>? = null

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(DefaultListableBeanFactory::class.java)

        init {
            try {
                javaxInjectProviderClass =
                    ClassUtils.forName<Any>("javax.inject.Provider", DefaultListableBeanFactory::class.java.classLoader)
            } catch (ignored: ClassNotFoundException) {
                // ignored
            }
        }
    }

    /**
     * 是否允许发生BeanDefinition的覆盖？后来的BeanDefinition是否有资格去替换掉之前的BeanDefinition？
     */
    private var allowBeanDefinitionOverriding: Boolean = false

    /**
     * beanDefinitionMap, 使用ConcurrentHashMap去保证线程安全, 也会作为操作beanDefinitionNames和manualSingletonNames的锁对象
     */
    private val beanDefinitionMap = ConcurrentHashMap<String, BeanDefinition>()

    /**
     * beanDefinitionNames, 采用的是ArrayList去进行存储；
     * 实际上得加上beanDefinitionMap锁, 并采用写时复制的原则去进行操作, 保证使用方可以更加安全地去完成迭代；
     * 在添加元素/删除元素时, 都得新复制一份数据并进行修改, 接着重新设置引用, 就不影响使用方进行的迭代(也就是写时复制COW)
     */
    private var beanDefinitionNames = ArrayList<String>()


    /**
     * 这是一个依赖的比较器, 可以通过beanFactory去进行获取, 可以基于比较规则, 对依赖去完成排序；
     * 在注解版IOC容器当中, 默认情况下会被设置为[AnnotationAwareOrderComparator]
     *
     * @see AnnotationAwareOrderComparator
     * @see OrderComparator
     */
    private var dependencyComparator: Comparator<Any?>? = null

    /**
     * 这是一个可以被解析的依赖, 加入到这个Map当中的依赖可以进行Autowire(一般这里会注册BeanFactory, ApplicationContext等)。
     * 对于Value不仅仅可以注册一个普通的单例对象，当然特殊地也可以注册一个[ObjectFactory]，在进行依赖解析时会自动触发[ObjectFactory.getObject]
     *
     * @see ObjectFactory
     */
    private var resolvableDependencies: ConcurrentHashMap<Class<*>, Any> = ConcurrentHashMap()

    /**
     * 这是用来处理Autowire的候选的依赖注入的Bean的解析器
     * 在注解版IOC容器当中, 默认会被设置为ContextAnnotationAutowireCandidateResolver
     */
    private var autowireCandidateResolver: AutowireCandidateResolver = SimpleAutowireCandidateResolver.INSTANCE

    /**
     * **手工(manual)**注册的 singletonObject的beanName列表, 在这里进行维护, 它内部的beanName和beanDefinitionNames列表不会冲突；
     * 不然通过registerSingleton操作对单例Bean进行注册时, 后续要对它去进行匹配时, 没有办法找到该对象；
     * 因此这里就需要维护一个列表, 方便后期去进行类型的匹配, 比如解析Autowire依赖的时候, 就会对这个列表去进行匹配
     */
    private var manualSingletonNames = LinkedHashSet<String>()

    /**
     * 按照类型去注入一个可以被解析的依赖, 比如BeanFactory/ApplicationContext, 就需要去进行注册, 方便使用者可以去进行@Autowired注入
     *
     * @param dependencyType 要注册的依赖的类型
     * @param autowireValue 要去进行注入的值
     */
    override fun registerResolvableDependency(dependencyType: Class<*>, autowireValue: Any) {
        resolvableDependencies[dependencyType] = autowireValue
    }

    /**
     * 预实例化所有的单例Bean, 在这里会完成后容器中所有非懒加载的单实例Bean的实例化和初始化工作
     */
    override fun preInstantiateSingletons() {
        if (logger.isTraceEnabled) {
            logger.trace("在[$this]当中完成所有的单实例Bean的初始化工作")
        }
        // copy一份BeanDefinitionNames去进行实例化, 避免在初始化过程当中又遇到了新注册进来的BeanDefinition的情况, 这时候会出现并发修改异常
        val beanDefinitionNames = ArrayList(this.beanDefinitionNames)

        beanDefinitionNames.forEach { beanName ->
            val mbd: RootBeanDefinition = getMergedLocalBeanDefinition(beanName)
            // 如果该Bean是单例的、非抽象的、非懒加载的, 那么需要在这里去完成初始化...
            if (mbd.isSingleton() && !mbd.isAbstract() && !mbd.isLazyInit()) {

                // 如果它是一个SmartFactoryBean, 并且渴望去进行初始化, 才需要去进行getBean
                if (isFactoryBean(beanName)) {
                    val bean = getBean(FACTORY_BEAN_PREFIX + beanName)
                    if (bean is SmartFactoryBean<*> && bean.isEagerInit()) {
                        getBean(beanName)
                    }

                    // 如果它不是FactoryBean, 那么直接去getBean完成Bean的实例化和初始化工作
                } else {
                    getBean(beanName)
                }
            }
        }

        // 在初始化完所有的单实例Bean之后, 需要回调所有的SmartInitializingSingleton, 完成Bean的初始化工作...
        beanDefinitionNames.forEach {
            val singleton = getSingleton(it, false)
            if (singleton is SmartInitializingSingleton) {
                val smartInitialize =
                    this.getApplicationStartup().start("spring.beans.smart-initialize") // start initialize
                        .tag("beanName", it)  // tag beanName
                singleton.afterSingletonsInstantiated()
                smartInitialize.end() // end
            }
        }
    }

    /**
     * 给定一个beanName, 去判断该Bean是否是FactoryBean
     *
     * @param name beanName
     * @return 该Bean是否是FactoryBean？
     */
    override fun isFactoryBean(name: String): Boolean {
        //将beanName当中的&前缀全部去掉
        val transformBeanName = transformBeanName(name)

        // 1.尝试去从容器当中获取到Singleton对象, 看它类型是否是一个FactoryBean？
        val singleton = getSingleton(transformBeanName, false)
        if (singleton != null) {
            return singleton is FactoryBean<*>
        }

        // 2.如果当前BeanFactory当中没有该BeanDefinition, 那么从parent去进行寻找
        if (!containsBeanDefinition(name) && getParentBeanFactory() is ConfigurableBeanFactory) {
            return (getParentBeanFactory() as ConfigurableBeanFactory).isFactoryBean(name)
        }

        // 3.从BeanDefinition当中去进行判断是否是FactoryBean...
        return isFactoryBean(name, getMergedLocalBeanDefinition(name))
    }

    /**
     * 设置BeanFactory的依赖比较器(在注解版的IOC容器当中, 会自动注册基于注解的依赖比较器到容器当中)
     *
     * @param dependencyComparator 设置你想要使用的依赖比较器
     */
    open fun setDependencyComparator(dependencyComparator: Comparator<Any?>?) {
        this.dependencyComparator = dependencyComparator
    }

    /**
     * 设置处理AutowireCandidate的Resolver(在注解版的IOC容器当中, 会自动注册一个ContextAnnotationAutowireCandidateResolver到容器当中)
     *
     * @param autowireCandidateResolver 你想要设置的Resolver
     */
    open fun setAutowireCandidateResolver(autowireCandidateResolver: AutowireCandidateResolver) {
        this.autowireCandidateResolver = autowireCandidateResolver

        // 完成BeanFactory的设置
        if (autowireCandidateResolver is BeanFactoryAware) {
            autowireCandidateResolver.setBeanFactory(this)
        }
    }

    /**
     * 获取Autowire的候选的解析器, 用于匹配该Bean是否是一个可以作为目标依赖的候选注入Bean
     *
     * @return AutowireCandidateResolver
     */
    open fun getAutowireCandidateResolver(): AutowireCandidateResolver = this.autowireCandidateResolver

    /**
     * 判断一个候选Bean能否注入给DependencyDescriptor的目标元素？
     * 支持去进行匹配BeanDefinition当中的AutowireCandidate属性以及Qualifier注解等情况
     *
     * @param beanName beanName
     * @param descriptor 依赖描述符
     * @return 是否是一个Autowire的候选Bean？
     */
    override fun isAutowireCandidate(beanName: String, descriptor: DependencyDescriptor): Boolean {
        val resolver = getAutowireCandidateResolver()

        // 如果包含单实例BeanDefinition的话, 那么我们拿它的BeanDefinition去进行匹配
        if (containsBeanDefinition(beanName)) {
            return resolver.isAutowireCandidate(
                BeanDefinitionHolder(getMergedBeanDefinition(beanName), beanName), descriptor
            )

            // 如果包含SingletonBean, 但是没有BeanDefinition的话, 我们这里构建一个BeanDefinition去适配一下
        } else if (containsSingleton(beanName)) {
            return resolver.isAutowireCandidate(
                BeanDefinitionHolder(RootBeanDefinition(getType(beanName)), beanName), descriptor
            )
        }
        return true
    }

    /**
     * BeanObject的ObjectProvider
     *
     * @see ObjectProvider
     */
    private interface BeanObjectProvider<T> : ObjectProvider<T>, java.io.Serializable

    /**
     * 方便去进行内部的依赖的解析, 因此, 这里需要将ObjectFactory的泛型类型去进行作为真正的依赖类型, 这里我们进行包装一下
     */
    private class InnerDependencyDescriptor(
        private val descriptor: DependencyDescriptor, private val type: ResolvableType,
    ) : DependencyDescriptor(null, null, false, false) {

        /**
         * 它是否是必须的？
         */
        private var required: Boolean? = null

        // 如果指定了required, 那么使用自定义的, 不然就使用origin的
        constructor(descriptor: DependencyDescriptor, type: ResolvableType, required: Boolean) : this(
            descriptor, type
        ) {
            this.required = required
        }

        override fun getMethodParameter() = descriptor.getMethodParameter()

        override fun getAnnotations() = descriptor.getAnnotations()

        override fun <T : Annotation> getAnnotation(annotationClass: Class<T>) =
            descriptor.getAnnotation(annotationClass)

        override fun initParameterNameDiscoverer(parameterNameDiscoverer: ParameterNameDiscoverer?) =
            descriptor.initParameterNameDiscoverer(parameterNameDiscoverer)

        override fun getGenericType() = descriptor.getGenericType()

        override fun getContainingClass() = descriptor.getContainingClass()

        override fun setContainingClass(containingClass: Class<*>?) = descriptor.setContainingClass(containingClass)

        override fun getParameterIndex() = descriptor.getParameterIndex()

        override fun getMethodName() = descriptor.getMethodName()

        override fun getDeclaringClass() = descriptor.getDeclaringClass()

        override fun getParameterTypes() = descriptor.getParameterTypes()

        override fun getFieldName() = descriptor.getFieldName()

        override fun isRequired() = required ?: descriptor.isRequired()

        override fun isEager() = descriptor.isEager()

        override fun getDependencyType(): Class<*> = type.resolve()!!

        override fun getResolvableType() = type
    }

    /**
     * 用来完成Dependency的解析的ObjectProvider, 为获取Java对象提供懒加载的机制去进行实现
     *
     * @param originDescriptor 原始依赖描述符
     * @param beanName beanName
     * @param asTarget 要使用哪个父类的泛型类型去进行寻找？
     */
    private open inner class DependencyObjectProvider(
        private val originDescriptor: DependencyDescriptor, private val beanName: String?, asTarget: Class<*>
    ) : BeanObjectProvider<Any> {

        /**
         * 获取asTarget的泛型类型...
         */
        private val type = originDescriptor.getResolvableType().`as`(asTarget).getGenerics()[0]

        override fun getObject(): Any {
            val descriptorToUse = InnerDependencyDescriptor(originDescriptor, type)
            return doResolveDependency(descriptorToUse, beanName, null, null) ?: throw NoSuchBeanDefinitionException(
                originDescriptor.getResolvableType().toString()
            )
        }

        /**
         * 如果能够解析到依赖的话, return解析到的依赖；如果无法解析到该依赖的话, return null
         */
        override fun getIfAvailable(): Any? {
            return try {
                val descriptorToUse = InnerDependencyDescriptor(originDescriptor, type, false)
                doResolveDependency(descriptorToUse, beanName, null, null)
            } catch (ex: Exception) {
                null
            }
        }
    }

    /**
     * 提供去构建一个Jsr330的Provider的Factory
     */
    private inner class Jsr330Factory : java.io.Serializable {
        fun createDependencyProvider(
            descriptor: DependencyDescriptor, beanName: String?
        ): Provider<*> {
            return Jsr330Provider(descriptor, beanName)
        }
    }

    /**
     * Jsr330的Provider的实现, 通过继承DependencyObjectProvider去完成
     */
    private inner class Jsr330Provider(descriptor: DependencyDescriptor, beanName: String?) :
        DependencyObjectProvider(descriptor, beanName, javaxInjectProviderClass!!), Provider<Any> {
        override fun get(): Any {
            return getObject()
        }
    }

    /**
     * 解析目标依赖, 目标依赖的类型可以是Optional、ObjectFactory/ObjectProvider/Provider、Map/List/Collection/Set等多种类型
     *
     * @param descriptor 依赖描述符
     * @param requestingBeanName 请求去进行注入的beanName(可以为null)
     * @return 解析到的要去进行注入的依赖
     */
    override fun resolveDependency(descriptor: DependencyDescriptor, requestingBeanName: String?): Any? {
        return resolveDependency(descriptor, requestingBeanName, null, null)
    }

    /**
     * 解析目标依赖, 目标依赖的类型可以是Optional、ObjectFactory/ObjectProvider/Provider、Map/List/Collection/Set等多种类型
     *
     * @param descriptor 依赖描述符
     * @param requestingBeanName 请求去进行注入的beanName(可以为null)
     * @param autowiredBeanNames 解析到的所有依赖的beanName列表(输出参数, 如果为null则不用输出)
     * @param typeConverter 解析过程当中要使用到的TypeConverter(可以为null, 从BeanFactory当中去进行自动获取)
     * @return 解析到的要去进行注入的依赖
     */
    override fun resolveDependency(
        descriptor: DependencyDescriptor,
        requestingBeanName: String?,
        autowiredBeanNames: MutableSet<String>?,
        typeConverter: TypeConverter?
    ): Any? {
        // 初始化依赖描述符的"参数名发现器", 方便后续的过程当中去进行方法/构造器的参数名获取
        descriptor.initParameterNameDiscoverer(getParameterNameDiscoverer())

        // 如果要求注入的类型的java8当中的Optional
        if (descriptor.getDependencyType() == Optional::class.java) {
            return createOptionalDependency(descriptor, requestingBeanName)
            // 如果要求注入的是一个ObjectFactory/ObjectProvider的话, 那么统一去进行构建一个ObjectProvider(ObjectProvider是ObjectFactory的子接口)
        } else if (descriptor.getDependencyType() == ObjectFactory::class.java || descriptor.getDependencyType() == ObjectProvider::class.java) {
            return DependencyObjectProvider(descriptor, requestingBeanName, ObjectFactory::class.java)

            // 如果要求注入的是Jsr330的Provider, 那么在这里去进行create
        } else if (descriptor.getDependencyType() == javaxInjectProviderClass) {
            return Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName)
        }

        // 如果必要的话, 对@Lazy注解去进行注入的Bean去进行生成懒加载的代理...
        var result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(descriptor, requestingBeanName)
        if (result == null) {
            // 如果没有生成懒加载代理的话, 那么从容器中去进行解析真正的依赖
            result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter)
        }
        return result
    }

    /**
     * 解析目标依赖, 可以是Map/List/Set/Collection/Array的情况, 也可以是普通的单个Bean的情况
     * @param descriptor 依赖描述符
     * @param requestingBeanName 请求去进行注入的beanName(可以为null)
     * @param autowiredBeanNames 解析到的所有依赖的beanName列表(输出参数, 如果为null则不用输出)
     * @param typeConverter 解析过程当中要使用到的TypeConverter(可以为null, 从BeanFactory当中去进行自动获取)
     * @return 解析到的要去进行注入的依赖
     */
    open fun doResolveDependency(
        descriptor: DependencyDescriptor,
        requestingBeanName: String?,
        autowiredBeanNames: MutableSet<String>?,
        typeConverter: TypeConverter?
    ): Any? {
        // 设置InjectionPoint
        val previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor)
        try {
            val type = descriptor.getDependencyType()

            // 1. 从AutowireCandidateResolver去获取到建议进行设置的值, 主要用来处理@Value注解
            var value = getAutowireCandidateResolver().getSuggestedValue(descriptor)
            if (value != null) {
                // 如果value是String类型
                // 那么需要使用嵌入式的值解析器完成解析...(SpEL呢？)
                if (value is String) {
                    value = this.resolveEmbeddedValue(value)
                }
                // fixed: 使用TypeConverter去完成类型的转换工作...因为有可能@Value字段类型不一定是String, 可能是Int等类型
                try {
                    return (typeConverter ?: getTypeConverter()).convertIfNecessary(value, type)
                } catch (ex: Exception) {
                    logger.error("类型转换失败, 无法将String转换为目标类型[type=$type]", ex)
                }
                return value
            }

            // 2. 解析要进行注入的元素是多个Bean的情况, 例如Collection/List/Map/Array等情况
            val multipleBeans = resolveMultipleBeans(descriptor, requestingBeanName, autowiredBeanNames, typeConverter)
            if (multipleBeans != null) {
                return multipleBeans
            }

            // 3. 下面需要解析注入的元素是单个Bean的情况
            val matchingBeans: Map<String, Any> = findAutowireCandidates(requestingBeanName, type, descriptor)

            // 3.1 如果根本没有找到候选的Bean, 那么需要处理required=true/false并return
            if (matchingBeans.isEmpty()) {
                if (isRequired(descriptor)) {
                    throw NoSuchBeanDefinitionException(
                        "至少需要一个该类型的Bean, beanType=[$type], 但是在BeanFactory当中不存在",
                        null, null, type
                    )
                }
                return null
            }
            val autowiredBeanName: String?  // 要进行autowire的beanName
            var instanceCandidate: Any? = null  // 要进行注入的bean

            // 3.2 如果找到了众多的候选Bean, 那么需要去进行决策...
            if (matchingBeans.size > 1) {
                // 根据Order和Primary去进行决策出来一个合适的BeanDefinition...
                autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor)
                if (autowiredBeanName != null) {
                    instanceCandidate = matchingBeans[autowiredBeanName]
                }
                // 3.3 如果就找到一个合适的候选Bean, 那么这个Bean就是最终的候选Bean(毫无疑问)
            } else {
                autowiredBeanName = matchingBeans.iterator().next().key
                instanceCandidate = matchingBeans.iterator().next().value
            }
            if (autowiredBeanNames != null && autowiredBeanName != null) {
                autowiredBeanNames.add(autowiredBeanName)
            }
            var result = instanceCandidate
            if (result == null) {
                if (descriptor.isRequired()) {
                    throw NoSuchBeanDefinitionException(
                        "至少需要一个该类型的Bean, beanType=[$type], 但是在BeanFactory当中不存在",
                        null, null, type
                    )
                }
                result = null
            }

            if (result != null && !type.isInstance(result)) {
                throw BeanNotOfRequiredTypeException("", type, result::class.java)
            }
            // 如果类型匹配, 那么返回最终的匹配的对象
            return result
        } finally {
            // 复原InjectionPoint
            ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint)
        }
    }

    /**
     * 判断该依赖是否是必须的？
     *
     * @param descriptor 依赖描述符
     * @return 如果该依赖是必要的, return true；否则return false
     */
    protected open fun isRequired(descriptor: DependencyDescriptor): Boolean {
        return getAutowireCandidateResolver().isRequired(descriptor)
    }

    /**
     * 从多个候选的Bean当中决定出最终要进行注入的bean, 支持使用以下两种方式去进行决策
     * * (1)使用Primary去进行决策
     * * (2)使用Order去进行决策
     *
     * @throws NoUniqueBeanDefinitionException 如果无法从多个Bean当中去决策出来一个合适的Bean
     */
    private fun determineAutowireCandidate(candidates: Map<String, Any>, descriptor: DependencyDescriptor): String? {
        val primaryCandidate = determinePrimaryCandidate(candidates, descriptor.getDependencyType())
        if (primaryCandidate != null) {
            return primaryCandidate
        }
        val highestOrderCandidate = determineHighestOrderCandidate(candidates, descriptor.getDependencyType())
        if (highestOrderCandidate != null) {
            return highestOrderCandidate
        }
        return null
    }

    /**
     * 从Primary当中去决定, 选出最佳的一个；如果找到了多个, 那么抛出不Bean不唯一异常
     *
     * @param candidates 候选的要去进行匹配Primary的Bean
     * @param requiredType 请求去进行匹配的类型
     * @throws NoUniqueBeanDefinitionException 如果无法从多个Bean当中决策出一个合适的Bean
     */
    private fun determinePrimaryCandidate(candidates: Map<String, Any>, requiredType: Class<*>): String? {
        var primaryCandidate: String? = null
        candidates.forEach { (beanName, bean) ->
            if (isPrimary(beanName, bean)) {
                if (primaryCandidate == null) {
                    primaryCandidate = beanName
                } else {
                    if (containsBeanDefinition(beanName) && containsBeanDefinition(primaryCandidate!!)) {
                        throw NoUniqueBeanDefinitionException("[requiredType=$requiredType]没有找到唯一的PrimaryBean去进行注入, 有[$primaryCandidate,$beanName]等都是PrimaryBean")
                    }
                }
            }
        }
        return primaryCandidate
    }

    /**
     * 根据最高优先级去决定候选Bean的beanName
     *
     * @param candidates 候选的要去进行匹配的Bean列表
     * @param requiredType 需要进行匹配的类型
     * @return 如果找到了合适的最高优先级的Bean, return；否则return null
     * @throws NoUniqueBeanDefinitionException 如果无法从多个Bean当中决策出一个合适的Bean
     */
    private fun determineHighestOrderCandidate(candidates: Map<String, Any>, requiredType: Class<*>): String? {
        var highOrderCandidate: String? = null
        var highOrder: Int? = null
        candidates.forEach { (beanName, bean) ->
            val priority = getPriority(bean)
            if (priority != null) {
                if (highOrder == null) {
                    highOrder = priority
                    highOrderCandidate = beanName
                } else {
                    // 如果遇到了highOrder==priority的情况, 那么抛出Bean不唯一异常
                    if (highOrder == priority) {
                        throw NoUniqueBeanDefinitionException("需要的Bean类型[requiredType=$requiredType]不唯一, 无法从容器中找到一个这样的合适的Bean")
                    } else if (highOrder!! > priority) {
                        highOrder = priority
                        highOrderCandidate = beanName
                    }
                }
            }
        }
        return highOrderCandidate
    }

    /**
     * 创建一个Optional的依赖(来自于jdk1.8), 使用Optional去包装解析到的依赖, 如果没有解析到, 则包装一个null给调用方
     *
     * @param descriptor 依赖描述符
     * @param requestingBeanName 请求去进行注入的beanName
     * @return 构建好的Optional对象
     */
    private fun createOptionalDependency(descriptor: DependencyDescriptor, requestingBeanName: String?): Optional<*> {
        val available = DependencyObjectProvider(descriptor, requestingBeanName, Optional::class.java).getIfAvailable()
        return Optional.ofNullable(available)
    }

    /**
     * 使用依赖比较器去获取Bean的优先级, 如果beanFactory没有设置依赖的比较器的话, 那么return null
     *
     * @param bean 要去进行匹配优先级的bean
     * @return 如果有依赖比较器, 使用依赖比较器去进行获取；不然return null
     */
    protected open fun getPriority(bean: Any?): Int? {
        val comparator = getDependencyComparator()
        if (comparator is OrderComparator) {
            return comparator.getPriority(bean)
        }
        return null
    }

    /**
     * 判断一个Bean是否是Primary的？
     *
     * @param beanName beanName
     * @param beanInstance beanInstance
     * @return 该Bean是否是Primary的？如果是return true, 不然return false
     */
    private fun isPrimary(beanName: String, beanInstance: Any) = getMergedBeanDefinition(beanName).isPrimary()

    /**
     * 根据DependencyDescriptor去BeanFactory当中寻找到所有的候选的要进行注入的Bean的列表；
     * 所有会设涉及到Autowire的候选Bean的逻辑, 都会使用这个方法去进行完成
     *
     * @param beanName 正在请求注入的Bean的beanName, 例如A请求注入B, 那么这里beanName为A
     * @param requiredType requiredType, 需要去进行注入的元素类型
     * @param descriptor 依赖描述符(如果正在请求注入的字段是一个Array/Map/List, 那么这里为MultiElementDescriptor, requiredType=elementType)
     */
    private fun findAutowireCandidates(
        beanName: String?, requiredType: Class<*>, descriptor: DependencyDescriptor
    ): MutableMap<String, Any> {
        // 从BeanFactory(以及它的parentBeanFactory)当中中拿到所有的类型匹配requiredType的beanName列表
        val candidateNames =
            BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, true, descriptor.isEager())
        val result = LinkedHashMap<String, Any>()

        // 1.从BeanFactory当中注册的可解析的依赖(resolvableDependencies)当中尝试去进行解析, 比如BeanFactory/ApplicationContext等
        this.resolvableDependencies.forEach { (autowiringType, obj) ->
            // 检查一下ResolvableDependency的Key和requiredType是否匹配
            if (ClassUtils.isAssignFrom(autowiringType, requiredType)) {

                // 解析到要去进行注入的值(DependencyObject支持使用ObjectFactory, 并不一定要使用普通的单例对象)
                val autowiringValue = AutowireUtils.resolveAutowiringValue(obj, requiredType)

                // 如果得到的值, 和requiredType确实匹配的话, 那么收集到result当中去
                if (requiredType.isInstance(autowiringValue)) {
                    result[requiredType.name] = autowiringValue
                }
            }
        }

        // 2.遍历容器中的所有的类型匹配的Bean, 去进行挨个地匹配...为了AutowireCandidate的Bean
        candidateNames.forEach {
            // 从DependencyDescriptor当中解析到合适的依赖, 判断该Bean, 是否是一个Autowire候选Bean？
            // 比较类型和Qualifier(beanName)是否匹配？
            // Note: 这里我们必须需要去排除自引用的情况
            if (!isSelfReference(beanName, it) && isAutowireCandidate(it, descriptor)) {
                addCandidateEntry(result, it, descriptor, requiredType)
            }
        }

        // 如果还没搜索结果的话...尝试换个手段, 去进行匹配
        if (result.isEmpty()) {
            val multiple = indicatesMultipleBeans(requiredType)

            // 获取到Fallback的DependencyDescriptor
            val fallbackDescriptor = descriptor.forFallbackMatch()

            // 根据给定的fallbackDescriptor, 再去进行一次匹配
            candidateNames.forEach {
                // 从DependencyDescriptor当中解析到合适的依赖, 判断该Bean, 是否是一个Autowire候选Bean？
                if (!isSelfReference(beanName, it) && isAutowireCandidate(it, fallbackDescriptor)) {
                    addCandidateEntry(result, it, descriptor, requiredType)
                }
            }

            // 如果不是一个MultipleBean的话, 那么我们再去检查一下是否是自身引用的情况？
            // 对于自身引用的情况, 我们是完全允许的...对于非Array/Collection/Map的情况, 那么我们在这里去允许去注入自身
            // 这里我们首先得排除掉MultiElementDescriptor的情况, 因为它也可以决定是否正在注入的元素是一个MultipleBean
            if (!multiple) {
                candidateNames.forEach {
                    if (isSelfReference(beanName, it)  // allow self reference
                        && (descriptor !is MultiElementDescriptor)  // should not be MultiElementDescriptor
                        && isAutowireCandidate(it, fallbackDescriptor)  // autowire candidate
                    ) {
                        addCandidateEntry(result, it, descriptor, requiredType)
                    }
                }
            }
        }

        return result
    }

    /**
     * 检查是否是自引用？有一种情况, 那就是一个CompositeXxx的Bean, 想要去注入所有的XXx类型的Bean；
     * 此时就会因为出现自引用, 从而导致循环依赖, 但是对于这种情况来说, 实际上我们是允许的, 因此我们需要去排除掉。
     *
     * @param beanName beanName
     * @param candidateName 候选的正在去进行匹配的注入的对象
     */
    private fun isSelfReference(beanName: String?, candidateName: String?): Boolean {
        return beanName != null && candidateName != null && candidateName == beanName
    }

    private fun addCandidateEntry(
        candidates: MutableMap<String, Any>,
        candidateName: String,
        descriptor: DependencyDescriptor,
        requiredType: Class<*>
    ) {
        val resolveCandidate = descriptor.resolveCandidate(candidateName, requiredType, this)
        candidates[candidateName] = resolveCandidate
    }

    /**
     * 多个元素的DependencyDescriptor
     *
     * @param descriptor 原始的Descriptor
     */
    private class MultiElementDescriptor(descriptor: DependencyDescriptor) : DependencyDescriptor(
        descriptor.getField(),
        descriptor.getMethodParameter(),
        descriptor.isRequired(),
        descriptor.isEager()
    )

    /**
     * 解析多个Bean的情况, 比如Collection/Map/Array等类型的依赖的解析, 有可能会需要用到Converter去完成类型的转换
     *
     * @param descriptor 要去进行注入的依赖的依赖描述符
     * @param requestingBeanName 请求去进行注入的beanName(A请求注入B, requestingBeanName=A)
     * @param autowiredBeanName 自动注入的beanName列表, 作为输出参数(可以为null)
     * @param typeConverter 类型转换器
     */
    @Suppress("UNCHECKED_CAST")
    private fun resolveMultipleBeans(
        descriptor: DependencyDescriptor,
        requestingBeanName: String?,
        autowiredBeanName: MutableSet<String>?,
        typeConverter: TypeConverter?
    ): Any? {
        // 从依赖描述符当中去获取到依赖的类型
        val type = descriptor.getDependencyType()
        if (type.isArray) {
            // 获取数组的元素类型, 可以通过componentType去进行获取
            val componentType = type.componentType
            // 获取所有的候选的Bean, 包括resolvableDependencies当中的依赖和beanFactory当中的对应的类型的Bean
            val candidates =
                findAutowireCandidates(requestingBeanName, componentType, MultiElementDescriptor(descriptor))
            // 交给TypeConverter, 去利用Java的反射(java.lang.reflect.Array)去创建数组, 交给JVM去创建一个合成的数组类型
            val typeArray = (typeConverter ?: getTypeConverter()).convertIfNecessary(candidates.values, type)

            // 如果必要的话, 将候选的要注入的beanNames列表进行输出...
            autowiredBeanName?.addAll(candidates.keys)

            // 利用Comparator完成排序并return
            Arrays.sort(typeArray as Array<*>, getDependencyComparator())
            return typeArray

        } else if (type == Map::class.java) {
            val generics = descriptor.getResolvableType().asMap().getGenerics()
            // 如果是Map类型, 那么这里完全可以去断言：泛型类型的长度一定为2
            val keyGeneric = generics[0].resolve()
            val valueGeneric = generics[1].resolve()

            // 如果key的泛型不是String类型, 那么return null
            if (keyGeneric != String::class.java) {
                return null
            }
            // 获取所有的候选Bean
            val candidates =
                findAutowireCandidates(requestingBeanName, valueGeneric as Class<*>, MultiElementDescriptor(descriptor))
            autowiredBeanName?.addAll(candidates.keys)
            return LinkedHashMap(candidates)

        } else if (ClassUtils.isAssignFrom(Collection::class.java, type) && type.isInterface) {
            val generics = descriptor.getResolvableType().asCollection().getGenerics()
            val valueType = generics[0].resolve()
            // 找到所有的候选类型的Bean
            val candidates = findAutowireCandidates(requestingBeanName, valueType!!, MultiElementDescriptor(descriptor))
            var result = (typeConverter ?: getTypeConverter()).convertIfNecessary(candidates.values, type)

            // 如果是List的话, 那么先在这里排个序
            if (result is MutableList<*> && getDependencyComparator() != null) {
                result = result.sortedWith(getDependencyComparator()!!)
            }

            autowiredBeanName?.addAll(candidates.keys)
            return result
        }
        return null  // return null to fallback match single bean
    }

    /**
     * 判断给定的beanType是否是一个MultipleBean(如果是Array/Collection/Map的话, 那么就是MultipleBean)
     *
     * @param type 需要进行匹配的类型Class
     * @return 它是否是MultipleBean(如果是Array/Collection/Map的话, return true；否则return false)
     */
    private fun indicatesMultipleBeans(type: Class<*>): Boolean {
        return type.isArray ||
                (type.isInterface && (
                        ClassUtils.isAssignFrom(Collection::class.java, type)
                                || ClassUtils.isAssignFrom(Map::class.java, type)))
    }

    /**
     * 获取BeanFactory当中的beanDefinition的数量
     *
     * @return BeanDefinition的数量
     */
    override fun getBeanDefinitionCount(): Int = beanDefinitionNames.size

    /**
     * 将FactoryBean的引用符号&去掉, 称为"解引用"
     *
     * @param beanName 要去进行转换的name
     */
    private fun transformBeanName(beanName: String): String = BeanFactoryUtils.transformBeanName(beanName)

    /**
     * 获取BeanFactory当中的BeanDefinition的name列表
     *
     * @return 当前的BeanFactory当中的所有的BeanDefinition的name
     */
    override fun getBeanDefinitionNames(): List<String> = ArrayList(beanDefinitionNames)

    /**
     * 获取BeanFactory当中已经完成注册的BeanDefinition列表
     *
     * @return 当前的BeanFactory当中的所有的BeanDefinition的列表
     */
    override fun getBeanDefinitions(): List<BeanDefinition> = ArrayList(beanDefinitionMap.values)

    /**
     * 获取Spring BeanFactory的依赖比较器(可以为null)
     *
     * @return Spring BeanFactory的依赖比较器
     */
    open fun getDependencyComparator(): Comparator<Any?>? = dependencyComparator

    /**
     * 当前BeanFactory当中是否包含了BeanDefinition？
     * 在进行getBeanDefinition之前请先使用containsBeanDefinition去进行判断, 因为getBeanDefinition方法在找不到时会抛出异常
     *
     * @param name beanName
     * @return BeanDefinitionNames当中是否存在有该name对应的BeanDefinition？
     * @see getBeanDefinition
     */
    override fun containsBeanDefinition(name: String): Boolean = beanDefinitionNames.contains(name)

    /**
     * 获取BeanDefinition, 一定能获取到, 如果获取不到直接抛出异常；
     * 如果想要不抛出异常, 请先使用containsBeanDefinition去进行判断该BeanDefinition是否存在
     *
     * @throws NoSuchBeanDefinitionException 如果没有找到这样的BeanDefinition异常
     * @see containsBeanDefinition
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun getBeanDefinition(beanName: String): BeanDefinition {
        val beanDefinition = beanDefinitionMap[beanName]
        if (beanDefinition == null) {
            if (logger.isTraceEnabled) {
                logger.trace("给定的beanName[$beanName]在BeanFactory[$this]当中不存在对应的BeanDefinition")
            }
        }
        return beanDefinition ?: throw NoSuchBeanDefinitionException("BeanFactory当中没有name=[$beanName]的BeanDefinition")
    }

    /**
     * 移除BeanDefinition, 需要拿到锁(beanDefinitionMap)才能去对其进行操作, 对BeanDefinitionNames列表去进行操作不是线程安全的；
     * 为了保证BeanDefinitionNames可以去进行安全的迭代, 我们这里使用拷贝一份数据去进行修改的方式去进行remove, 也就是写时复制(COW)
     *
     * @param name beanName
     * @throws NoSuchBeanDefinitionException 如果没有根据name找到该BeanDefinition的话
     */
    override fun removeBeanDefinition(name: String) {
        beanDefinitionMap[name] ?: throw NoSuchBeanDefinitionException("BeanFactory当中没有name=[$name]的BeanDefinition")
        synchronized(this.beanDefinitionMap) {
            // copy一份原来的数据, 不要动原来的数据, 保证可以进行更加安全的迭代
            val beanDefinitionNames = ArrayList(beanDefinitionNames)
            beanDefinitionNames -= name
            this.beanDefinitionNames = beanDefinitionNames
            this.beanDefinitionMap -= name
        }

        // clear Merged BeanDefinition
        clearMergedBeanDefinition(name)
    }

    /**
     * clear掉MergedBeanDefinition, 对于一个BeanDefinition, 在完成Merge之后,
     * 有可能之后BeanDefinition已经去进行更新过, 因此, 就需要去进行重新Merge
     *
     * @param beanName beanName
     */
    override fun clearMergedBeanDefinition(beanName: String) {
        super.clearMergedBeanDefinition(beanName)
    }

    /**
     * 注册BeanDefinition, 需要拿到锁(beanDefinitionMap)才能去进行操作, 对BeanDefinitionNames列表去进行操作不是线程安全的
     *
     * Note: 注册BeanDefinition时, 会将之前的单实例Bean给移除掉
     *
     * @param name beanName
     * @param beanDefinition BeanDefinition
     */
    override fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition) {
        val existBeanDefinition = this.beanDefinitionMap[name]
        // 如果之前没有存在过, 那么需要操作BeanDefinitionNames, 需要加锁
        if (existBeanDefinition == null) {
            synchronized(this.beanDefinitionMap) {
                // copy一份原来的数据, 不要动原来的数据, 保证可以进行更加安全的迭代
                val beanDefinitionNames = ArrayList(beanDefinitionNames)
                beanDefinitionNames += name
                this.beanDefinitionNames = beanDefinitionNames
                beanDefinitionMap[name] = beanDefinition

                // 注册BeanDefinition时, 会将之前的单实例Bean给移除掉...
                removeManualSingletonName(name)
            }

            // 如果已经存在过的话, 那么...
        } else {
            beanDefinitionMap[name] = beanDefinition
        }
    }

    /**
     * 重写摧毁Singleton的逻辑, 因为有可能摧毁SingletonBean时, 我的manualSingleNames列表没有正确地去清除；
     * 因为本类当中的的manualSingleNames也是与单实例Bean的注册中心是使用的同一套逻辑(并进行扩展)
     *
     * @param beanName beanName
     */
    override fun destroySingleton(beanName: String) {
        super.destroySingleton(beanName)

        //将手动注册的SingletonBean, 也需要尝试去进行移除掉
        removeManualSingletonName(beanName)
    }

    /**
     * 移除一个已经注册的Singleton, 仅仅是移除手动通过registerSingleton注册到BeanFactory当中的, 别的方式注册的不会移除
     *
     * @param beanName beanName
     */
    protected open fun removeManualSingletonName(beanName: String) {
        updateManualSingleNames({ it.remove(beanName) }, { it.contains(beanName) })
    }

    /**
     * 更新所有已经进行手动注册的单实例Bean的列表, 对于更新什么, 如何更新都通过Function去进行给定
     *
     * @param action 要执行操作(Consumer)
     * @param filter 去匹配的filter(filter匹配时, 才去执行给定的action)
     */
    protected open fun updateManualSingleNames(action: Consumer<MutableSet<String>>, filter: Predicate<Set<String>>) {
        synchronized(this.beanDefinitionMap) {
            if (filter.test(this.manualSingletonNames)) {
                val updatedManualSingleNames = LinkedHashSet(this.manualSingletonNames)
                action.accept(updatedManualSingleNames)
                this.manualSingletonNames = updatedManualSingleNames
            }
        }
    }

    /**
     * 重写注册DefaultSingletonBeanRegistry的registerSingleton方法,
     * 目的是添加singletonBeanName去进行保存, 方便后续去进行寻找；
     * 因为有可能直接使用registerSingleton去注册一个单例Bean到容器当中,
     * 但是该Bean没有被BeanDefinitionMap所管理, 我们得找个地方去进行存储,
     * 需要去对手动管理的单例Bean, 去提供管理, 因为我们也许会用到获取SingletonBean的情况
     *
     * @see manualSingletonNames
     * @param beanName beanName
     * @param singleton 要去进行注册的单例Bean
     */
    override fun registerSingleton(beanName: String, singleton: Any) {
        super.registerSingleton(beanName, singleton)

        // Note: 操作manualSingletonNames , 也需要加上BeanDefinitionMap的锁
        synchronized(this.beanDefinitionMap) {
            // Note: 如果在BeanDefinition当中没有这个beanName, 才需要去进行添加
            // 因此, manualSingletonNames里面的内容和BeanDefinitionMap当中不会冲突
            updateManualSingleNames({ it.add(beanName) }, { !this.beanDefinitionMap.contains(beanName) })
        }
    }

    /**
     * 根据类型去进行getBean, 获取该类型的所有Bean的列表
     *
     * @param type beanType
     * @return Map<String,T>, key-beanName, value-BeanObject
     * @param T beanType类型
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getBeansForType(type: Class<T>): Map<String, T> {
        val beans = HashMap<String, T>()
        getBeanNamesForType(type).forEach { beans[it] = getBean(it, type) }
        return beans
    }

    /**
     * 给定具体类型(type), 去容器中找到所有的类型匹配的单实例Bean
     *
     * Note：这里不能去getBean的, 只能从BeanDefinition当中去进行匹配...
     *
     * @param type beanType
     * @return beanType对应的beanName列表
     */
    override fun getBeanNamesForType(type: Class<*>): List<String> {
        return doGetBeanNamesForType(type, true, true)
    }

    /**
     * 给定具体类型(type), 去容器中找到所有的类型匹配的单实例Bean
     *
     * Note：这里不能去getBean的, 只能从BeanDefinition当中去进行匹配...
     *
     * @param type beanType
     * @param includeNonSingletons 是否允许非单例对象？
     * @param allowEagerInit 是否允许去进行eager加载
     * @return beanType对应的beanName列表
     */
    override fun getBeanNamesForType(
        type: Class<*>, includeNonSingletons: Boolean, allowEagerInit: Boolean
    ): List<String> {
        return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit)
    }

    /**
     * 给定具体类型(type), 去容器中找到所有的类型匹配的单实例Bean, 也支持FactoryBean的匹配
     *
     * Note：这里不能去getBean的, 只能从BeanDefinition当中去进行匹配...
     *
     * @param type beanType
     * @param includeNonSingletons 是否允许非单例对象？
     * @param allowEagerInit 是否允许去进行eager加载
     * @return beanType对应的beanName列表
     */
    private fun doGetBeanNamesForType(
        type: Class<*>, includeNonSingletons: Boolean, allowEagerInit: Boolean
    ): List<String> {
        val beanNames = ArrayList<String>()
        getBeanDefinitionNames().forEach { beanName ->
            var beanNameToUse = beanName
            val mbd = getMergedLocalBeanDefinition(beanNameToUse)
            val isFactoryBean = isFactoryBean(beanNameToUse, mbd)

            val allowFactoryBeanInit = allowEagerInit || containsSingleton(beanNameToUse)

            var matchFound = false
            // 如果它不是一个FactoryBean的话, 那么直接去匹配就行
            if (!isFactoryBean) {
                if (isTypeMatch(beanNameToUse, type, allowFactoryBeanInit)) {
                    matchFound = true
                }
                // 如果它是一个FactoryBean的话, 那么需要匹配beanName, 也要匹配&beanName
            } else {
                if (isTypeMatch(beanNameToUse, type, allowFactoryBeanInit)) {
                    matchFound = true
                }
                if (!matchFound) {
                    // fixed: 如果是FactoryBean才匹配了你给的类型, 那么说明你想要的是FactoryBean, 我们必须给beanName加上&
                    beanNameToUse = FACTORY_BEAN_PREFIX + beanName
                    if (isTypeMatch(beanNameToUse, type, allowFactoryBeanInit)) {
                        matchFound = true

                    }
                }
            }
            if (matchFound) {
                beanNames += beanNameToUse
            }
        }
        // 匹配已经注册的单实例Bean的列表
        this.manualSingletonNames.forEach {
            val singleton = getSingleton(it)
            if (type.isInstance(singleton)) {
                beanNames += it
            }
        }
        return beanNames
    }

    /**
     * 摧毁掉当前BeanFactory当中的所有的Singleton
     * 对于父类SingletonBeanRegistry当中, 已经摧毁了它当中的所有的单例Bean,
     * 在这个类当中, 我们新增了手动注册SingletonBean的方式, 因此, 我们需要将
     * 全部手动注册的Bean, 都去进行destroy
     */
    override fun destroySingletons() {
        // super.destroySingletons, 清除Registry当中的所有的单实例Bean
        super.destroySingletons()
        // clear掉所有的手动去进行管理的SingletonBean
        updateManualSingleNames({ it.clear() }) { it.isNotEmpty() }
    }

    open fun setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding: Boolean) {
        this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding
    }

    open fun isAllowBeanDefinitionOverriding(): Boolean = this.allowBeanDefinitionOverriding
}