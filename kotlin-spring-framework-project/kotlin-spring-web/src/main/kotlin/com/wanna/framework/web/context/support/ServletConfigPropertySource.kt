package com.wanna.framework.web.context.support

import com.wanna.framework.core.environment.EnumerablePropertySource
import com.wanna.framework.lang.Nullable
import javax.servlet.ServletConfig

/**
 * ServletConfig的initParameters的PropertySource
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 *
 * @param name name
 * @param servletConfig ServletConfig
 */
open class ServletConfigPropertySource(name: String, servletConfig: ServletConfig) :
    EnumerablePropertySource<ServletConfig>(name, servletConfig) {

    /**
     * 获取ServletConfig当中的所有的initParameters
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