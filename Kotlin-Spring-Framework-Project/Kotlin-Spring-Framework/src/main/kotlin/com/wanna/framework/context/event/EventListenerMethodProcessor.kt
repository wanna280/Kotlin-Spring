package com.wanna.framework.context.event

import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ReflectionUtils
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * [EventListener]方法的处理器, 探测所有的Bean上的[EventListener]方法,
 * 并将这些探测到的[EventListener]方法包装成为一个[ApplicationListener]注册到[ApplicationContext]当中
 *
 * @see EventListener
 * @see EventListenerFactory
 * @see BeanFactoryPostProcessor
 * @see DefaultEventListenerFactory
 */
open class EventListenerMethodProcessor : BeanFactoryPostProcessor, SmartInitializingSingleton,
    ApplicationContextAware {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(EventListenerMethodProcessor::class.java)


    }

    /**
     * 没有标注@EventListener注解的类
     */
    private val nonAnnotatedClasses = Collections.newSetFromMap<Class<*>>(ConcurrentHashMap())

    /**
     * EventListenerFactory
     */
    @Nullable
    private var eventListenerFactories: MutableList<EventListenerFactory>? = null

    /**
     * BeanFactory
     */
    @Nullable
    private var beanFactory: ConfigurableListableBeanFactory? = null

    /**
     * ApplicationContext
     */
    @Nullable
    private var applicationContext: ConfigurableApplicationContext? = null

    /**
     * 当所有的单例Bean都完成初始化了, 我们需要去找到合适的EventListener并完成注册
     */
    override fun afterSingletonsInstantiated() {
        val beanFactory = beanFactory ?: throw IllegalStateException("ConfigurableListBeanFactory不能为null")
        val beanDefinitionNames = beanFactory.getBeanDefinitionNames()
        beanDefinitionNames.forEach {
            val type = beanFactory.getType(it) ?: throw IllegalStateException("无法获取到beanType")
            processBean(it, type)
        }
    }

    /**
     * 对于[beanType]类上的所有方法去进行检查，看它是否有[EventListener]注解;
     * 如果有的话, 我们需要将它去转换成为[ApplicationListener]
     *
     * @param beanName beanName
     * @param beanType beanType
     */
    private fun processBean(beanName: String, beanType: Class<*>) {
        if (!nonAnnotatedClasses.contains(beanType)) {
            val annotatedMethods = LinkedHashMap<Method, EventListener>()
            ReflectionUtils.doWithMethods(beanType) {
                val listener = AnnotatedElementUtils.getMergedAnnotation(it, EventListener::class.java)
                if (listener != null) {
                    annotatedMethods[it] = listener
                }
            }
            // 如果没有找到合适的@EventListener注解的方法
            if (annotatedMethods.isEmpty()) {
                nonAnnotatedClasses += beanType
                if (logger.isTraceEnabled) {
                    logger.trace("没有在[${beanType.name}]上找到@EventListener方法")
                }
            } else {
                val context = this.applicationContext ?: throw IllegalStateException("ApplicationContext不能为null")
                val factories =
                    this.eventListenerFactories ?: throw IllegalStateException("EventListenerFactories不能为null")
                annotatedMethods.keys.forEach { method ->
                    for (factory in factories) {
                        if (factory.supportsMethod(method)) {

                            // 使用EventListenerFactory去创建出来ApplicationListener
                            val applicationListener = factory.createApplicationListener(beanName, beanType, method)

                            // 如果它是一个ApplicationListenerMethodAdapter, 那么先完成ApplicationContext初始化
                            // 因为它是一个探测@EventListener方法的ApplicationListener, 它是真实需要用到ApplicationContext的...(比如getBean)
                            if (applicationListener is ApplicationListenerMethodAdapter) {
                                applicationListener.init(context)
                            }

                            // 将该ApplicationListener去添加到ApplicationContext当中
                            context.addApplicationListener(applicationListener)
                            break
                        }
                    }
                }
                if (logger.isDebugEnabled) {
                    logger.debug("在beanName=[$beanName]的类当中去找到了[${annotatedMethods.size}]个@EventListener方法")
                }
            }
        }
    }

    /**
     * 设置[ApplicationContext]
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        if (applicationContext is ConfigurableApplicationContext) {
            this.applicationContext = applicationContext
        } else {
            throw IllegalStateException("ApplicationContext的类型必须是ConfigurableApplicationContext")
        }

    }

    /**
     * 对[ConfigurableListableBeanFactory]去进行后置处理时, 我们从中探测到[EventListenerFactory]去进行保存下来
     *
     * @param beanFactory beanFactory
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // 保存下来BeanFactory
        this.beanFactory = beanFactory

        // 从BeanFactory当中探测到所有的EventListenerFactory
        val eventListenerFactoryNames = beanFactory.getBeanNamesForType(EventListenerFactory::class.java, false, false)
        val factories = ArrayList<EventListenerFactory>()
        eventListenerFactoryNames.forEach { factories.add(beanFactory.getBean(it, EventListenerFactory::class.java)) }

        // 对EventListerFactory按照Order去进行排序
        AnnotationAwareOrderComparator.sort(factories)

        // 保存下来所有的EventListenerFactory
        this.eventListenerFactories = factories
    }
}