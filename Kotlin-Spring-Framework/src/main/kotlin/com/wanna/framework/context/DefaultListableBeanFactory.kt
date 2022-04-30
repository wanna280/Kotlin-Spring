package com.wanna.framework.context

import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.beans.factory.support.AutowireCandidateResolver
import com.wanna.framework.beans.factory.support.DependencyDescriptor
import com.wanna.framework.beans.factory.support.SimpleAutowireCandidateResolver
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.aware.BeanFactoryAware
import com.wanna.framework.context.exception.BeanNotOfRequiredTypeException
import com.wanna.framework.context.exception.NoSuckBeanDefinitionException
import com.wanna.framework.context.exception.NoUniqueBeanDefinitionException
import com.wanna.framework.core.OrderComparator
import com.wanna.framework.core.util.BeanFactoryUtils
import com.wanna.framework.core.util.ClassUtils
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.Arrays
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap


/**
 * 这是一个DefaultListableBeanFactory。
 * (1)它本身是一个ConfigurableListableBeanFactory，提供对BeanFactory的配置功能以及列举BeanFactory中的Bean相关信息的功能；
 * (2)它本身还是一个BeanDefinitionRegistry，主要提供BeanDefinition的注册中心的功能
 * (3)它本身还是一个AutowireCapableBeanFactory(有Autowire能力的BeanFactory)，可以从容器中获取到要进行注入的依赖
 */
open class DefaultListableBeanFactory : ConfigurableListableBeanFactory, BeanDefinitionRegistry,
    AbstractAutowireCapableBeanFactory() {

    // beanDefinitionMap
    private val beanDefinitionMap = HashMap<String, BeanDefinition>()

    // beanDefinitionNames
    private val beanDefinitionNames = HashSet<String>()

    // 这是一个依赖的比较器
    private var dependencyComparator: Comparator<Any?>? = null

    // 这是一个可以被解析的依赖，加入到这个Map当中的依赖可以进行Autowire，一般这里会注册BeanFactory，ApplicationContext等
    private var resolvableDependencies: ConcurrentHashMap<Class<*>, Any> = ConcurrentHashMap()

    // 这是用来处理Autowire的候选的依赖注入的Bean的解析器
    private var autowireCandidateResolver: AutowireCandidateResolver = SimpleAutowireCandidateResolver.INSTANCE

    override fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition) {
        beanDefinitionNames += name
        beanDefinitionMap[name] = beanDefinition
    }

    /**
     * 按照类型去注入一个可以被解析的依赖
     */
    override fun registerResolvableDependency(dependencyType: Class<*>, autowireValue: Any) {
        resolvableDependencies[dependencyType] = autowireValue
    }

    /**
     * 预实例化所有的单例Bean，在这里会完成后容器中所有非懒加载的单实例Bean的实例化和初始化工作
     */
    override fun preInstantiateSingletons() {
        ArrayList(beanDefinitionNames).forEach { beanName ->
            val mbd: RootBeanDefinition = getMergedBeanDefinition(beanName)
            // 如果该Bean是单例的、非抽象的、非懒加载的
            if (mbd.isSingleton() && !mbd.isAbstract() && !mbd.isLazyInit()) {
                if (isFactoryBean(beanName)) {
                    val bean = getBean(BeanFactory.FACTORY_BEAN_PREFIX + beanName)
                    if (bean is FactoryBean<*>) {
                        var isEagerInit = false
                        if (bean is SmartFactoryBean<*>) {
                            isEagerInit = bean.isEagerInit()
                        }
                        if (isEagerInit) {
                            getBean(beanName)
                        }
                    }
                } else {
                    getBean(beanName)
                }
            }
        }

        // 在初始化完所有的单实例Bean之后，需要回调所有的SmartInitializingSingleton
        beanDefinitionNames.forEach {
            val singleton = getSingleton(it, false)
            if (singleton is SmartInitializingSingleton) {
                singleton.afterSingletonsInstantiated()
            }
        }
    }

    override fun isFactoryBean(beanName: String): Boolean {
        val transformBeanName = transformBeanName(beanName)
        val singleton = getSingleton(transformBeanName, false)
        if (singleton != null) {
            return singleton is FactoryBean<*>
        }
        return false
    }

    private fun transformBeanName(beanName: String) = BeanFactoryUtils.transformBeanName(beanName)

    override fun getBeanDefinition(beanName: String) = beanDefinitionMap[beanName]

    override fun getBeanDefinitionNames() = ArrayList(beanDefinitionNames)

    override fun getBeanDefinitions() = ArrayList(beanDefinitionMap.values)

    override fun getBeanDefinitionCounts() = beanDefinitionNames.size

    override fun containsBeanDefinition(name: String) = beanDefinitionMap[name] != null

    open fun getDependencyComparator() = dependencyComparator

    open fun setDependencyComparator(dependencyComparator: Comparator<Any?>) {
        this.dependencyComparator = dependencyComparator
    }

    fun setAutowireCandidateResolver(autowireCandidateResolver: AutowireCandidateResolver) {
        this.autowireCandidateResolver = autowireCandidateResolver

        // 完成BeanFactory的设置
        if (autowireCandidateResolver is BeanFactoryAware) {
            autowireCandidateResolver.setBeanFactory(this)
        }
    }

    fun getAutowireCandidateResolver() = this.autowireCandidateResolver

    override fun isAutowireCandidate(beanName: String, descriptor: DependencyDescriptor): Boolean {
        return true
    }

    override fun resolveDependency(descriptor: DependencyDescriptor, requestingBeanName: String): Any? {
        return resolveDependency(descriptor, requestingBeanName, null, null)
    }

    override fun resolveDependency(
        descriptor: DependencyDescriptor,
        requestingBeanName: String,
        autowiredBeanName: MutableSet<String>?,
        typeConverter: TypeConverter?
    ): Any? {
        // TODO 需要对要进行注入的元素的@Lazy注解的检查，如果必要的话，需要生成代理
        var result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(descriptor, requestingBeanName)
        if (result == null) {
            // 从容器中去进行解析真正的依赖
            result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanName, typeConverter)
        }
        return result
    }

    open fun doResolveDependency(
        descriptor: DependencyDescriptor,
        requestingBeanName: String,
        autowiredBeanName: MutableSet<String>?,
        typeConverter: TypeConverter?
    ): Any? {

        val type = descriptor.getDependencyType()

        // 从AutowireCandidateResolve获取建议进行设置的值，主要用来处理@Value注解
        val value = getAutowireCandidateResolver().getSuggestedValue(descriptor)
        if (value != null) {
            // 如果value是String类型，那么需要使用嵌入式的值解析器完成解析...
            // TODO 在这里需要完成SpEL表达式的解析，以及嵌入式值解析器的解析工作
            if (value is String) {
                return this.resolveEmbeddedValue(value)
            }
            return null
        }

        // 解析要进行注入的元素是多个Bean的情况，例如Collection/Map/Array等情况
        val multipleBeans = resolveMultipleBeans(descriptor, requestingBeanName, autowiredBeanName, typeConverter)
        if (multipleBeans != null) {
            return multipleBeans
        }
        // 下面需要解析注入的元素是单个Bean的情况
        val candidates: Map<String, Any> = findAutowireCandidates(requestingBeanName, type, descriptor)

        // 如果根本没有找到候选的Bean，那么需要处理required=true/false并return
        if (candidates.isEmpty()) {
            if (descriptor.isRequired()) {
                throw NoSuckBeanDefinitionException("没有找到合适的Bean-->[beanType=$type]")
            }
            return null
        }
        var autowiredBeanName: String? = null  // 要进行autowire的beanName
        var instanceCandidate: Any? = null  // 要进行注入的bean

        // 如果找到了众多的候选Bean
        if (candidates.size > 1) {
            // TODO 从众多的候选Bean当中选择出来一个合适的Bean的beanName
            autowiredBeanName = determineAutowireCandidate(candidates, descriptor)
            if (autowiredBeanName != null) {
                instanceCandidate = candidates.get(autowiredBeanName)
            }
            // 如果就找到一个合适的候选Bean，那么这个Bean就是最终的候选Bean
        } else {
            autowiredBeanName = candidates.iterator().next().key
            instanceCandidate = candidates.iterator().next().value
        }

        var result = instanceCandidate
        if (result == null) {
            if (descriptor.isRequired()) {
                throw NoSuckBeanDefinitionException("没有找到合适的Bean-->[beanType=$type]")
            }
            result = null
        }
        if (result != null && !type.isInstance(result)) {
            throw BeanNotOfRequiredTypeException("给定的类型为[requiredType=$type]，找到的Bean类型为[type=${result::class.java}]不匹配")
        }
        // 如果类型匹配，那么返回最终的匹配的对象
        return result
    }

    /**
     * 从多个候选的Bean当中决定出最终要进行注入的bean
     * (1)使用Primary去进行决策
     * (2)使用Order去进行决策
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
     * 从Primary当中去决定，选出最佳的一个；如果找到了多个，那么抛出不Bean不唯一异常
     */
    private fun determinePrimaryCandidate(candidates: Map<String, Any>, requiredType: Class<*>): String? {
        var primaryCandidate: String? = null
        candidates.forEach { (beanName, bean) ->
            if (isPrimary(beanName, bean)) {
                if (primaryCandidate == null) {
                    primaryCandidate = beanName
                } else {
                    if (containsBeanDefinition(beanName) && containsBeanDefinition(primaryCandidate!!)) {
                        throw NoUniqueBeanDefinitionException("[requiredType=$requiredType]没有找到唯一的PrimaryBean去进行注入，有[$primaryCandidate,$beanName]等都是PrimaryBean")
                    }
                }
            }
        }
        return primaryCandidate
    }

    /**
     * 根据最高优先级去决定候选Bean的beanName
     */
    private fun determineHighestOrderCandidate(candidates: Map<String, Any>, requiredType: Class<*>): String? {
        var hignOrderCandidate: String? = null
        var highOrder: Int? = null
        candidates.forEach { beanName, bean ->
            val priority = getPriority(bean)
            if (priority != null) {
                if (highOrder == null) {
                    highOrder = priority
                    hignOrderCandidate = beanName
                } else {
                    // 如果遇到了highOrder==priority的情况，那么抛出Bean不唯一异常
                    if (highOrder == priority) {
                        throw NoUniqueBeanDefinitionException("需要的Bean类型[requiredType=$requiredType]不唯一，无法从容器中找到一个这样的¬合适的Bean")
                    } else if (highOrder!! > priority) {
                        highOrder = priority
                        hignOrderCandidate = beanName
                    }
                }
            }
        }
        return hignOrderCandidate
    }

    /**
     * 获取Bean的优先级
     */
    protected open fun getPriority(bean: Any?): Int? {
        val comparator = getDependencyComparator()
        if (comparator is OrderComparator) {
            return comparator.getPriority(bean)
        }
        return null
    }

    private fun isPrimary(beanName: String, beanInstance: Any): Boolean {
        return getMergedBeanDefinition(beanName).isPrimary()
    }

    /**
     * 根据DependencyDescriptor去BeanFactory当中寻找到所有的候选的要进行注入的Bean的列表
     */
    private fun findAutowireCandidates(
        beanName: String, requiredType: Class<*>, descriptor: DependencyDescriptor
    ): MutableMap<String, Any> {
        // 从容器中拿到所有的匹配requiredType的类型的beanName列表
        val candidateNames: List<String> = getBeanNamesForType(requiredType)
        val result: HashMap<String, Any> = HashMap()

        // 1.从BeanFactory当中注册的可解析的依赖(resolvableDependencies)当中尝试去进行解析
        this.resolvableDependencies.forEach { (type, obj) ->
            if (ClassUtils.isAssignFrom(requiredType, type) && requiredType.isInstance(obj)) {
                result[requiredType.name] = obj
            }
        }

        // 2.遍历容器中的所有类型去进行匹配
        candidateNames.forEach {
            // 从DependencyDescriptor当中解析到合适的依赖
            if (isAutowireCandidate(it, descriptor)) {
                result[it] = descriptor.resolveCandidate(it, requiredType, this) as Any
            }
        }

        return result
    }

    /**
     * 解析多个Bean的情况，比如Collection/Map/Array等类型的依赖的解析，有可能会需要用到Converter去完成类型的转换
     */
    private fun resolveMultipleBeans(
        descriptor: DependencyDescriptor,
        requestingBeanName: String,
        autowiredBeanName: MutableSet<String>?,
        typeConverter: TypeConverter?
    ): Any? {
        // 获取依赖的类型
        val type = descriptor.getDependencyType()
        if (type.isArray) {
            // 获取数组的元素类型，可以通过componentType去进行获取
            val componentType = type.componentType

            // 获取所有的候选的Bean，包括resolvableDependencies当中的依赖和beanFactory当中的对应的类型的Bean
            val candidates = findAutowireCandidates(requestingBeanName, componentType, descriptor)
            if (candidates.isEmpty()) {
                return null
            }
            // 利用Java的反射去创建数组，交给JVM去创建一个合成的数组类型
            val typeArray = java.lang.reflect.Array.newInstance(componentType, candidates.size)
            // 将候选的Bean列表转换为List，方便完成遍历
            val candidatesList = ArrayList(candidates.values)
            for (index in 0 until candidates.size) {
                java.lang.reflect.Array.set(typeArray, index, candidatesList[index])
            }

            // 将候选的要注入的beanNames列表进行输出...
            autowiredBeanName?.addAll(candidates.keys)

            // 利用Comparator完成排序并return
            Arrays.sort(typeArray as Array<*>, getDependencyComparator())
            return typeArray
        } else if (type == Map::class.java) {
            val genericTypes = descriptor.getGenericType()
            if (genericTypes is ParameterizedType) {
                val actualTypeArguments: Array<Type> = genericTypes.actualTypeArguments
                // 如果是Map类型，那么这里完全可以去断言：泛型类型的长度一定为2
                val keyGeneric = actualTypeArguments[0]
                val valueGeneric = actualTypeArguments[1]
                if (keyGeneric != String::class.java) {  // 如果key的泛型不是String类型，那么return null
                    return null
                }
                // 获取所有的候选Bean，这里的valueGeneric有可能是野生类型(WildTyppe，也就是Java中的?或者是Kotlin中的*)此时，这里会抛出类型转换异常
                val candidates = findAutowireCandidates(requestingBeanName, valueGeneric as Class<*>, descriptor)
                autowiredBeanName?.addAll(candidates.keys)
                return HashMap(candidates)
            }
        } else if (ClassUtils.isAssignFrom(Collection::class.java, type)) {
            val genericTypes = descriptor.getGenericType()
            if (genericTypes is ParameterizedType) {
                val valueType = genericTypes.actualTypeArguments[0] as Class<*>
                // 找到所有的候选类型的Bean
                val candidates = findAutowireCandidates(requestingBeanName, valueType, descriptor)
                val collection = type.getDeclaredConstructor().newInstance() as MutableCollection<Any>
                candidates.values.forEach(collection::add)
                autowiredBeanName?.addAll(candidates.keys)
                return collection
            }
        }
        return null
    }

    override fun getBeanDefinitionCount(): Int {
        return beanDefinitionNames.size
    }
}