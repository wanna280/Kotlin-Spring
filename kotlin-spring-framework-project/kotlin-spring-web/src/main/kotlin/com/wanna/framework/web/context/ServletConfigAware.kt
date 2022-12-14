package com.wanna.framework.web.context

import com.wanna.framework.context.aware.Aware
import javax.servlet.ServletConfig

/**
 * 处理ServletConfig的自动注入的Aware接口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/14
 */
fun interface ServletConfigAware : Aware {

    /**
     * 注入ServletConfig
     *
     * @param servletConfig ServletConfig
     */
    fun setServletConfig(servletConfig: ServletConfig)
}