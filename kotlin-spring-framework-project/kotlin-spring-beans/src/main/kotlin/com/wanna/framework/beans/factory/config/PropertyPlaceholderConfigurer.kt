package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.beans.factory.config.BeanFactoryPostProcessor
import com.wanna.framework.util.PropertyPlaceholderHelper
import java.util.*

/**
 * 基于Property的方式去进行真正提供占位符解析的[BeanFactoryPostProcessor]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/22
 */
open class PropertyPlaceholderConfigurer : PlaceholderConfigurerSupport() {

    companion object {
        /**
         * 永远不要使用SystemProperties去作为占位符解析时提供的属性值
         */
        const val SYSTEM_PROPERTIES_MODE_NEVER = 0

        /**
         * 对于SystemProperties的属性值去fallback的占位符解析时提供的属性值
         */
        const val SYSTEM_PROPERTIES_MODE_FALLBACK = 1

        /**
         * 使用SystemProperties的属性值去作为优先的占位符解析时提供的属性值
         */
        const val SYSTEM_PROPERTIES_MODE_OVERRIDE = 2
    }


    /**
     * SystemProperties的Mode
     */
    var systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK

    /**
     * 实现父类的processProperties方法, 在postProcessBeanFactory时, 会被自动回调到
     *
     * @param beanFactory BeanFactory
     * @param properties Properties
     */
    override fun processProperties(beanFactory: ConfigurableListableBeanFactory, properties: Properties) {
        // 创建一个提供占位符的解析的StringValueResolver
        val valueResolver = PlaceholderResolvingStringValueResolver(properties)

        // doProcessProperties, 使用StringValueResolver去解析BeanDefinition当中的占位符
        // 并将该StringValueResolver去添加到BeanFactory当中
        doProcessProperties(beanFactory, valueResolver)
    }

    /**
     * 从SystemProperties&给定的Properties当中去解析占位符
     *
     * @param placeholder 占位符的属性名
     * @param properties Properties
     * @param systemPropertiesMode 对于SystemProperties, 应该怎么去进行使用? override/fallback/never
     * @return 占位符解析到的属性值
     */
    protected open fun resolvePlaceholder(
        placeholder: String?,
        properties: Properties,
        systemPropertiesMode: Int
    ): String? {
        placeholder ?: return null
        var propertyValue: String? = nullValue
        if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
            propertyValue = resolveSystemProperty(placeholder)
        }
        if (propertyValue == null) {
            propertyValue = resolvePlaceholder(placeholder, properties)
        }

        if (propertyValue == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
            propertyValue = resolveSystemProperty(placeholder)
        }

        return propertyValue
    }

    /**
     * 从给定的Properties当中去提供属性值, 供解析占位符使用
     *
     * @param placeholder 占位符的属性名
     * @param properties 提供占位符的属性值获取的Map
     * @return 从Properties当中去获取到的属性值(解析不到return null)
     */
    protected open fun resolvePlaceholder(placeholder: String, properties: Properties): String? {
        return properties[placeholder]?.toString()
    }

    /**
     * 使用SystemProperties去作为占位符属性值的来源
     *
     * @param key propertyKey
     * @return propertyValue
     */
    protected open fun resolveSystemProperty(key: String): String? {
        var propertyValue = System.getProperty(key)
        if (propertyValue == null) {
            propertyValue = System.getenv(key)
        }
        return propertyValue
    }

    /**
     * 提供占位符的解析的[StringValueResolver]
     */
    private inner class PlaceholderResolvingStringValueResolver(properties: Properties) :
        StringValueResolver {
        /**
         * 提供占位符解析的Helper工具类
         */
        private val helper = PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix, valueSeparator)

        /**
         * 提供Placeholder的解析的Resolver
         */
        private val resolver = PropertyPlaceholderConfigurerResolver(properties)

        /**
         * 真正地去解析字符串的值
         *
         * @param strVal 原始的字符串的值
         * @return 解析得到的字符串的值
         */
        override fun resolveStringValue(strVal: String): String? {
            var resolved: String? = helper.replacePlaceholder(strVal, resolver)
            if (trimValues) {
                resolved = resolved?.trim()
            }
            // 如果它的值和配置的默认的nullValue一致, 那么就得return null
            return if (nullValue == resolved) null else resolved
        }
    }

    /**
     * PlaceholderResolver, 提供对于获取属性的Callback方法, 供[PropertyPlaceholderHelper]使用
     */
    private inner class PropertyPlaceholderConfigurerResolver(private val properties: Properties) :
        PropertyPlaceholderHelper.PlaceholderResolver {
        override fun resolvePlaceholder(text: String) =
            this@PropertyPlaceholderConfigurer.resolvePlaceholder(text, properties, systemPropertiesMode)
    }
}