package com.wanna.framework.context

import com.wanna.framework.beans.InitializatingBean
import com.wanna.framework.beans.method.PropertyValues
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.method.MutablePropertyValues
import com.wanna.framework.context.aware.BeanFactoryAware
import com.wanna.framework.context.aware.BeanNameAware

abstract class AbstractAutowireCapableBeanFactory : AbstractBeanFactory(), AutowireCapableBeanFactory {


    // 是否开启了循环依赖？
    var allowCircularReferences: Boolean = true

    override fun createBean(beanName: String, bd: BeanDefinition): Any? {
        // 如果实例之前的BeanPostProcessor已经return 非空，产生出来一个对象了，那么需要完成初始化工作...
        // 如果必要的话，会完成动态代理，如果创建出来Bean，那么直接return，就不走doCreateBean的创建Bean的逻辑了...
        for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
            val instance = postProcessor.postProcessBeforeInstantiation(beanName, bd)
            if (instance != null) {
                return applyBeanPostProcessorsAfterInitialization(instance, beanName)
            }
        }

        return doCreateBean(beanName, bd)
    }


    protected open fun doCreateBean(beanName: String, bd: BeanDefinition): Any? {
        val beanInstance = bd.getBeanClass()!!.getDeclaredConstructor().newInstance()
        val beanWrapper: BeanWrapper = BeanWrapperImpl(beanInstance)

        val allowEarlyExposure = bd.isSingleton() && allowCircularReferences && isSingletonCurrentlyInCreation(beanName)
        // 如果设置了允许早期引用，那么将Bean放入到三级缓存当中...
        if (allowEarlyExposure) {
            // 添加到SingletonFactory当中
            addSingletonFactory(beanName, object : ObjectFactory<Any> {
                override fun getObject(): Any {
                    return getEarlyReference(beanInstance, beanName)
                }
            })
        }


        // 填充属性
        populateBean(beanWrapper, beanName)

        // 初始化Bean
        initializeBean(beanWrapper, beanName)

        // populateBean(BeanWrapper beanWrapper, BeanDefinition<?> bd, String beanName)
        return beanInstance
    }

    private fun initializeBean(wrapper: BeanWrapper, beanName: String) {
        var beanInstance = wrapper.getBeanInstance()

        // 执行Aware方法
        invokeAwareMethods(beanInstance, beanName)

        applyBeanPostProcessorsBeforeInitialization(beanInstance, beanName)

        // 执行初始化方法
        invokeInitMethod(beanInstance)

        applyBeanPostProcessorsAfterInitialization(beanInstance, beanName)
    }

    private fun populateBean(wrapper: BeanWrapper, beanName: String) {

        // 执行实例化之后的BeanPostProcessor
        for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
            if (!postProcessor.postProcessAfterInstantiation(beanName, wrapper.getBeanInstance())) {
                return
            }
        }

        // 完成Bean的属性填充
        var pvs: PropertyValues? = MutablePropertyValues()
        for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
            pvs = postProcessor.postProcessProperties(pvs, wrapper.getBeanInstance(), beanName)
        }
    }

    /**
     * 执行Init方法完成初始化
     */
    private fun invokeInitMethod(bean: Any) {
        if (bean is InitializatingBean) {
            bean.afterPropertiesSet()
        }
    }

    /**
     * 执行Aware，主要是BeanNameAware和BeanFactoryAware，别的Aware会在ApplicationContextAwareProcessor当中进行处理
     */
    private fun invokeAwareMethods(bean: Any, beanName: String) {
        if (bean is BeanNameAware) {
            bean.setBeanName(beanName)
        }
        if (bean is BeanFactoryAware) {
            bean.setBeanFactory(this)
        }
    }

    /**
     * 获取Bean的早期引用
     */
    protected open fun getEarlyReference(bean: Any, beanName: String): Any {
        var result = bean
        for (postProcessor in getBeanPostProcessorCache().smartInstantiationAwareCache) {
            result = postProcessor.getEarlyReference(bean, beanName)
        }
        return result
    }

    /**
     * 执行beforeInitialization方法，如果必要的话，创建代理
     */
    protected open fun applyBeanPostProcessorsBeforeInitialization(bean: Any, beanName: String): Any? {
        var result: Any? = bean
        for (postProcessor in this.beanPostProcessors) {
            result = postProcessor.postProcessBeforeInitialization(beanName, result!!)
        }
        return result
    }

    /**
     * 执行afterInitialization方法，如果必要的话，创建代理
     */
    protected open fun applyBeanPostProcessorsAfterInitialization(bean: Any, beanName: String): Any? {
        var result = bean
        for (postProcessor in this.beanPostProcessors) {
            val current = postProcessor.postProcessAfterInitialization(beanName, result)
            if (current == null) {
                return null
            }
            result = current
        }
        return result
    }
}