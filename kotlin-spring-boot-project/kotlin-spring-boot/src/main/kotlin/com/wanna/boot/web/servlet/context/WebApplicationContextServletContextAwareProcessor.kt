package com.wanna.boot.web.servlet.context

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.context.ConfigurableWebApplicationContext
import com.wanna.framework.web.context.support.ServletContextAwareProcessor
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 * WebApplicationContext场景下的ServletContextAwareProcessor, 因为WebApplicationContext当中可以获取到ServletContext/ServletConfig
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 *
 * @param webApplicationContext WebApplicationContext
 *
 * @see ServletContext
 * @see ServletConfig
 * @see ServletContextAwareProcessor
 */
open class WebApplicationContextServletContextAwareProcessor(private val webApplicationContext: ConfigurableWebApplicationContext) :
    ServletContextAwareProcessor() {

    /**
     * 获取ServletConfig
     *
     * @return ServletConfig
     */
    @Nullable
    override fun getServletConfig(): ServletConfig? =
        this.webApplicationContext.getServletConfig() ?: super.getServletConfig()

    /**
     * 获取ServletContext
     *
     * @return ServletContext
     */
    @Nullable
    override fun getServletContext(): ServletContext? =
        this.webApplicationContext.getServletContext() ?: super.getServletContext()
}