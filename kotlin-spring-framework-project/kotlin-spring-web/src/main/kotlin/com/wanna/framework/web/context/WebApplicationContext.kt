package com.wanna.framework.web.context

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.lang.Nullable
import javax.servlet.ServletContext

/**
 * Servlet容器的ApplicationContext
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/13
 */
interface WebApplicationContext : ApplicationContext {

    companion object {

        /**
         * ServletContext的beanName
         */
        const val SERVLET_CONTEXT_BEAN_NAME = "servletContext"

        /**
         * ServletContext的ContextParameter的beanName
         */
        const val CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters"

        /**
         * ServletContext的ContextAttributes的beanName
         */
        const val CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes"

        /**
         * SpringMVC的Root ApplicationContext的属性, 在父子容器分离的情况下, 就需要有一个Root容器去存放Controller的Bean
         *
         * @see ServletContext.getAttribute
         */
        const val ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = "com.wanna.framework.web.context.WebApplicationContext.ROOT"
    }


    /**
     * 获取到当前Servlet应用的ServletContext
     *
     * @return ServletContext(or null)
     */
    @Nullable
    fun getServletContext(): ServletContext?
}