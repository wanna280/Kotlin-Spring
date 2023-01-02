package com.wanna.boot.context.properties

import com.wanna.boot.context.properties.ConfigurationPropertiesBean.BindMethod
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable

/**
 * 这是一个ConfigurationPropertiesBean的Registrar, 负责将@EnableConfigurationProperties的注解当中配置的组件注册到容器当中；
 * 在@EnableConfigurationProperties注解当中的value属性当中配置的所有的类, 都会被注册到BeanDefinitionRegistry当中
 *
 * @see EnableConfigurationPropertiesRegistrar
 * @see EnableConfigurationProperties.value
 * @see ConfigurationProperties.prefix
 *
 * @param registry 提供BeanDefinition的注册的BeanDefinitionRegistry
 */
open class ConfigurationPropertiesBeanRegistrar(private val registry: BeanDefinitionRegistry) {

    /**
     * BeanFactory
     */
    private val beanFactory: BeanFactory = registry as BeanFactory

    /**
     * 将指定的类以配置类的方式去注册到容器当中, 自动从类上去寻找[ConfigurationProperties]注解中去配置的相关属性去完成注册
     *
     * @param type 要去进行注册到SpringBeanFactory当中的配置类
     */
    open fun register(type: Class<*>) {
        register(type, getConfigurationPropertiesAnnotationAttributes(type))
    }

    /**
     * 注册一个ConfigurationPropertiesBean到容器当中
     *
     * @param type 要注册的class
     * @param attributes 类或者方法上的@ConfigurationProperties注解的属性(可以为空)
     */
    open fun register(type: Class<*>, @Nullable attributes: AnnotationAttributes?) {
        // 生成beanName
        val name = getName(type, attributes)

        // 创建合适类型的BeanDefinition注册BeanDefinition到容器当中
        registry.registerBeanDefinition(name, createBeanDefinition(name, type))
    }

    /**
     * 根据BindMethod的不同, 创建匹配的BeanDefinition去提供支持
     *
     * @param name beanName
     * @param type beanType
     */
    private fun createBeanDefinition(name: String, type: Class<*>): BeanDefinition {
        // 如果绑定方式是ValueObject, 说明需要使用构造器绑定的方式去进行绑定, 需要创建支持ValueObject的BeanDefinition
        if (BindMethod.forType(type) == BindMethod.VALUE_OBJECT) {
            return ConfigurationPropertiesValueObjectBeanDefinition(beanFactory, type, name)
        }
        // 如果绑定方式是JavaBean, 那么说明需要使用JavaBean的setter的方式去进行绑定, 使用普通的方式去进行绑定即可
        // 等待PostProcessor对该Bean去进行干预时, 自动去完成setter的自动注入即可
        return GenericBeanDefinition(type)
    }

    /**
     * 获取@ConfigurationProperties的属性, 并封装为AnnotationAttributes
     *
     * @param type 要进行寻找的目标类
     * @return 如果类上找到了注解信息, 那么return；没有找到则return null
     */
    @Nullable
    private fun getConfigurationPropertiesAnnotationAttributes(type: Class<*>): AnnotationAttributes? {
        return AnnotatedElementUtils.getMergedAnnotationAttributes(type, ConfigurationProperties::class.java)
    }

    /**
     * 获取beanName, 如果指定了前缀的话, 那么beanName为"{prefix}-{typeName}", 不然beanName为typeName
     *
     * @param type type
     * @param attributes @ConfigurationProperties注解的属性信息(可以为null)
     */
    private fun getName(type: Class<*>, @Nullable attributes: AnnotationAttributes?): String {
        var prefix: String? = null
        if (attributes != null) {
            prefix = attributes.getString("prefix")
        }
        return if (prefix != null && prefix.isNotBlank()) "${prefix}-${type.name}" else type.name
    }


}