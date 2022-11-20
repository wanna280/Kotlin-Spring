package com.wanna.nacos.api.config.filter

import com.wanna.nacos.api.config.filter.IConfigContext

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/14
 */
interface IConfigResponse {

    /**
     * 获取参数
     */
    fun getParameter(name: String): Any?

    /**
     * 添加参数
     */
    fun putParameter(name: String, value: Any?)

    /**
     * 获取ConfigContext
     */
    fun getConfigContext(): IConfigContext
}