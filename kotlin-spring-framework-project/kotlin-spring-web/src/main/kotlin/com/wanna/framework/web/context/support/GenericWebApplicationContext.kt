package com.wanna.framework.web.context.support

import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.support.GenericApplicationContext
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.context.ConfigurableWebApplicationContext
import com.wanna.framework.web.context.WebApplicationContext
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 * 通用的[WebApplicationContext]的实现, 在[GenericApplicationContext]的基础上, 新增[WebApplicationContext]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/13
 *
 * @see WebApplicationContext
 *
 * @param servletContext ServletContext
 * @param beanFactory DefaultListableBeanFactory
 */
open class GenericWebApplicationContext(
    @Nullable private var servletContext: ServletContext? = null,
    beanFactory: DefaultListableBeanFactory
) : GenericApplicationContext(beanFactory), ConfigurableWebApplicationContext {

    /**
     * 提供一个无参数构造器, 对于BeanFactory采用默认的BeanFactory
     */
    constructor() : this(null, DefaultListableBeanFactory())

    /**
     * 提供一个基于ServletContext的构造器
     *
     * @param servletContext ServletContext
     */
    constructor(@Nullable servletContext: ServletContext?) : this(servletContext, DefaultListableBeanFactory())

    /**
     * 提供一个基于BeanFactory的构造器
     *
     * @param beanFactory BeanFactory
     */
    constructor(beanFactory: DefaultListableBeanFactory) : this(null, beanFactory)


    /**
     * set ServletContext
     *
     * @param servletContext ServletContext
     */
    override fun setServletContext(@Nullable servletContext: ServletContext?) {
        this.servletContext = servletContext
    }

    /**
     * get ServletContext
     *
     * @return ServletContext
     */
    @Nullable
    override fun getServletContext(): ServletContext? = this.servletContext

    /**
     * set ServletConfig
     *
     * @param servletConfig ServletConfig
     */
    override fun setServletConfig(@Nullable servletConfig: ServletConfig?) {
        // no op
    }

    override fun getServletConfig(): ServletConfig? {
        throw UnsupportedOperationException("不支持去进行getServletConfig")
    }
}