package com.wanna.framework.context

import com.wanna.framework.beans.InitializatingBean
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.method.MutablePropertyValues
import com.wanna.framework.beans.method.PropertyValues
import com.wanna.framework.context.aware.BeanFactoryAware
import com.wanna.framework.context.aware.BeanNameAware
import com.wanna.framework.context.exception.BeanCreationException

abstract class AbstractAutowireCapableBeanFactory : AbstractBeanFactory(), AutowireCapableBeanFactory {

    // 是否开启了循环依赖？
    var allowCircularReferences: Boolean = true

    override fun createBean(beanName: String, mbd: RootBeanDefinition): Any? {
        // 如果实例之前的BeanPostProcessor已经return 非空，产生出来一个对象了，那么需要完成初始化工作...
        // 如果必要的话，会完成动态代理，如果创建出来Bean，那么直接return，就不走doCreateBean的创建Bean的逻辑了...
        for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
            val instance = postProcessor.postProcessBeforeInstantiation(beanName, mbd)
            if (instance != null) {
                return applyBeanPostProcessorsAfterInitialization(instance, beanName)
            }
        }

        return doCreateBean(beanName, mbd)
    }


    protected open fun doCreateBean(beanName: String, mbd: RootBeanDefinition): Any? {
        var beanWrapper = createBeanInstance(beanName, mbd)
        val beanInstance = beanWrapper.getWrappedInstance()
        val beanType = beanWrapper.getWrappedClass()

        val allowEarlyExposure =
            mbd.isSingleton() && allowCircularReferences && isSingletonCurrentlyInCreation(beanName)
        // 如果设置了允许早期引用，那么将Bean放入到三级缓存当中...
        if (allowEarlyExposure) {
            // 添加到SingletonFactory当中，ObjectFactory当中包装的是一getEarlyReference，当从SingletonFactory中获取对象时
            // 会自动回调getEarlyReference方法完成对象的创建
            addSingletonFactory(beanName, object : ObjectFactory<Any> {
                override fun getObject(): Any {
                    return getEarlyReference(beanInstance, beanName)
                }
            })
        }

        synchronized(mbd.postProcessLock) {
            if (!mbd.postProcessed) {
                /**
                 * 在Bean实例化之后，可以获取到BeanClass的真正类型，可以去完成BeanDefinition的Merged工作
                 * 给BeanPostProcessor一个机会，让它可以将parent BeanDefinition中的属性可以合并到当前的BeanDefinition当中
                 */
                try {
                    applyMergedBeanDefinitionPostProcessor(mbd, beanType, beanName)
                } catch (ex: Throwable) {
                    throw BeanCreationException("完成merged的后置处理工作失败，[beanName=$beanName]")
                }
                mbd.postProcessed = true
            }
        }

        var exposedBean: Any? = null

        try {
            // 填充属性
            populateBean(beanWrapper, beanName)

            // 初始化Bean
            exposedBean = initializeBean(beanWrapper, beanName)
        } catch (ex: Throwable) {
            if (ex is BeanCreationException) {
                throw ex
            }
            // 对ex进行再一次包装，往上抛
            throw BeanCreationException("创建Bean[beanName=$beanName]失败", ex)
        }

        // registerDisposableBeanIfNecessary
        return exposedBean
    }

    /**
     * 创建真正的Bean实例
     */
    private fun createBeanInstance(beanName: String, mbd: RootBeanDefinition): BeanWrapper {
        val beanInstance = mbd.getBeanClass()!!.getDeclaredConstructor().newInstance()
        val beanWrapper: BeanWrapper = BeanWrapperImpl(beanInstance)
        return beanWrapper
    }

    /**
     * 应用所有的MergedBeanDefinitionPostProcessor，去完成BeanDefinition的合并，此时得到的beanType为实例化之后得到的对象的真实beanType
     */
    private fun applyMergedBeanDefinitionPostProcessor(mbd: RootBeanDefinition, beanType: Class<*>, beanName: String) {
        getBeanPostProcessorCache().mergedDefinitions.forEach {
            it.postProcessMergedBeanDefinition(
                mbd,
                beanType,
                beanName
            )
        }
    }

    private fun initializeBean(wrapper: BeanWrapper, beanName: String): Any? {
        val beanInstance = wrapper.getWrappedInstance()

        // 执行Aware方法，beanName和beanFactory，是需要这里去完成的，别的类型的Aware
        // 就交给ApplicationContextAwareBeanPostProcessor去完成，因为ApplicationContextAware能获取更多对象，比如Environment
        invokeAwareMethods(beanInstance, beanName)

        applyBeanPostProcessorsBeforeInitialization(beanInstance, beanName)

        try {
            // 执行初始化方法
            invokeInitMethod(beanInstance)
        } catch (ex: Throwable) {
            throw BeanCreationException("执行对Bean[beanName=$beanName]的初始化过程中出现了异常", ex)
        }

        val bean = applyBeanPostProcessorsAfterInitialization(beanInstance, beanName)
        return bean
    }

    private fun populateBean(wrapper: BeanWrapper, beanName: String) {

        // 执行实例化之后的BeanPostProcessor
        for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
            if (!postProcessor.postProcessAfterInstantiation(beanName, wrapper.getWrappedInstance())) {
                return
            }
        }

        // 完成Bean的属性填充
        var pvs: PropertyValues? = MutablePropertyValues()
        for (postProcessor in getBeanPostProcessorCache().instantiationAwareCache) {
            pvs = postProcessor.postProcessProperties(pvs, wrapper.getWrappedInstance(), beanName)
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
     * 获取Bean的早期引用的回调，如果必要的话，会在这里去进行生成代理
     */
    protected open fun getEarlyReference(bean: Any, beanName: String): Any {
        var result = bean
        // 遍历所有的SmartInstantiationAware的BeanPostProcessor的getEarlyReference方法
        // 如果必要的话，会在这里完成AOP动态代理
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