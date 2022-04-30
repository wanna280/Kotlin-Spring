package com.wanna.framework.context.event

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator

/**
 * 它提供了ApplicationListener的注册工作
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

    open fun getBeanClassLoader(): ClassLoader? {
        return this.beanClassLoader
    }

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
     */
    protected open fun getApplicationListeners(
        event: ApplicationEvent,
        eventType: Class<out ApplicationEvent>
    ): Collection<ApplicationListener<*>> {
        val applicationListeners = ArrayList<ApplicationListener<*>>()
        getApplicationListeners().forEach {
            if (it is SmartApplicationListener) {
                if (it.supportEventType(eventType)) {
                    applicationListeners += it
                }
            } else {
                applicationListeners += it
            }
        }
        return applicationListeners
    }

    /**
     * 这是一个ApplicationListener的注册中心，它可以存放ApplicationListener对象列表，也可以存放ApplicationListener的beanName
     * <note>这必须是一个inner class，因为它要访问外部的BeanFactory完成getBean</note>
     */
    private inner class DefaultListenerRetriever {
        val applicationListeners = LinkedHashSet<ApplicationListener<*>>()
        val applicationListenerBeans = LinkedHashSet<String>()

        fun getAllApplicationListeners(): Collection<ApplicationListener<*>> {
            val listeners = ArrayList<ApplicationListener<*>>()
            listeners += applicationListeners  // 添加实例对象的ApplicationListener对象列表
            if (getBeanFactory() != null) {
                applicationListenerBeans.forEach {
                    val listener = getBeanFactory()!!.getBean(it, ApplicationListener::class.java)
                    if (!listeners.contains(listener)) {
                        listeners += listener as ApplicationListener<*>
                    }
                }
            }
            // 完成排序
            AnnotationAwareOrderComparator.sort(listeners)
            return listeners
        }

    }
}