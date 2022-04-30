package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.AbstractApplicationContext
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.processor.beans.MergedBeanDefinitionPostProcessor
import com.wanna.framework.core.util.ClassUtils
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap

/**
 * 它是一个ApplicationListener的Detector，完成ApplicationListener的检测，将容器中所有的ApplicationListener的Bean都给注册到容器当中
 */
open class ApplicationListenerDetector(private val applicationContext: AbstractApplicationContext) :
    MergedBeanDefinitionPostProcessor {

    // 容器中的所有的ApplicationListener的beanName列表，value存放的是它是否单例
    private val singletonNames = ConcurrentHashMap<String, Boolean>()

    /**
     * 完成ApplicationListener的检测，并将它是否单例的相关信息保存到Map当中
     */
    override fun postProcessMergedBeanDefinition(
        beanDefinition: RootBeanDefinition,
        beanType: Class<*>,
        beanName: String
    ) {
        if (ClassUtils.isAssignFrom(ApplicationListener::class.java, beanType)) {
            singletonNames[beanName] = beanDefinition.isSingleton()
        }
    }

    /**
     * 如果它是一个ApplicationListener的话，那么需要把它注册到ApplicationContext当中
     *
     * @see AbstractApplicationContext.addApplicationListener
     */
    override fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        if (bean is ApplicationListener<*>) {
            if (singletonNames[beanName] == true) {
                this.applicationContext.addApplicationListener(bean)
            } else {
                singletonNames.remove(beanName)
            }
        }
        return bean
    }

    /**
     * 重写equals方法，实现自定义的equals逻辑
     */
    override fun equals(other: Any?): Boolean =
        this === other || (other is ApplicationListenerDetector && other.applicationContext == this.applicationContext)

    override fun hashCode(): Int = Objects.hashCode(applicationContext)
}