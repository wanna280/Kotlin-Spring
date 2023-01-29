package com.wanna.framework.context.annotation

import com.wanna.framework.aop.framework.AopInfrastructureBean
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.event.EventListenerFactory
import com.wanna.framework.beans.factory.config.BeanPostProcessor
import com.wanna.framework.beans.factory.config.BeanFactoryPostProcessor
import com.wanna.framework.context.processor.factory.internal.ConfigurationClassPostProcessor
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.lang.Nullable

/**
 * 这是一个ConfigurationClass的配置类, 在ConfigurationClassPostProcessor扫描时,
 * 会将一个类包装成为一个ConfigurationClass(配置类), 这是一个提供ConfigurationClass的支持的相关工具类
 *
 * @see ConfigurationClass
 * @see ConfigurationClassParser
 * @see ConfigurationClassPostProcessor
 */
object ConfigurationClassUtils {

    // 配置类的候选匹配注解, 只要一个类上标注了这些类, 那么它就是有资格成为一个候选的配置类
    private val candidateIndicators = setOf(
        Import::class.java.name,
        Component::class.java.name,
        ImportResource::class.java.name,
        PropertySource::class.java.name
    )

    // 配置类的属性, 可以从BeanDefinition当中获取到该属性, 用来判断该配置类是否被处理过！
    val CONFIGURATION_CLASS_ATTRIBUTE = ConfigurationClassPostProcessor::class.java.name + ".configurationClass"
    const val CONFIGURATION_CLASS_FULL = "full"  // full, 全配置类(@Configuration & proxyBeanMethods=true)
    const val CONFIGURATION_CLASS_LITE = "lite"  // lite, 半配置类

    // 一个配置类上的@Order解析完成的属性, 可以支持对配置类去进行排序
    val ORDER_ATTRIBUTE = ConfigurationClassPostProcessor::class.java.name + ".order"


    /**
     * 检查一个给定的BeanDefinition是否有资格成为一个配置类?
     *
     * * 1.类上标注了@Import/@ImportResource/@PropertySource/@ComponentScan注解, return true;
     * * 2.当中包含了@Bean的方法, return true
     * * 3.Spring的基础设施Bean, 包括BeanFactoryPostProcessor/BeanPostProcessor等, return false
     * * 4.别的情况, 一律return false
     *
     * 与此同时, 还会做以下的功能：
     * * 1.检查一个配置类是帮配置类, 还是全配置类? 将属性设置到BeanDefinition的属性当中, 方便后续去进行检查是否被检查过
     *     * 1.1如果它标注了@Configuration注解, 并且proxyBeanMethods=true, 那么它是一个全配置类(FULL)
     *     * 1.2如果它是一个合格的配置类, 比如类上标注了@Import/@ImportSource注解的去了, 它是一个半配置类(LITE)
     * * 2.如果一个类上有@Order注解, 那么将它的order属性设置到BeanDefinition的属性当中, 外部可能会需要用到(用来排序)
     *
     * @param beanDefinition 你要去进行检查的BeanDefinition
     * @return 如果有资格, 那么return true; 如果没有资格, return false
     */
    @JvmStatic
    fun checkConfigurationClassCandidate(beanDefinition: BeanDefinition): Boolean {
        if (beanDefinition.getBeanClassName() == null || beanDefinition.getFactoryMethodName() != null) {
            return false
        }
        var metadata: AnnotationMetadata? = null
        // 如果解析出来的配置类是一个AnnotatedBeanDefinition的话, 那么直接获取之前解析的metadata
        if (beanDefinition is AnnotatedBeanDefinition) {
            metadata = beanDefinition.getMetadata()

            // 如果它不是一个AnnotatedBeanDefinition, 那么需要把beanClass包装成为一个metadata
        } else if (beanDefinition is AbstractBeanDefinition && beanDefinition.hasBeanClass()) {
            val beanClass = beanDefinition.getBeanClass()!!

            // 如果是一些Spring的基础设施的Bean, 那么直接pass掉, 它不应该去成为一个候选的配置类
            if (ClassUtils.isAssignFrom(BeanFactoryPostProcessor::class.java, beanClass) || ClassUtils.isAssignFrom(
                    BeanPostProcessor::class.java,
                    beanClass
                ) || ClassUtils.isAssignFrom(AopInfrastructureBean::class.java, beanClass) || ClassUtils.isAssignFrom(
                    EventListenerFactory::class.java,
                    beanClass
                )
            ) {
                return false
            }
            metadata = AnnotationMetadata.introspect(beanClass)  // 把beanClass包装成为一个metadata
        }

        // 检查类上的@Configuration注解, 判断proxyBeanMethods是否为true
        if (metadata != null) {
            val attributes = metadata.getAnnotations().get(Configuration::class.java)

            // 如果有@Configuration注解, 并且proxyBeanMethods=true, 那么说明它是一个全配置类
            if (attributes.present && attributes.getBoolean("proxyBeanMethods")) {
                beanDefinition.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL)

                // 如果它有@Import/@ImportResource/@Component/@Configuration/@PropertySource, 说明它是一个半配置类
            } else if (attributes.present || isConfigurationCandidate(metadata)) {  // fixed: 条件应该使用&, 而不是||
                beanDefinition.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE)
            } else {
                return false
            }
            // 如果它是一个配置类的话, 那么尝试去解析它的@Order注解, 放入到BeanDefinition的属性当中, 方便后续去进行获取
            val order = getOrder(metadata)
            if (order != null) {
                beanDefinition.setAttribute(ORDER_ATTRIBUTE, order)
            }
        }
        return true
    }

    /**
     * 根据注解元信息, 去判断判断一个类, 是否有资格成为Spring的一个的配置类?
     * 只要有Spring当中的一些标志性注解, 那么它就有机会成为一个候选的配置类
     *
     * @param metadata 目标配置类的相关注解信息
     * @return 如果有资格, return true; 否则return false
     */
    @JvmStatic
    fun isConfigurationCandidate(metadata: AnnotationMetadata): Boolean {
        // 1.如果它是一个接口, 那么肯定没资格成为一个配置类, return false
        if (metadata.isInterface()) {
            return false
        }
        // 2.匹配它是否是一个配置类(检查@Import/@ImportResource/@PropertySource/@ComponentScan注解)
        candidateIndicators.forEach {
            if (metadata.isAnnotated(it)) {
                return true
            }
        }
        // 3.检查它身上是否有@Bean注解? 如果有的话, return true, 否则return false
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
     * @return 如果找到了Order属性, 那么return order; 否则return Ordered.LOWEST
     */
    @JvmStatic
    fun getOrder(beanDefinition: BeanDefinition): Int {
        return beanDefinition.getAttribute(ORDER_ATTRIBUTE) as Int? ?: Ordered.ORDER_LOWEST
    }

    /**
     * 从AnnotationMetadata当中去获取到@Order当中的value属性
     *
     * @param metadata AnnotationMetadata
     * @return 从metadata当中找到的order属性, 如果没有找到return null
     */
    @JvmStatic
    @Nullable
    fun getOrder(metadata: AnnotationMetadata): Int? {
        val orderMergedAnnotation = metadata.getAnnotations().get(Order::class.java)
        if (!orderMergedAnnotation.present) {
            return null
        }
        return orderMergedAnnotation.getInt("value")
    }
}