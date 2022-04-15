package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.beans.method.PropertyValues
import com.wanna.framework.beans.factory.InjectionMetadata
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.ConfigurableListableBeanFactory
import com.wanna.framework.context.annotations.Autowired
import com.wanna.framework.context.annotations.Value
import com.wanna.framework.context.aware.ApplicationContextAware
import com.wanna.framework.context.processor.beans.SmartInstantiationAwareBeanPostProcessor
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
        // Autowired相关的注解
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
    val injectionMetadataCache = HashMap<Class<*>, InjectionMetadata>()

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
     * 构建要进行注入的InjectionMetadata
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
     * 决定是否是required？
     */
    private fun determineRequiredStatus(accessibleObject: AccessibleObject): Boolean {
        return false
    }

    inner class AutowiredFieldElement(_field: Field, _required: Boolean) : InjectionMetadata.InjectedElement(_field) {
        private val field = _field
        private val required = _required

        override fun getResourceToInject(bean: Any, beanName: String): Any? {
            return beanFactory!!.getBean(field.type)
        }
    }

    inner class AutowiredMethodElement(_method: Method, _required: Boolean) :
        InjectionMetadata.InjectedElement(_method) {
        private val method = _method
        private val required = _required

        override fun getResourceToInject(bean: Any, beanName: String): Any? {
            return beanFactory!!.getBean(method.returnType)
        }
    }


}