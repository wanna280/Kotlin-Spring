package com.wanna.framework.web.context.support

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.core.environment.MutablePropertySources
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.context.ConfigurableWebApplicationContext
import com.wanna.framework.web.context.WebApplicationContext
import java.util.*
import javax.servlet.ServletConfig
import javax.servlet.ServletContext
import kotlin.collections.LinkedHashMap
import kotlin.jvm.Throws

/**
 * WebApplicationContext的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 */
object WebApplicationContextUtils {

    /**
     * 将ServletContext环境相关的Bean去注册到BeanFactory当中去
     *
     * @param beanFactory BeanFactory
     * @param servletContext ServletContext
     */
    @JvmStatic
    fun registerEnvironmentBeans(
        beanFactory: ConfigurableListableBeanFactory,
        @Nullable servletContext: ServletContext?
    ) = registerEnvironmentBeans(beanFactory, servletContext, null)

    /**
     * 将ServletContext/ServletConfig环境相关的Bean去注册到BeanFactory当中去
     *
     * * 1.注册ServletContext(如果ServletContext不为null)
     * * 2.注册ServletConfig(如果ServletConfig不为null)
     * * 3.注册"contextParameters"(initParameters of ServletContext&ServletConfig)
     * * 4.注册"contextAttributes"(ServletContext)
     *
     * @param beanFactory BeanFactory
     * @param servletContext ServletContext
     * @param servletConfig  ServletConfig
     */
    @JvmStatic
    fun registerEnvironmentBeans(
        beanFactory: ConfigurableListableBeanFactory,
        @Nullable servletContext: ServletContext?,
        @Nullable servletConfig: ServletConfig?
    ) {

        // 1.register ServletContext if necessary
        if (servletContext != null && !beanFactory.containsBean(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME)) {
            beanFactory.registerSingleton(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME, servletContext)
        }

        // 2.register ServletConfig if necessary
        if (servletConfig != null && !beanFactory.containsBean(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME)) {
            beanFactory.registerSingleton(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME, servletConfig)
        }

        // 3.register ContextParameters if necessary
        if (!beanFactory.containsBean(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME)) {
            val paramMap = LinkedHashMap<String, String>()

            // add ServletContext initParameterNames
            servletContext?.initParameterNames?.toList()?.forEach { paramMap[it] = servletContext.getInitParameter(it) }
            // add ServletConfig initParameters
            servletConfig?.initParameterNames?.toList()?.forEach { paramMap[it] = servletConfig.getInitParameter(it) }

            // register ContextParameters Bean
            beanFactory.registerSingleton(
                WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME,
                Collections.unmodifiableMap(paramMap)
            )
        }

        // 4.register ContextAttributes if necessary
        if (!beanFactory.containsBean(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME)) {
            val attributeMap = LinkedHashMap<String, Any>()
            servletContext?.attributeNames?.toList()?.forEach { attributeMap[it] = servletContext.getAttribute(it) }

            // register ContextAttributes Bean
            beanFactory.registerSingleton(
                WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME,
                Collections.unmodifiableMap(attributeMap)
            )
        }
    }

    /**
     * 根据ServletContext/ServletConfig去初始化PropertySources
     *
     * @param sources PropertySources
     * @param servletContext ServletContext
     * @param servletConfig ServletConfig
     */
    @JvmStatic
    fun initServletPropertySources(
        sources: MutablePropertySources,
        @Nullable servletContext: ServletContext?,
        @Nullable servletConfig: ServletConfig?
    ) {
        // servletContext PropertySource
        val servletContextPropertySourceName = StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME
        if (servletContext != null && !sources.contains(servletContextPropertySourceName)) {
            sources.addLast(ServletContextPropertySource(servletContextPropertySourceName, servletContext))
        }

        // servletConfig PropertySource
        val servletConfigPropertySourceName = StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME
        if (servletConfig != null && !sources.contains(servletConfigPropertySourceName)) {
            sources.addLast(ServletConfigPropertySource(servletConfigPropertySourceName, servletConfig))
        }
    }

    /**
     * 从ServletContext当中获取到WebApplicationContext
     *
     * @param servletContext ServletContext
     * @return WebApplicationContext
     */
    @Nullable
    @Throws(RuntimeException::class)
    @JvmStatic
    fun getWebApplicationContext(servletContext: ServletContext): WebApplicationContext? =
        getWebApplicationContext(servletContext, WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)

    /**
     * 从ServletContext当中根据给定的属性名去获取到WebApplicationContext
     *
     * @param servletContext ServletContext
     * @param name name
     * @return WebApplicationContext
     */
    @Nullable
    @Throws(RuntimeException::class)
    @JvmStatic
    fun getWebApplicationContext(servletContext: ServletContext, name: String): WebApplicationContext? {
        val attribute = servletContext.getAttribute(name) ?: return null

        // 如果属性当中的值的是一个异常的话, 丢出去
        if (attribute is RuntimeException) {
            throw attribute
        }
        if (attribute is Error) {
            throw attribute
        }
        if (attribute is Exception) {
            throw IllegalStateException(attribute)
        }

        // 检查是否是一个WebApplicationContext
        if (attribute !is WebApplicationContext) {
            throw IllegalStateException("Context attribute is not of type WebApplicationContext: $name")
        }
        return attribute
    }
}