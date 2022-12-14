package com.wanna.framework.web.context.support

import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.context.ConfigurableWebEnvironment
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 * 标准的Servlet容器下的Environment的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 */
open class StandardServletEnvironment : StandardEnvironment(), ConfigurableWebEnvironment {
    companion object {

        /**
         * ServletContext的initParameters组成的PropertySource的name
         */
        const val SERVLET_CONTEXT_PROPERTY_SOURCE_NAME = "servletContextInitParams"

        /**
         * ServletConfig的initParameters组成的PropertySource的name
         */
        const val SERVLET_CONFIG_PROPERTY_SOURCE_NAME = "servletConfigInitParams"
    }

    /**
     * 根据ServletContext/ServletConfig去初始化PropertySources(将对应的initParameters各添加成为一个PropertySource)
     *
     * @param servletContext ServletContext
     * @param servletConfig ServletConfig
     */
    override fun initPropertySources(
        @Nullable servletContext: ServletContext?,
        @Nullable servletConfig: ServletConfig?
    ) {
        WebApplicationContextUtils.initServletPropertySources(getPropertySources(), servletContext, servletConfig)
    }
}