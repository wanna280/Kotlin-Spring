package com.wanna.framework.web.context

import com.wanna.framework.lang.Nullable
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 * 可以支持去进行配置的[WebApplicationContext]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/13
 *
 * @see WebApplicationContext
 */
interface ConfigurableWebApplicationContext : WebApplicationContext {

    companion object {
        /**
         * ServletContext的beanName
         */
        const val SERVLET_CONFIG_BEAN_NAME = "servletContext"
    }


    /**
     * set ServletContext
     *
     * @param servletContext ServletContext
     */
    fun setServletContext(@Nullable servletContext: ServletContext?)

    /**
     * set ServletConfig
     *
     * @param servletConfig ServletConfig
     */
    fun setServletConfig(@Nullable servletConfig: ServletConfig?)

    /**
     * get ServletConfig
     *
     * @return ServletConfig
     */
    @Nullable
    fun getServletConfig(): ServletConfig?
}