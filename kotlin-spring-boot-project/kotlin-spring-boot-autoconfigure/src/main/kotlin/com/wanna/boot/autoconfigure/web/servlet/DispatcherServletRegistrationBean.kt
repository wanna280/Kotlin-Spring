package com.wanna.boot.autoconfigure.web.servlet

import com.wanna.boot.web.servlet.ServletRegistrationBean
import com.wanna.framework.web.server.servlet.DispatcherServlet

/**
 * DispatcherServlet的RegistrationBean
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class DispatcherServletRegistrationBean(val path: String, dispatcherServlet: DispatcherServlet) :
    ServletRegistrationBean<DispatcherServlet>(dispatcherServlet)