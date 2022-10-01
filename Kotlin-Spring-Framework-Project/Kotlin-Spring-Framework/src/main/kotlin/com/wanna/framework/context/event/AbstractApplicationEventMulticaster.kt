package com.wanna.framework.context.event

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.util.ClassUtils
import java.util.Optional

/**
 * 它是一个抽象的ApplicationEventMulticaster，它提供了ApplicationListener的注册工作的相关的默认实现
 *
 * @see ApplicationEventMulticaster
 * @see SimpleApplicationEventMulticaster
 */
abstract class AbstractApplicationEventMulticaster : ApplicationEventMulticaster, BeanFactoryAware,
    BeanClassLoaderAware {

    private var beanFactory: BeanFactory? = null

    private var beanClassLoader: ClassLoader? = null

    private val defaultRetriever = DefaultListenerRetriever()

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    private fun getBeanFactory(): BeanFactory? = this.beanFactory

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.beanClassLoader = classLoader
    }

    open fun getBeanClassLoader(): ClassLoader? = this.beanClassLoader

    override fun addApplicationListener(listener: ApplicationListener<*>) {
        synchronized(this.defaultRetriever) {
            this.defaultRetriever.applicationListeners += listener
        }
    }

    override fun addApplicationListenerBean(listenerBeanName: String) {
        synchronized(this.defaultRetriever) {
            this.defaultRetriever.applicationListenerBeans += listenerBeanName
        }
    }

    override fun removeApplicationListener(listener: ApplicationListener<*>) {
        synchronized(this.defaultRetriever) {
            this.defaultRetriever.applicationListeners -= listener
        }
    }

    override fun removeApplicationListenerBean(listenerBeanName: String) {
        synchronized(this.defaultRetriever) {
            this.defaultRetriever.applicationListenerBeans -= listenerBeanName
        }
    }

    override fun removeAllApplicationListeners() {
        synchronized(this.defaultRetriever) {
            this.defaultRetriever.applicationListeners.clear()
            this.defaultRetriever.applicationListenerBeans.clear()
        }
    }

    protected open fun getApplicationListeners(): Collection<ApplicationListener<*>> {
        synchronized(this.defaultRetriever) {
            return this.defaultRetriever.getAllApplicationListeners()
        }
    }

    /**
     * 根据ApplicationEvent以及eventType，去找到合适的ApplicationListener列表
     *
     * @param event 目标事件
     * @param eventType 目标事件的类型
     * @return 能够支持处理当前事件的Listener列表
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <E : ApplicationEvent> getApplicationListeners(
        event: ApplicationEvent,
        eventType: ResolvableType
    ): Collection<ApplicationListener<E>> {
        val applicationListeners = ArrayList<ApplicationListener<*>>()
        getApplicationListeners()
            .forEach {
                if (it is SmartApplicationListener) {
                    if (it.supportEventType(eventType.resolve() as Class<out ApplicationEvent>)) {
                        applicationListeners += it
                    }
                } else if (it is GenericApplicationListener) {
                    if (it.supportsEventType(eventType)) {
                        applicationListeners += it
                    }
                    // 如果是普通的ApplicationListener，那么需要去推断Event类型是否合法...
                } else {
                    val listenerType = ResolvableType.forClass(it::class.java).`as`(ApplicationListener::class.java)
                    val generics = listenerType.getGenerics()
                    if (generics.isNotEmpty()) {
                        val eventClass = eventType.resolve()
                        val listenerGeneric = generics[0].resolve()
                        if (ClassUtils.isAssignFrom(listenerGeneric, eventClass)) {
                            applicationListeners += it
                        }
                        // 如果没有泛型类型？自己看着办
                    } else {
                        applicationListeners += it
                    }
                }
            }
        return applicationListeners as ArrayList<ApplicationListener<E>>
    }

    /**
     * 这是一个ApplicationListener的注册中心，它可以存放ApplicationListener对象列表，也可以存放ApplicationListener的beanName
     * <note>这必须是一个inner class，因为它要访问外部的BeanFactory完成getBean</note>
     */
    private inner class DefaultListenerRetriever {
        val applicationListeners = LinkedHashSet<ApplicationListener<*>>()
        val applicationListenerBeans = LinkedHashSet<String>()

        /**
         * 获取所有的ApplicationListener，包括单例Bean以及beanName
         *
         * @return 所有的ApplicationListener的列表(完成排序工作)
         */
        fun getAllApplicationListeners(): Collection<ApplicationListener<*>> {
            // 1.添加实例对象的ApplicationListener对象列表
            val listeners = ArrayList<ApplicationListener<*>>(this.applicationListeners)

            // 2.对所有的ApplicationListener的beanName的列表去完成getBean，并加入到候选的Listeners列表当中
            Optional.ofNullable(getBeanFactory()).ifPresent { beanFactory ->
                applicationListenerBeans.forEach {
                    val listener = beanFactory.getBean(it, ApplicationListener::class.java)
                    if (!listeners.contains(listener)) {
                        listeners += listener
                    }
                }
            }
            // 完成对所有的ApplicationListener的排序工作
            AnnotationAwareOrderComparator.sort(listeners)
            return listeners
        }

    }
}