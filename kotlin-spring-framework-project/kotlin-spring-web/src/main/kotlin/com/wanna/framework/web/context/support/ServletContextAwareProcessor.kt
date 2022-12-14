package com.wanna.framework.web.context.support

import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.context.ServletConfigAware
import com.wanna.framework.web.context.ServletContextAware
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 * [ServletContextAware]和[ServletConfigAware]这两个Aware接口的处理器, 对于Servlet应用去提供ServletContext的自动注入
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 *
 * @param servletContext ServletContext
 * @param servletConfig ServletConfig
 *
 * @see ServletContextAware
 * @see ServletConfigAware
 */
open class ServletContextAwareProcessor(
    @Nullable private val servletContext: ServletContext? = null,
    @Nullable private val servletConfig: ServletConfig? = null
) : BeanPostProcessor {

    /**
     * 支持使用无参数构造器去进行创建
     */
    constructor() : this(null, null)

    /**
     * 提供一个ServletContext的构造器
     *
     * @param servletContext ServletContext
     */
    constructor(@Nullable servletContext: ServletContext?) : this(servletContext, null)

    /**
     * 提供一个ServletConfig的构造器
     *
     * @param servletConfig ServletConfig
     */
    constructor(@Nullable servletConfig: ServletConfig?) : this(null, servletConfig)

    /**
     * 获取ServletConfig, 支持子类去进行重写
     *
     * @return ServletConfig(可能为null)
     */
    @Nullable
    protected open fun getServletConfig(): ServletConfig? {
        return this.servletConfig
    }

    /**
     * 获取ServletContext, 支持子类去进行重写
     *
     * @return ServletContext(可能为null)
     */
    @Nullable
    protected open fun getServletContext(): ServletContext? {
        // 检查一下ServletConfig是否为null? 因为ServletConfig也可以获取到ServletContext
        if (this.servletContext == null && getServletConfig() != null) {
            return getServletConfig()?.servletContext
        }
        return this.servletContext
    }

    override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any? {
        if (bean is ServletContextAware && servletContext != null) {
            bean.setServletContext(servletContext)
        }
        if (bean is ServletConfigAware && this.servletConfig != null) {
            bean.setServletConfig(servletConfig)
        }
        return bean
    }
}