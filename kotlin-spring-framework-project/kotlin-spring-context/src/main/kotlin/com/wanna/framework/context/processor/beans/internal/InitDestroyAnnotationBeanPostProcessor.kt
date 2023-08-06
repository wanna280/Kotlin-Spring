package com.wanna.framework.context.processor.beans.internal

import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.beans.factory.config.DestructionAwareBeanPostProcessor
import com.wanna.framework.beans.factory.config.MergedBeanDefinitionPostProcessor
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.PriorityOrdered
import com.wanna.framework.util.ReflectionUtils
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 它是一个处理init/destroy的注解的处理器, 比如@PreDestroy、@PostConstruct注解, 可以通过相应的setter去完成自定义的AnnotationType
 */
open class InitDestroyAnnotationBeanPostProcessor : DestructionAwareBeanPostProcessor,
    MergedBeanDefinitionPostProcessor, PriorityOrdered {
    companion object {
        // 标识这是一个空的LifecycleMetadata常量, 在没有initMethods和destroyMethods时可以使用它来进行占位
        private val EMPTY_LIFECYCLE_METADATA = object : LifecycleMetadata(Any::class.java, emptyList(), emptyList()) {
            override fun invokeInitMethods(bean: Any) {}
            override fun invokeDestroyMethod(bean: Any) {}
            override fun hasDestroyMethods(): Boolean = false
        }

        // Logger
        private val logger = LoggerFactory.getLogger(InitDestroyAnnotationBeanPostProcessor::class.java)
    }

    // order
    private var order: Int = Ordered.ORDER_LOWEST

    // 要使用的初始化(init)方法的注解类型
    private var initAnnotationType: Class<out Annotation>? = null

    // 要使用的摧毁(destroy)方法的注解类型
    private var destroyAnnotationType: Class<out Annotation>? = null

    // lifecycle的Metadata缓存信息(如果该类上没有init的方法, 也没有destroy的方法, 放入一个EMPTY)
    private val lifecycleMetadataCache = ConcurrentHashMap<Class<*>, LifecycleMetadata>()

    /**
     * 在处理MergedBeanDefinition时, 提前完成Lifecycle的Metadata的构建工作
     *
     * @param beanDefinition MergedBeanDefinition
     * @param beanType beanType
     * @param beanName beanName
     */
    override fun postProcessMergedBeanDefinition(
        beanDefinition: RootBeanDefinition, beanType: Class<*>, beanName: String
    ) {
        // 在clazz上去寻找并构建LifecycleMetadata
        findLifecycleMetadata(beanType)
    }

    /**
     * 是否需要注册destroy回调? 只要该Bean有Destroy方法, 就需要进行注册
     *
     * @param bean bean
     * @return 是否需要注册destroy回调? return true则需要, return false则不需要
     */
    override fun requiresDestruction(bean: Any): Boolean {
        return findLifecycleMetadata(bean::class.java).hasDestroyMethods()
    }

    /**
     * 处理BeforeDestruction, 它会在一个Bean被从Spring BeanFactory当中移除掉时, 自动触发回调;
     * 完成Bean的收尾工作, SpringBeanFactory会去回调所有的destroy方法
     *
     * @param bean bean
     * @param beanName
     * @see com.wanna.framework.beans.factory.support.DisposableBeanAdapter
     */
    override fun postProcessBeforeDestruction(bean: Any, beanName: String) {
        findLifecycleMetadata(bean::class.java).invokeDestroyMethod(bean)
    }

    /**
     * 在Spring Bean初始化之前, 需要完成initMethod的回调, 去完成Spring Bean的初始化工作
     *
     * @param bean bean
     * @param beanName beanName
     * @return bean
     */
    override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any? {
        findLifecycleMetadata(bean::class.java).invokeInitMethods(bean)
        return bean
    }

    override fun getOrder(): Int = this.order

    /**
     * 给定一个beanClass, 去获取到生命周期的LifecycleMetadata信息;
     * 尝试先从缓存当中去进行获取, 如果缓存当中没有, 那么尝试去进行构建, 这里需要使用DCL去保证多线程下的线程安全问题;
     *
     * @param clazz beanClass
     * @return 该clazz构建完成的LifecycleMetadata(包含了所有的init/destroy的方法的信息)
     */
    private fun findLifecycleMetadata(clazz: Class<*>): LifecycleMetadata {
        // 使用DCL的方法, 去获取到LifecycleMetadata, 如果没有的话, 先去构建并加入缓存当中
        var lifecycleMetadata = lifecycleMetadataCache[clazz]
        if (lifecycleMetadata == null) {
            synchronized(this.lifecycleMetadataCache) {
                lifecycleMetadata = lifecycleMetadataCache[clazz]
                if (lifecycleMetadata == null) {
                    lifecycleMetadata = buildLifecycleMetadata(clazz)
                    this.lifecycleMetadataCache[clazz] = lifecycleMetadata!!
                }
            }
        }
        return lifecycleMetadata!!
    }

    /**
     * 根据clazz去构建LifecycleMetadata, 需要去处理方法上的initAnnotation和destroyAnnotation
     *
     * @param clazz 要去用于匹配init/destroy方法的目标beanClass
     * @return 构建好的LifecycleMetadata(如果init和destroy都没有, 那么return "EMPTY")
     */
    private fun buildLifecycleMetadata(clazz: Class<*>): LifecycleMetadata {
        val initMethods = ArrayList<LifecycleElement>()
        val destroyMethods = ArrayList<LifecycleElement>()
        var targetClass: Class<*>? = clazz

        // 遍历它以及它的所有父类, 去找到初始化方法和destroy方法
        do {
            targetClass ?: throw IllegalStateException("targetClass不应该为null")
            val currentInitMethods = ArrayList<LifecycleElement>()
            val currentDestroyMethods = ArrayList<LifecycleElement>()
            ReflectionUtils.doWithLocalMethods(targetClass) {
                if (initAnnotationType != null && it.isAnnotationPresent(initAnnotationType!!)) {
                    currentInitMethods += LifecycleElement(it)
                }
                if (destroyAnnotationType != null && it.isAnnotationPresent(destroyAnnotationType!!)) {
                    currentDestroyMethods += LifecycleElement(it)
                }
            }
            initMethods.addAll(0, currentInitMethods)  // 头插
            destroyMethods.addAll(currentDestroyMethods)  // 尾插
            targetClass = targetClass.superclass
        } while (targetClass != null && targetClass != Any::class.java)

        // 如果都为空, 返回一个空Lifecycle的常量
        if (initMethods.isEmpty() && destroyMethods.isEmpty()) {
            return EMPTY_LIFECYCLE_METADATA
        }
        // 如果存在有不为空的(init/destroy方法), 那么需要去构建LifecycleMetadata
        return LifecycleMetadata(clazz, initMethods, destroyMethods)
    }

    /**
     * 设置用于初始化的注解, 比如@PostConstruct
     *
     * @param annotationType 你想要设置的初始化注解
     */
    open fun setInitAnnotationType(annotationType: Class<out Annotation>) {
        this.initAnnotationType = annotationType
    }

    /**
     * 设置用于destroy的注解, 比如@PreDestroy
     *
     * @param annotationType 你想要设置的destroy注解
     */
    open fun setDestroyAnnotationType(annotationType: Class<out Annotation>) {
        this.destroyAnnotationType = annotationType
    }

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 它维护了一个类的生命周期(Lifecycle)元信息
     *
     * @param targetClass 目标类
     * @param initMethods 目标类当中的初始化方法
     * @param destroyMethods 目标类上的destroy方法
     */
    private open class LifecycleMetadata(
        private val targetClass: Class<*>,
        private val initMethods: List<LifecycleElement>,
        private val destroyMethods: List<LifecycleElement>
    ) {

        /**
         * 执行所有的init方法
         *
         * @param bean
         */
        open fun invokeInitMethods(bean: Any) {
            initMethods.forEach { it.invoke(bean) }
        }

        /**
         * 执行所有的destroy方法
         *
         * @param bean
         */
        open fun invokeDestroyMethod(bean: Any) {
            destroyMethods.forEach { it.invoke(bean) }
        }

        /**
         * 它是否有destroy方法, 如果有的话, 将会去添加destroy的回调
         */
        open fun hasDestroyMethods(): Boolean = this.destroyMethods.isNotEmpty()
    }

    /**
     * 它是一个Lifecycle的Element, 它维护了Lifecycle的一个方法;
     * 在Bean被初始化/摧毁时, 需要去进行反射执行该方法
     *
     * @param method init方法/destroy方法
     */
    private open class LifecycleElement(val method: Method) {
        open fun invoke(bean: Any) {
            try {
                ReflectionUtils.makeAccessible(method)
                ReflectionUtils.invokeMethod(method, bean)
            } catch (ex: Exception) {
                logger.warn("执行Lifecycle回调方法[${method.toGenericString()}]失败", ex)
            }
        }
    }

}