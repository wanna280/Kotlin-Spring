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

    /**
     * 获取到当前Servlet应用的ServletContext
     *
     * @return ServletContext(or null)
     */
    @Nullable
    fun getServletContext(): ServletContext?
}