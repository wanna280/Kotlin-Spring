package com.wanna.framework.context

import com.wanna.framework.beans.definition.BeanDefinition
import com.wanna.framework.context.exception.BeanDefNotFoundException
import com.wanna.framework.context.exception.BeansException
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.processor.beans.InstantiationAwareBeanPostProcessor
import com.wanna.framework.context.processor.beans.MergedBeanDefinitionPostProcessor
import com.wanna.framework.context.processor.beans.SmartInstantiationAwareBeanPostProcessor
import com.wanna.framework.util.ClassUtils

/**
 * 这是一个抽象的BeanFactory
 */
abstract class AbstractBeanFactory() : BeanFactory, ConfigurableBeanFactory, ListableBeanFactory,
    FactoryBeanRegistrySupport() {

    class BeanPostProcessorCache {
        val instantiationAwareCache = ArrayList<InstantiationAwareBeanPostProcessor>()
        val smartInstantiationAwareCache = ArrayList<SmartInstantiationAwareBeanPostProcessor>()
        val mergedDefinitions = ArrayList<MergedBeanDefinitionPostProcessor>()

        fun hasInstantiationAware(): Boolean {
            return instantiationAwareCache.isEmpty()
        }

        fun hasSmartInstantiationAware(): Boolean {
            return smartInstantiationAwareCache.isEmpty()
        }

        fun hasMergedDefinition(): Boolean {
            return mergedDefinitions.isEmpty()
        }
    }

    // BeanPostProcessorCache
    private var beanPostProcessorCache: BeanPostProcessorCache? = null

    /**
     * 获取BeanPostProcessor的Cache
     */
    fun getBeanPostProcessorCache(): BeanPostProcessorCache {
        if (this.beanPostProcessorCache == null) {
            this.beanPostProcessorCache = BeanPostProcessorCache()
            beanPostProcessors.forEach {
                if (it is InstantiationAwareBeanPostProcessor) {
                    this.beanPostProcessorCache!!.instantiationAwareCache += it
                    if (it is SmartInstantiationAwareBeanPostProcessor) {
                        this.beanPostProcessorCache!!.smartInstantiationAwareCache += it
                    }
                }
                if (it is MergedBeanDefinitionPostProcessor) {
                    this.beanPostProcessorCache!!.mergedDefinitions += it
                }
            }
        }
        return beanPostProcessorCache!!
    }

    // BeanPostProcessor列表
    private val beanPostProcessors = ArrayList<BeanPostProcessor>()

    override fun getBean(beanName: String): Any? {
        return doGetBean(beanName)
    }

    private fun doGetBean(beanName: String): Any? {

        var singleton = getSingleton(beanName, true)

        // 这里其实还需要判断FactoryBean，这里暂时不处理
        if (singleton != null) {
            return singleton
        }

        val beanDefinition = getBeanDefinition(beanName)
        if (beanDefinition == null) {
            throw BeanDefNotFoundException("The bean definition of [$beanName] can't be find")
        }

        singleton = getSingleton(beanName, object : ObjectFactory<Any> {
            override fun getObject(): Any {
                return createBean(beanName, beanDefinition)
                    ?: throw BeansException("Create bean instance of [$beanName] failed")
            }
        })

        return singleton
    }

    /**
     * 提供创建Bean的逻辑，交给子类去进行实现
     */
    protected abstract fun createBean(beanName: String, bd: BeanDefinition): Any?

    override fun <T> getBean(beanName: String, type: Class<T>): T? {
        return getBean(beanName) as T?
    }

    override fun <T> getBean(type: Class<T>): T? {
        TODO("Not yet implemented")
    }

    override fun isSingleton(beanName: String): Boolean {
        val beanDefinition = getBeanDefinition(beanName)
        return if (beanDefinition != null) beanDefinition.isSingleton() else throw BeansException()
    }

    override fun isPrototype(beanName: String): Boolean {
        val beanDefinition = getBeanDefinition(beanName)
        return if (beanDefinition != null) beanDefinition.isPrototype() else throw BeansException()
    }

    override fun addBeanPostProcessor(processor: BeanPostProcessor) {
        beanPostProcessors -= processor
        beanPostProcessors += processor
        this.beanPostProcessorCache = null  // clear
    }

    override fun removeBeanPostProcessor(type: Class<*>) {
        beanPostProcessors.removeIf { ClassUtils.isAssginFrom(type, it::class.java) }
    }

    override fun removeBeanPostProcessor(index: Int) {
        beanPostProcessors.removeAt(index)
    }

    override fun isFactoryBean(beanName: String): Boolean {
        return getBeanDefinition(beanName)?.isFactoryBean() ?: throw BeansException()
    }

    override fun isTypeMatch(beanName: String, type: Class<*>): Boolean {
        val beanDefinition = getBeanDefinition(beanName)
        return if (beanDefinition != null) ClassUtils.isAssginFrom(
            type, beanDefinition.beanClass
        ) else throw BeansException()
    }

    override fun getType(beanName: String): Class<*> {
        val beanDefinition = getBeanDefinition(beanName)
        return if (beanDefinition != null) beanDefinition.beanClass else throw BeansException()
    }

    override fun getBeanNamesForType(type: Class<*>): List<String> {
        TODO("Not yet implemented")
    }

    override fun <T> getBeansForType(type: Class<T>): List<T> {
        TODO("Not yet implemented")
    }

    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> {
        TODO("Not yet implemented")
    }

    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): List<T> {
        TODO("Not yet implemented")
    }

    /**
     * 获取BeanDefinition
     */
    abstract fun getBeanDefinition(beanName: String): BeanDefinition?
}