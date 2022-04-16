package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.beans.method.PropertyValues
import com.wanna.framework.beans.factory.InjectionMetadata
import com.wanna.framework.beans.factory.support.DependencyDescriptor
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.ConfigurableListableBeanFactory
import com.wanna.framework.context.annotations.Autowired
import com.wanna.framework.context.annotations.Value
import com.wanna.framework.context.aware.ApplicationContextAware
import com.wanna.framework.context.processor.beans.SmartInstantiationAwareBeanPostProcessor
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 处理Autowired/Inject/Value注解的BeanPostProcessor
 */
class AutowiredAnnotationPostProcessor : SmartInstantiationAwareBeanPostProcessor, ApplicationContextAware {

    private var applicationContext: ApplicationContext? = null

    private var beanFactory: ConfigurableListableBeanFactory? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        this.beanFactory = (applicationContext as ConfigurableApplicationContext).getBeanFactory()
    }

    companion object {
        // Autowired相关的注解，包括@Autowired/@Value和@Inject
        @JvmField
        val autowiredAnnotationType = HashSet<Class<out Annotation>>()

        init {
            autowiredAnnotationType.add(Autowired::class.java)
            autowiredAnnotationType.add(Value::class.java)
            try {
                autowiredAnnotationType.add(ClassUtils.forName("javax.inject.Inject"))
            } catch (ignored: ClassNotFoundException) {
                // ignore ClassNotFountException
            }
        }
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
        beanClass: Class<*>,
        beanName: String,
        pvs: PropertyValues?
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
        for (annotationClass in autowiredAnnotationType) {
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
    inner class AutowiredFieldElement(_field: Field, _required: Boolean) :
        InjectionMetadata.InjectedElement(_field) {
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
                ReflectionUtils.invokeMethod(method, bean, methodParams)
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