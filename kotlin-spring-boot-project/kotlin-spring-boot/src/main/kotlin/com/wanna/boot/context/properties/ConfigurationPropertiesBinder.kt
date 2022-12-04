package com.wanna.boot.context.properties

import com.wanna.boot.context.properties.bind.BindResult
import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import org.slf4j.LoggerFactory
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * 这是一个ConfigurationProperties的Binder，负责完成@ConfigurationProperties的绑定工作
 *
 * @see ConfigurationProperties
 * @see ConfigurationPropertiesBean
 * @see ConfigurationPropertiesBindingPostProcessor
 */
open class ConfigurationPropertiesBinder : ApplicationContextAware {

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    /**
     * Environment
     */
    private var environment: ConfigurableEnvironment? = null

    /**
     * 内部组合一个真正用于去完成对于一个Bean的属性绑定工作的Binder
     */
    @Volatile
    private var binder: Binder? = null

    /**
     * 设置[ApplicationContext]
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext

        // 同步设置一下Environment
        this.environment = applicationContext.getEnvironment() as ConfigurableEnvironment
    }

    /**
     * 对于没有完成实例化的Bean，那么使用构造器去进行实例化并完成属性的设置
     *
     * @param bean 要去进行绑定的ConfigurationPropertiesBean
     */
    open fun bindOrCreate(bean: ConfigurationPropertiesBean): Any? {
        return getBinder().bindOrCreate(bean.getAnnotation().prefix, bean.asTarget())
    }

    /**
     * 对已经完成实例化的Bean，去完成ConfigurationProperties的绑定工作
     *
     * @param bean 要去进行绑定的ConfigurationPropertiesBean
     * @return 绑定结果BindResult
     */
    open fun bind(bean: ConfigurationPropertiesBean): BindResult<Any> {
        return getBinder().bind(bean.getAnnotation().prefix, bean.asTarget())
    }

    /**
     * 获取Binder, 去提供Java对象的属性的绑定功能
     *
     * @return Binder
     */
    open fun getBinder(): Binder {
        if (this.binder == null) {
            this.binder = Binder.get(environment!!)
        }
        return binder!!
    }

    companion object {

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ConfigurationProperties::class.java)

        /**
         * ConfigurationPropertiesBinder的beanName
         */
        @JvmField
        val BEAN_NAME: String = ConfigurationPropertiesBinder::class.java.name

        /**
         * 给容器中注册一个[ConfigurationPropertiesBinder]的相关基础设施Bean
         *
         * @param registry BeanDefinitionRegistry
         */
        @JvmStatic
        fun register(registry: BeanDefinitionRegistry) {
            // 如果之前Registry当中不存在这样的一个beanName, 那么往Registry当中去注册一个ConfigurationPropertiesBinder的BeanDefinition
            if (!registry.containsBeanDefinition(BEAN_NAME)) {
                val beanDefinition = GenericBeanDefinition()
                beanDefinition.setBeanClass(ConfigurationPropertiesBinder::class.java)
                beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                registry.registerBeanDefinition(BEAN_NAME, beanDefinition)
            }
        }

        /**
         * 从给定的beanFactory当中去获取[ConfigurationPropertiesBinder]
         *
         * @param beanFactory beanFactory
         * @return 获取到的[ConfigurationPropertiesBinder]
         */
        @JvmStatic
        fun get(beanFactory: BeanFactory): ConfigurationPropertiesBinder {
            return beanFactory.getBean(BEAN_NAME, ConfigurationPropertiesBinder::class.java)
        }
    }
}