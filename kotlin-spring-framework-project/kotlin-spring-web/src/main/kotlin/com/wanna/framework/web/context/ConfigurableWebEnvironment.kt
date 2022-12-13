package com.wanna.framework.web.context

import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.lang.Nullable
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 * 可以支持去进行配置的WebEnvironment
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 *
 * @see com.wanna.framework.web.context.support.StandardServletEnvironment
 */
interface ConfigurableWebEnvironment : ConfigurableEnvironment {

    /**
     * 根据ServletContext/ServletConfig去初始化PropertySources
     *
     * @param servletContext ServletContext
     * @param servletConfig ServletConfig
     */
    fun initPropertySources(@Nullable servletContext: ServletContext?, @Nullable servletConfig: ServletConfig?)
}