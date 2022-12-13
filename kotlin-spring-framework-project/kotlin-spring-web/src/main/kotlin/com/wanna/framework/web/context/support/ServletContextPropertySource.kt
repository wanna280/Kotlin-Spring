package com.wanna.framework.web.context.support

import com.wanna.framework.core.environment.EnumerablePropertySource
import com.wanna.framework.lang.Nullable
import javax.servlet.ServletContext

/**
 * ServletContext的initParameters的PropertySource
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 *
 * @param name name
 * @param servletContext ServletContext
 */
open class ServletContextPropertySource(name: String, servletContext: ServletContext) :
    EnumerablePropertySource<ServletContext>(name, servletContext) {

    /**
     * 获取ServletContext当中的所有的initParameters
     *
     * @return initParameter names
     */
    override fun getPropertyNames(): Array<String> = source.initParameterNames.toList().toTypedArray()

    /**
     * 根据属性名去获取到Property
     *
     * @param name name
     * @return propertyValue
     */
    @Nullable
    override fun getProperty(name: String): Any? = source.getInitParameter(name)
}