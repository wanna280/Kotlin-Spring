package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.processor.factory.internal.ConfigurationClassPostProcessor
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个ConfigurationClass的配置类，在ConfigurationClassPostProcessor扫描时，会将一个类包装成为一个ConfigurationClass(配置类)，
 * 这是一个提供ConfigurationClass的相关工具类
 */
object ConfigurationClassUtils {

    // 配置类的候选匹配注解
    private val candidateIndicators = setOf(
        Configuration::class.java.name,
        Component::class.java.name,
        ImportSource::class.java.name,
        PropertySource::class.java.name
    )

    // 配置类的属性
    val CONFIGURATION_CLASS_ATTRIBUTE = ConfigurationClassPostProcessor::class.java.name + ".configurationClass"
    val ORDER_ATTRIBUTE = ConfigurationClassPostProcessor::class.java.name + ".order"
    const val CONFIGURATION_CLASS_FULL = "full"  // full，全配置类(@Configuration & proxyBeanMethods=true)
    const val CONFIGURATION_CLASS_LITE = "lite"  // lite，半配置类

    /**
     * 检查一个ConfigurationClass是否是候选的？
     */
    @JvmStatic
    fun checkConfigurationClassCandidate(beanDefinition: BeanDefinition): Boolean {
        if (beanDefinition.getBeanClass() == null || beanDefinition.getFactoryMethodName() != null) {
            return false
        }
        var metadata: AnnotationMetadata? = null
        if (beanDefinition is AnnotatedBeanDefinition) {
            metadata = beanDefinition.getMetadata()
        }

        // 检查类上的@Configuration注解，判断proxyBeanMethods是否为true
        if (metadata != null) {
            val attributes = metadata.getAnnotationAttributes(Configuration::class.java)
            val proxyBeanMethods = attributes["proxyBeanMethods"]

            // 如果有@Configuration注解，并且proxyBeanMethods=true，那么说明它是一个全配置类
            if (attributes.isNotEmpty() && proxyBeanMethods == true) {
                beanDefinition.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL)

                // 如果它有@ImportSource/@Component/@Configuration/@PropertySource，说明它是一个半配置类
            } else if (attributes.isNotEmpty() && isConfigurationCandidate(metadata)) {
                beanDefinition.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE)
            } else {
                return false
            }

            // 如果它是一个配置类的话，那么尝试去解析它的@Order注解，放入到BeanDefinition的属性当中，方便后续去进行获取
            val order = getOrder(metadata)
            if (order != null) {
                beanDefinition.setAttribute(ORDER_ATTRIBUTE, order)
            }
        }
        return true
    }

    /**
     * 判断它是否是一个候选的配置类？
     *
     * @param metadata 目标配置类的相关注解信息
     */
    @JvmStatic
    fun isConfigurationCandidate(metadata: AnnotationMetadata): Boolean {
        if (metadata.isInterface()) {
            return false
        }

        // 匹配它是否是一个配置类
        candidateIndicators.forEach {
            if (metadata.isAnnotated(it)) {
                return true
            }
        }
        return try {
            metadata.hasAnnotatedMethods(Bean::class.java.name)
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * 从BeanDefinition的属性当中去获取到Order属性
     *
     * @param beanDefinition 要获取Order的BeanDefinition
     * @return 如果找到了Order属性，那么return order；否则return Ordered>LOWEST
     */
    @JvmStatic
    fun getOrder(beanDefinition: BeanDefinition): Int {
        return beanDefinition.getAttribute(ORDER_ATTRIBUTE) as Int? ?: Ordered.ORDER_LOWEST
    }

    /**
     * 从AnnotationMetadata当中去获取到@Order当中的value属性
     *
     * @param metadata AnnotationMetadata
     * @return 找到的order属性，如果没有找到return null
     */
    @JvmStatic
    fun getOrder(metadata: AnnotationMetadata): Int? {
        val attributes = metadata.getAnnotationAttributes(Order::class.java.name)
        return attributes["value"] as Int?
    }
}