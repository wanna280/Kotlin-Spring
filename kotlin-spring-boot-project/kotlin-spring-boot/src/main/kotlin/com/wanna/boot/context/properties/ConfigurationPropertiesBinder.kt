package com.wanna.boot.context.properties

import com.wanna.boot.context.properties.bind.BindResult
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.boot.context.properties.bind.PropertySourcesPlaceholdersResolver
import com.wanna.boot.context.properties.source.ConfigurationPropertySource
import com.wanna.boot.context.properties.source.ConfigurationPropertySources
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.support.PropertySourcesPlaceholderConfigurer
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.PropertySources
import org.slf4j.LoggerFactory

/**
 * 这是一个ConfigurationProperties的Binder, 负责完成@ConfigurationProperties的绑定工作
 *
 * @see ConfigurationProperties
 * @see ConfigurationPropertiesBean
 * @see ConfigurationPropertiesBindingPostProcessor
 *
 * @param applicationContext ApplicationContext
 */
open class ConfigurationPropertiesBinder @Autowired private constructor(private val applicationContext: ApplicationContext) {
    /**
     * 内部组合一个真正用于去完成对于一个Bean的属性绑定工作的Binder
     */
    @Volatile
    private var binder: Binder? = null

    /**
     * 从[ApplicationContext]当中去推断出来合适的[PropertySources],
     * * 1.优先选[PropertySourcesPlaceholderConfigurer]内部的[PropertySources];
     * * 2.如果获取不到, 那么尝试从[ConfigurableEnvironment]当中去进行获取[PropertySources].
     */
    private var propertySources: PropertySources = PropertySourcesDeducer(applicationContext).getPropertySources()

    /**
     * 对于没有完成实例化的Bean, 那么使用构造器去进行实例化并完成属性的设置
     *
     * @param bean 要去进行绑定的ConfigurationPropertiesBean
     */
    open fun bindOrCreate(bean: ConfigurationPropertiesBean): Any? {
        return getBinder().bindOrCreate(bean.getAnnotation().prefix, bean.asTarget())
    }

    /**
     * 对已经完成实例化的Bean, 去完成ConfigurationProperties的绑定工作
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
        ConfigurationPropertiesBindConstructorProvider.INSTANCE
        if (this.binder == null) {
            this.binder = Binder(
                getConfigurationPropertySources(),
                getPropertySourcesPlaceholdersResolver(),
                getConversionServices(),
                null,
                ConfigurationPropertiesBindConstructorProvider.INSTANCE
            )
        }
        return binder!!
    }

    /**
     * 根据[PropertySources]去获取到[ConfigurationPropertySource]列表
     *
     * @return ConfigurationPropertySource列表
     */
    private fun getConfigurationPropertySources(): Iterable<ConfigurationPropertySource> {
        return ConfigurationPropertySources.from(this.propertySources)
    }

    /**
     * 根据[PropertySources]去获取到[PropertySourcesPlaceholdersResolver]
     *
     * @return PropertySourcesPlaceholdersResolver
     */
    private fun getPropertySourcesPlaceholdersResolver(): PropertySourcesPlaceholdersResolver {
        return PropertySourcesPlaceholdersResolver(this.propertySources)
    }

    /**
     * 从[ApplicationContext]当中去去获取到[ConversionService]列表
     *
     * @return ConversionServices
     */
    private fun getConversionServices(): List<ConversionService> {
        return ConversionServiceDeducer(this.applicationContext).getConversionServices()
    }

    companion object {

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ConfigurationPropertiesBinder::class.java)

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