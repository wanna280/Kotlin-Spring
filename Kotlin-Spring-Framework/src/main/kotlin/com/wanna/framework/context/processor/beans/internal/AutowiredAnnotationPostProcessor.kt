package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.beans.factory.annotation.Lookup
import com.wanna.framework.core.Ordered
import com.wanna.framework.beans.factory.InjectionMetadata
import com.wanna.framework.beans.factory.support.DependencyDescriptor
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.method.LookupOverride
import com.wanna.framework.beans.PropertyValues
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.beans.factory.annotation.Value
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.exception.BeanCreationException
import com.wanna.framework.context.processor.beans.SmartInstantiationAwareBeanPostProcessor
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.ReflectionUtils
import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 处理Autowired/Inject/Value注解的BeanPostProcessor
 */
open class AutowiredAnnotationPostProcessor : SmartInstantiationAwareBeanPostProcessor, ApplicationContextAware,
    Ordered {

    private var applicationContext: ApplicationContext? = null

    private var beanFactory: ConfigurableListableBeanFactory? = null

    // order
    private var order: Int = Ordered.ORDER_LOWEST - 10

    // 已经完成过处理的lookupMethod缓存，存的是beanName
    private val lookupMethodChecked: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap(256))

    // 候选构造器的缓存，key-beanClass，value-constructors
    private val candidateConstructorsCache: ConcurrentHashMap<Class<*>, Array<Constructor<*>>> = ConcurrentHashMap(256)

    // 哪些注解要作为Autowire注解？默认包括@Autowired/@Value和@Inject
    private val autowiredAnnotationTypes = HashSet<Class<out Annotation>>()

    init {
        autowiredAnnotationTypes.add(Autowired::class.java)
        autowiredAnnotationTypes.add(Value::class.java)
        try {
            autowiredAnnotationTypes.add(ClassUtils.forName("javax.inject.Inject"))
        } catch (ignored: ClassNotFoundException) {
            // ignore ClassNotFountException
        }
    }

    override fun getOrder(): Int {
        return this.order
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 设置AutowiredAnnotationTypess
     * @param types 要用哪些注解作为Autowired的注解？
     */
    open fun setAutowiredAnnotationTypess(types: Set<Class<out Annotation>>) {
        this.autowiredAnnotationTypes.clear()  // clear
        this.autowiredAnnotationTypes.addAll(types)  // add
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        this.beanFactory = (applicationContext as ConfigurableApplicationContext).getBeanFactory()
    }

    /**
     * 检查LookupMethodOverride，并推断合适的候选的构造器
     */
    override fun determineCandidateConstructors(beanClass: Class<*>, beanName: String): Array<Constructor<*>>? {
        // 如果这个Bean没有被处理LookupMethod过的话，遍历它的所有父类的所有方法去找Lookup注解
        if (!lookupMethodChecked.contains(beanName)) {
            var targetClass: Class<*>? = beanClass;
            do {
                ReflectionUtils.doWithLocalMethods(beanClass) { method ->
                    val lookup = method.getAnnotation(Lookup::class.java)
                    if (lookup != null) {
                        // 创建一个LookupOverride对象，并存放methodName和Lookup的value(要进行lookup的beanName)
                        val lookupOverride = LookupOverride(method, lookup.value)

                        // 将它加入到对应的BeanDefinition的运行时方法的Override列表当中
                        val rootBeanDefinition = beanFactory!!.getMergedBeanDefinition(beanName) as RootBeanDefinition
                        rootBeanDefinition.getMethodOverrides().addMethodOverride(lookupOverride);
                    }
                }
                targetClass = targetClass!!.superclass
            } while (targetClass != null && targetClass.superclass != Any::class.java)
            lookupMethodChecked += beanName
        }

        // 遍历所有的构造器，去推断出来合适的构造器
        var candidateConstructors = candidateConstructorsCache[beanClass]
        if (candidateConstructors == null) {
            synchronized(candidateConstructorsCache) {
                candidateConstructors = candidateConstructorsCache[beanClass]
                if (candidateConstructors == null) {
                    // 获取beanClass的原始构造器
                    val rawConstructors: Array<Constructor<*>> = beanClass.declaredConstructors

                    // 候选的构造器列表
                    val candidates = ArrayList<Constructor<*>>(rawConstructors.size)
                    // required构造器
                    var requiredConstructor: Constructor<*>? = null
                    // 默认的无参数构造器
                    var defaultConstructor: Constructor<*>? = null

                    // 遍历所有的构造器列表，去获取候选的构造器列表、默认的构造器，required的构造器
                    for (rawConstructor in rawConstructors) {
                        val autowiredAnnotation = findAutowiredAnnotation(rawConstructor)
                        // 如果从该构造器上找到了Autowired注解，那么它一定是一个候选的构造器
                        if (autowiredAnnotation != null) {
                            val requiredStatus = determineRequiredStatus(rawConstructor)
                            // 如果required=true
                            if (requiredStatus) {
                                if (candidates.isNotEmpty()) {
                                    throw BeanCreationException("找到了多个标注@Autowired注解的构造器，并且还有一个标注了required=true")
                                }
                                requiredConstructor = rawConstructor
                            }
                            candidates += rawConstructor
                            // 获取无参数构造器(默认构造器)
                        } else if (rawConstructor.parameterCount == 0) {
                            defaultConstructor = rawConstructor
                        }
                    }

                    if (candidates.isNotEmpty()) {
                        // 如果没有required=true的构造器，那么需要加入无参构造器作为候选构造器
                        if (requiredConstructor == null) {
                            if (defaultConstructor != null) {
                                candidates += defaultConstructor
                            }
                            candidateConstructors = candidates.toTypedArray()
                        }
                        // 如果没有找到候选的，但是只要一个有参数构造器，那么会以它为准
                    } else if (rawConstructors.size == 1 && rawConstructors[0].parameterCount > 0) {
                        candidateConstructors = arrayOf(rawConstructors[0])

                        // 如果没有找到合适的构造器...那么
                    } else {
                        candidateConstructors = emptyArray()
                    }
                    // 加入缓存
                    this.candidateConstructorsCache[beanClass] = candidateConstructors!!
                }
            }
        }
        return if (candidateConstructors!!.isNotEmpty()) candidateConstructors else null
    }

    // 要进行注入的元素的MetadataCache
    private val injectionMetadataCache = HashMap<Class<*>, InjectionMetadata>()

    /**
     * 需要去处理属性值注入
     */
    override fun postProcessProperties(pvs: PropertyValues?, bean: Any, beanName: String): PropertyValues? {
        val injectionMetadata = findAutowiringMetadata(bean::class.java, beanName, pvs)
        injectionMetadata.inject(bean, beanName, pvs)
        return null
    }

    /**
     * 在指定的类上去寻找要进行注入的Metadata元信息
     */
    protected fun findAutowiringMetadata(
        beanClass: Class<*>, beanName: String, pvs: PropertyValues?
    ): InjectionMetadata {
        // 先尝试从缓存中获取，如果缓存中没有，那么就去进行构建
        var injectionMetadata = injectionMetadataCache[beanClass]
        if (injectionMetadata == null) {
            val metadata = buildInjectionMetadata(beanClass)
            injectionMetadataCache[beanClass] = metadata
            injectionMetadata = metadata
        }
        return injectionMetadata
    }

    /**
     * 构建要进行注入的InjectionMetadata，封装每个要进行注入的元素成为InjectedElement
     */
    private fun buildInjectionMetadata(beanClass: Class<*>): InjectionMetadata {
        val elements = ArrayList<InjectionMetadata.InjectedElement>()

        var targetClass: Class<*>? = beanClass

        // 遍历所有的方法和字段，去构建InjectMetadata
        do {
            ReflectionUtils.doWithLocalFields(targetClass!!) { field ->
                val autowiredAnnotation = findAutowiredAnnotation(field)
                if (autowiredAnnotation != null) {
                    if (!Modifier.isStatic(field.modifiers)) {

                    }
                    // 决定是否是required
                    val required = determineRequiredStatus(field)
                    // 添加一个要进行注入的元素
                    elements += AutowiredFieldElement(field, required)
                }
            }
            ReflectionUtils.doWithLocalMethods(targetClass) { method ->
                val autowiredAnnotation = findAutowiredAnnotation(method)
                if (autowiredAnnotation != null) {
                    if (!Modifier.isStatic(method.modifiers)) {

                    }
                    // 决定是否是required
                    val required = determineRequiredStatus(method)

                    // 添加一个要进行注入的元素
                    elements += AutowiredMethodElement(method, required)
                }
            }


            targetClass = targetClass.superclass
        } while (targetClass != null && targetClass != Any::class.java)

        return InjectionMetadata.forElements(beanClass, elements)
    }

    /**
     * 在指定的元素上去找Autowired/Inject/Value注解
     */
    private fun findAutowiredAnnotation(accessibleObject: AccessibleObject): Annotation? {
        for (annotationClass in autowiredAnnotationTypes) {
            val annotation = AnnotatedElementUtils.getMergedAnnotation(accessibleObject, annotationClass)
            if (annotation != null) {
                return annotation
            }
        }
        return null
    }

    /**
     * 决定一个方法/字段上标注的Autowire相关注解中，是否是required？
     */
    private fun determineRequiredStatus(accessibleObject: AccessibleObject): Boolean {
        if (accessibleObject is Field) {
            return AnnotatedElementUtils.getMergedAnnotation(accessibleObject, Autowired::class.java)?.required ?: true
        } else if (accessibleObject is Method) {
            return AnnotatedElementUtils.getMergedAnnotation(accessibleObject, Autowired::class.java)?.required ?: true
        }
        return false
    }

    /**
     * 这是一个用来完成Autowired的字段注入的InjectedElement
     */
    inner class AutowiredFieldElement(_field: Field, _required: Boolean) : InjectionMetadata.InjectedElement(_field) {
        private val field = _field
        private val required = _required

        override fun inject(bean: Any, beanName: String, pvs: PropertyValues?) {
            // 解析出来字段的值
            val value = resolveFieldValue(field, beanName, bean)
            // 如果解析到的字段值不为空的话，那么使用反射去完成字段值的设置
            if (value != null) {
                ReflectionUtils.makeAccessiable(field)
                ReflectionUtils.setField(field, bean, value)
            }
        }

        /**
         * 从BeanFactory当中去解析一个字段的值，如果解析不到，return null
         */
        private fun resolveFieldValue(field: Field, beanName: String, bean: Any): Any? {
            // 根据field和required去构建一个DependencyDescriptor
            val descriptor = DependencyDescriptor(field, required)
            val autowiredBeans = HashSet<String>()
            val typeConverter = beanFactory?.getTypeConverter()
            // 交给BeanFactory去解析依赖
            return beanFactory?.resolveDependency(descriptor, beanName, autowiredBeans, typeConverter)
        }
    }

    /**
     * 这是一个用来完成Autowired的方法的注入的InjectedElement
     */
    inner class AutowiredMethodElement(_method: Method, _required: Boolean) :
        InjectionMetadata.InjectedElement(_method) {
        private val method = _method
        private val required = _required

        override fun inject(bean: Any, beanName: String, pvs: PropertyValues?) {
            // 从BeanFactory当中去完成解析方法参数
            val methodParams = resolveMethodParams(method, bean, beanName)

            // 如果解析到了方法参数列表，那么使用反射去执行
            if (methodParams != null) {
                ReflectionUtils.makeAccessiable(method)
                ReflectionUtils.invokeMethod(method, bean, *methodParams)
            }
        }

        /**
         * 从BeanFactory当中去解析到方法的全部参数
         */
        private fun resolveMethodParams(method: Method, bean: Any, beanName: String): Array<Any?>? {
            val parameterCount = method.parameterCount
            val descriptors: Array<DependencyDescriptor?> = arrayOfNulls(parameterCount)
            // 构建参数数组
            val arguments: Array<Any?> = arrayOfNulls(parameterCount)
            // 获取BeanFactory中的TypeConverter
            val typeConverter = beanFactory?.getTypeConverter()
            val autowiredBeans = HashSet<String>()

            // 遍历构建方法参数数组中的每个元素
            for (index in 0 until parameterCount) {
                val methodParameter = MethodParameter(method, index)
                val descriptor = DependencyDescriptor(methodParameter, required)
                descriptor.setContainingClass(bean::class.java)
                descriptors[index] = descriptor

                // 从beanFactory当中去进行解析出参数
                arguments[index] = beanFactory?.resolveDependency(descriptor, beanName, autowiredBeans, typeConverter)
            }
            return arguments
        }

    }


}