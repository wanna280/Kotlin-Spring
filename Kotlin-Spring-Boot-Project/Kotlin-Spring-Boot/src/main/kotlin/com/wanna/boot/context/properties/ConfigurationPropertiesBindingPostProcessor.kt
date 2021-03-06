package com.wanna.boot.context.properties

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.PriorityOrdered

/**
 * 这是一个处理@ConfigurationPropertie的绑定的BeanPostProcessor，它负责根据解析@ConfigurationProperties注解
 * 并把有@ConfigurationProperties注解的Bean去封装成为ConfigurationPropertiesBean，并把它注册到ConfigurationPropertiesBinder当中
 *
 * @see ConfigurationPropertiesBinder
 * @see ConfigurationPropertiesBean
 */
open class ConfigurationPropertiesBindingPostProcessor : BeanPostProcessor, ApplicationContextAware, PriorityOrdered,
    InitializingBean {
    companion object {
        @JvmField
        val BEAN_NAME: String = ConfigurationPropertiesBindingPostProcessor::class.java.name

        /**
         * (1)将ConfigurationPropertiesBinderPostProcessor注册到容器当中；
         * (2)将ConfigurationPropertiesBinder注册到容器当中
         */
        @JvmStatic
        fun register(registry: BeanDefinitionRegistry) {
            // 如果必要的话，给容器中注册一个ConfigurationPropertiesBindingPostProcessor的BeanDefinition
            if (!registry.containsBeanDefinition(BEAN_NAME)) {
                val beanDefinition = GenericBeanDefinition()
                beanDefinition.setBeanClass(ConfigurationPropertiesBindingPostProcessor::class.java)
                beanDefinition.setInstanceSupplier { ConfigurationPropertiesBindingPostProcessor() }
                beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                registry.registerBeanDefinition(BEAN_NAME, beanDefinition)
            }
            // 给BeanDefinitionRegistry当中注册一个ConfigurationPropertiesBinder
            ConfigurationPropertiesBinder.register(registry)
        }
    }

    private lateinit var applicationContext: ApplicationContext
    private lateinit var registry: BeanDefinitionRegistry
    private var binder: ConfigurationPropertiesBinder? = null


    /**
     * 在初始化Bean之前，去拦截下来Bean的创建，去判断Bean是否标注了@ConfigurationProperties注解？
     */
    override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any? {
        // 处理@ConfigurationProperties注册，把它注册到ConfigurationPropertiesBinder当中
        bind(ConfigurationPropertiesBean.get(this.applicationContext, bean, beanName))
        return bean
    }

    /**
     * 如果必要的话，将处理好的ConfigurationPropertiesBean注册到Binder当中
     */
    private fun bind(bean: ConfigurationPropertiesBean?) {
        // 如果该Bean没有标注@ConfigurationProperties注解，那么pass
        bean ?: return
        // 如果该Bean标注了@ConfigurationProperties注解，那么把它注册到Binder当中
        try {
            binder!!.bind(bean)
        } catch (ex: Exception) {
            throw ConfigurationPropertiesBindException(bean = bean, cause = ex)
        }
    }

    /**
     * 在初始化Bean时，获取BeanDefinitionRegistry和ConfigurationPropertiesBinder
     */
    override fun afterPropertiesSet() {
        this.registry = this.applicationContext.getAutowireCapableBeanFactory() as BeanDefinitionRegistry
        this.binder = ConfigurationPropertiesBinder.get(this.applicationContext)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 优先级，设置为最高的优先级，保证它可以最早地去生效
     */
    override fun getOrder() = Ordered.ORDER_HIGHEST + 1
}