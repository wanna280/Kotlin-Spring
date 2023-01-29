package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.PropertyValues
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.InjectionMetadata
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import  com.wanna.framework.beans.factory.config.InstantiationAwareBeanPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.util.StringUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.Nullable
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.annotation.Resource

/**
 * 它负责去处理通用的java的注解, 尤其是在JSR250当中的"java.annotations"包中的相关注解;
 * 它通过继承InitDestroyAnnotationBeanPostProcessor去支持@PostConstruct/@PreDestroy注解的处理;
 * 这个类的核心是去支持@Resource注解的处理, 它是JSR规范当中注解驱动的方式去实现按名注入的基础, 它能够支持
 * 从Spring BeanFactory容器当中自动完成JavaPojo的按名去进行注入
 *
 * @see InitDestroyAnnotationBeanPostProcessor
 * @see PreDestroy
 * @see PostConstruct
 */
open class CommonAnnotationPostProcessor : InitDestroyAnnotationBeanPostProcessor(),
    InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    companion object {
        // 支持去进行处理的@Resource注解类型列表
        private val resourceAnnotationTypes = LinkedHashSet<Class<out Annotation>>(2)

        init {
            resourceAnnotationTypes += Resource::class.java
        }
    }

    @Nullable
    private var resourceFactory: BeanFactory? = null

    @Nullable
    private var beanFactory: BeanFactory? = null

    open fun setResourceFactory(resourceFactory: BeanFactory) {
        this.resourceFactory = resourceFactory
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
        if (this.resourceFactory == null) {
            this.resourceFactory = beanFactory
        }
    }

    // InjectionMetadataCache, 维护要去进行自动注入的相关信息
    private val injectionMetadataCache = ConcurrentHashMap<String, InjectionMetadata>(256)

    // 在初始化对象时, 需要设置父类当中的init/destroy的注解
    init {
        this.setInitAnnotationType(PostConstruct::class.java)
        this.setDestroyAnnotationType(PreDestroy::class.java)
        this.setOrder(Ordered.ORDER_LOWEST - 3)
    }

    /**
     * 在父类当中, postProcessMergedBeanDefinition用于完成LifecycleMetadata的构建;
     * 在本类当中, 应该扩展该功能, 完成ResourceMetadata的构建工作(提供自动注入工作)
     *
     * @param beanDefinition MergedBeanDefinition
     * @param beanName beanName
     * @param beanType beanType
     */
    override fun postProcessMergedBeanDefinition(
        beanDefinition: RootBeanDefinition, beanType: Class<*>, beanName: String
    ) {
        super.postProcessMergedBeanDefinition(beanDefinition, beanType, beanName)
        findResourceMetadata(beanName, beanType, null)
    }

    /**
     * 在postProcessProperties时, 完成ResourceMetadata当中的每个InjectedElement的注入工作
     *
     * @param pvs PropertyValues
     * @param bean bean
     * @param beanName beanName
     * @return PropertyValues
     */
    override fun postProcessProperties(pvs: PropertyValues?, bean: Any, beanName: String): PropertyValues? {
        findResourceMetadata(beanName, bean::class.java, pvs).inject(bean, beanName, pvs)
        return pvs
    }

    /**
     * 寻找@Resource的元信息, 先尝试从缓存当中获取, 如果缓存当中没有, 那么尝试先去进行构建并加入到缓存当中;
     * 操作缓存时需要加锁, 避免多线程并发时造成线程安全问题
     *
     * @param beanName beanName
     * @param beanType beanType
     * @param pvs propertyValues
     * @return 寻找到的InjectionMetadata
     */
    private fun findResourceMetadata(beanName: String, beanType: Class<*>, pvs: PropertyValues?): InjectionMetadata {
        var injectionMetadata = injectionMetadataCache[beanName]
        if (injectionMetadata == null) {
            synchronized(this.injectionMetadataCache) {
                injectionMetadata = injectionMetadataCache[beanName]
                if (injectionMetadata == null) {
                    injectionMetadata = buildResourceMetadata(beanType)
                    injectionMetadataCache[beanName] = injectionMetadata!!
                }
            }
        }
        return injectionMetadata!!
    }

    /**
     * 构建@Resource的Metadata元信息, 方便后期去进行主任
     *
     * @param clazz 要去寻找@Resource的目标类(支持寻找它的父类)
     * @return 构建好的InjectMetadata
     */
    private fun buildResourceMetadata(clazz: Class<*>): InjectionMetadata {
        // 存放clazz当中要去进行注入的元素列表
        val elements = ArrayList<InjectionMetadata.InjectedElement>()

        // 从当前给定的clazz开始, 遍历它的所有父类
        var targetClass: Class<*>? = clazz
        do {
            targetClass ?: throw IllegalStateException("targetClass不应该为空")

            // 1.检查@Resource注解的方法
            ReflectionUtils.doWithLocalMethods(targetClass) {
                if (it.isAnnotationPresent(Resource::class.java)) {
                    if (Modifier.isStatic(it.modifiers)) {
                        throw IllegalStateException("@Resource注解不能标注在static方法上")
                    }
                    if (it.parameterCount > 1) {
                        throw IllegalStateException("@Resource注解的方法参数不能超过1个")
                    }
                    elements += ResourceElement(it, it)
                }
            }

            // 2.检查@Resource注解的字段
            ReflectionUtils.doWithLocalFields(targetClass) {
                if (it.isAnnotationPresent(Resource::class.java)) {
                    if (Modifier.isStatic(it.modifiers)) {
                        throw IllegalStateException("@Resource注解不能标注在static字段上")
                    }
                    elements += ResourceElement(it, it)
                }
            }
            targetClass = targetClass.superclass
        } while (targetClass != null && targetClass != Any::class.java)
        return InjectionMetadata.forElements(clazz, elements)
    }

    /**
     * 这是一个Resource的InjectedElement, 它描述了一个@Resource标注的方法/字段, 需要去完成自动注入
     *
     * @param member 方法/字段
     * @param element 标注@Resource的元素(方法或者字段)
     */
    private inner class ResourceElement(member: Member, private val element: AnnotatedElement) :
        InjectionMetadata.InjectedElement(member) {
        private var name: String = element.getAnnotation(Resource::class.java).name

        private var lazyLookup = false

        init {
            // 如果必要的话, 需要去解析resourceName
            if (!StringUtils.hasText(name)) {
                if (isField) {
                    name = (this.member as Field).name
                } else {
                    val methodName = (this.member as Method).name
                    if (methodName.startsWith("set") && methodName.length > 3) {
                        name = StringUtils.uncapitalizeAsProperty(methodName.substring(3))
                    }
                }
            }
            val lazy: com.wanna.framework.context.annotation.Lazy? =
                element.getAnnotation(com.wanna.framework.context.annotation.Lazy::class.java)
            lazyLookup = lazy?.value ?: false
        }

        /**
         * 实现自定义的获取Resource去进行注入逻辑, 其余部分沿用父类当中的模板方法
         *
         * @param bean bean
         * @param beanName beanName
         * @return 去执行自动注入的元素
         */
        override fun getResourceToInject(bean: Any, beanName: String): Any {
            return resourceFactory?.getBean(name)
                ?: throw IllegalStateException("没有ResourceFactory去提供@Resource的注入")
        }
    }
}