package com.wanna.boot.context.properties

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.ReflectionUtils
import org.slf4j.LoggerFactory
import java.lang.reflect.Field
import java.lang.reflect.Method

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
     *
     * @param bean 要去进行绑定的ConfigurationPropertiesBean
     */
    open fun bind(bean: ConfigurationPropertiesBean) {
        val annotation = bean.getAnnotation()
        val instance = bean.getInstance() ?: return
        val clazz = instance::class.java
        ReflectionUtils.doWithFields(clazz) {
            bindInternal(it, instance, clazz, annotation.prefix, bean)
        }
    }

    /**
     * 完成真正的绑定工作
     *
     * @param field 要去进行绑定的字段
     * @param instance 要去绑定的字段的实例对象
     * @param clazz clazz
     * @param prefix 字段要采用什么prefix去进行绑定
     */
    private fun bindInternal(
        field: Field,
        instance: Any,
        clazz: Class<*>,
        prefix: String,
        bean: ConfigurationPropertiesBean
    ) {
        val name = field.name  // 获取到字段名
        val fieldType = field.type

        // 支持原始的name，和"-"转换驼峰之后的propertyName两类propertyKey去进行尝试
        val propertyKeys = setOf("$prefix.$name", getFallbackPropertyName(prefix, name))
        propertyKeys.forEach { propertyKey ->
            // 1.判断是否有@NestedConfigurationProperty注解，如果有的话，需要去对该字段去进行递归处理
            if (AnnotatedElementUtils.isAnnotated(field, NestedConfigurationProperty::class.java)) {
                val getterMethod = getterMethod(clazz, name) ?: return@forEach
                ReflectionUtils.makeAccessible(getterMethod)

                // 如果该字段为空的话，需要使用无参数构造器去去创建一个空的对象
                val nestedFieldValue =
                    ReflectionUtils.invokeMethod(getterMethod, instance) ?: ClassUtils.newInstance(fieldType)
                // 递归处理该字段的所有内部字段
                ReflectionUtils.doWithFields(fieldType) {
                    bindInternal(it, nestedFieldValue, fieldType, propertyKey, bean)
                }

                // 将绑定好的字段的对象，使用setter去设置到外层对象的字段当中
                val setterMethod = setterMethod(clazz, name, fieldType) ?: return@forEach
                ReflectionUtils.makeAccessible(setterMethod)
                ReflectionUtils.invokeMethod(setterMethod, instance, nestedFieldValue)

                // 2.如果fieldType是简单类型/集合类型(List/Set/Collection)的话，那么尝试去进行类型的转换
            } else {
                val stringTypeDescriptor = TypeDescriptor.forClass(String::class.java)
                val fieldTypeDescriptor = TypeDescriptor.forField(field)
                if (BeanUtils.isSimpleProperty(fieldType)
                    || fieldType == Collection::class.java
                    || fieldType == List::class.java
                    || fieldType == Set::class.java
                ) {
                    val property = environment?.getProperty(propertyKey) ?: return@forEach
                    try {
                        // 获取setter的方法
                        val setterMethod = setterMethod(clazz, name, fieldType) ?: return@forEach
                        val conversionService = environment?.getConversionService() ?: return

                        if (conversionService.canConvert(stringTypeDescriptor, fieldTypeDescriptor)
                        ) {
                            val convertedValue = conversionService.convert(property, fieldTypeDescriptor)
                            // 反射执行目标方法
                            ReflectionUtils.makeAccessible(setterMethod)
                            ReflectionUtils.invokeMethod(setterMethod, instance, convertedValue)
                        } else {
                            throw ConfigurationPropertiesBindException("无法完成[$fieldType]的绑定", bean)
                        }
                    } catch (_: java.lang.reflect.InvocationTargetException) {

                    } catch (_: java.lang.IllegalArgumentException) {

                    } catch (_: NoSuchMethodException) {

                    }
                } else {

                }
            }
        }
    }

    private fun getFallbackPropertyName(prefix: String, fieldName: String): String {
        val builder = StringBuilder(prefix).append(".")
        fieldName.forEach {
            if (it.isUpperCase()) {
                builder.append("-").append(it.lowercase())
            } else {
                builder.append(it)
            }
        }
        return builder.toString()
    }

    private fun getterMethod(clazz: Class<*>, fieldName: String): Method? {
        // 获取getter的方法
        val getterMethodName = "get" + fieldName[0].uppercaseChar() + fieldName.substring(1)
        return ReflectionUtils.findMethod(clazz, getterMethodName)
    }

    private fun setterMethod(clazz: Class<*>, fieldName: String, fieldType: Class<*>): Method? {
        // 获取setter的方法
        val setMethodName = "set" + fieldName[0].uppercaseChar() + fieldName.substring(1)
        return ReflectionUtils.findMethod(clazz, setMethodName, fieldType)
    }

    companion object {
        @JvmField
        val BEAN_NAME: String = ConfigurationPropertiesBinder::class.java.name

        // logger
        private val logger = LoggerFactory.getLogger(ConfigurationProperties::class.java)

        /**
         * 给容器中注册ConfigurationPropertiesBinder的相关基础设施Bean
         *
         * @param registry BeanDefinitionRegistry
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
         *
         * @param beanFactory beanFactory
         * @return 获取到的ConfigurationPropertiesBinder
         */
        @JvmStatic
        fun get(beanFactory: BeanFactory): ConfigurationPropertiesBinder {
            return beanFactory.getBean(BEAN_NAME, ConfigurationPropertiesBinder::class.java)
        }
    }
}