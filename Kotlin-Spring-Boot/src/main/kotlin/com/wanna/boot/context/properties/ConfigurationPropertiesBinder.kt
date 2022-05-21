package com.wanna.boot.context.properties

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.util.ReflectionUtils
import org.slf4j.LoggerFactory

/**
 * 这是一个ConfigurationProperties的Binder，负责完成@ConfigurationProperties的绑定工作
 */
open class ConfigurationPropertiesBinder : ApplicationContextAware {

    private var applicationContext: ApplicationContext? = null

    private var environment: ConfigurableEnvironment? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        this.environment = applicationContext.getEnvironment() as ConfigurableEnvironment
    }

    companion object {
        @JvmField
        val BEAN_NAME: String = ConfigurationPropertiesBinder::class.java.name

        // logger
        private val logger = LoggerFactory.getLogger(ConfigurationProperties::class.java)

        /**
         * 给容器中注册ConfigurationPropertiesBinder的相关基础设施Bean
         */
        @JvmStatic
        fun register(registry: BeanDefinitionRegistry) {
            if (!registry.containsBeanDefinition(BEAN_NAME)) {
                val beanDefinition = GenericBeanDefinition()
                beanDefinition.setBeanClass(ConfigurationPropertiesBinder::class.java)
                beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                registry.registerBeanDefinition(BEAN_NAME, beanDefinition)
            }
        }

        /**
         * 从beanFactory当中去获取ConfigurationPropertiesBinder
         */
        @JvmStatic
        fun get(beanFactory: BeanFactory): ConfigurationPropertiesBinder {
            return beanFactory.getBean(BEAN_NAME, ConfigurationPropertiesBinder::class.java)!!
        }
    }

    /**
     * 对于没有完成实例化的Bean，那么使用构造器去进行实例化并完成属性的设置
     */
    open fun bindOrCreate(bean: ConfigurationPropertiesBean): Any {
        val bindMethod = bean.getBindMethod()
        val beanType = bean.getBeanType()
        // 获取@ConstructorBinding注解匹配的构造器
        val bindConstructor = ConfigurationPropertiesBindConstructorProvider.INSTANCE.getBindConstructor(beanType)!!
        val parameters = bindConstructor.parameters
        return Any()
    }

    /**
     * 对已经完成实例化的Bean，去完成ConfigurationProperties的绑定工作
     */
    open fun bind(bean: ConfigurationPropertiesBean) {
        val annotation = bean.getAnnotation()
        val instance = bean.getInstance()
        val environment = environment!!
        if (instance != null) {
            val prefix = annotation.prefix
            val clazz = instance::class.java
            clazz.declaredFields.forEach {
                val name = it.name  // 字段名
                val property = environment.getProperty("${prefix}.${name}")

                // 获取setter的方法
                val setMethodName = "set" + name[0].uppercaseChar() + name.substring(1)
                try {
                    val setterMethod = clazz.getDeclaredMethod(setMethodName, it.type)
                    val conversionService = environment.getConversionService()

                    if (conversionService.canConvert(String::class.java, it.type)) {
                        // 反射执行目标方法
                        ReflectionUtils.makeAccessiable(setterMethod)
                        ReflectionUtils.invokeMethod(
                            setterMethod,
                            instance,
                            conversionService.convert(property, it.type)
                        )
                    }
                } catch (_: java.lang.reflect.InvocationTargetException) {

                } catch (_: java.lang.IllegalArgumentException) {

                }
            }
        }
    }

}