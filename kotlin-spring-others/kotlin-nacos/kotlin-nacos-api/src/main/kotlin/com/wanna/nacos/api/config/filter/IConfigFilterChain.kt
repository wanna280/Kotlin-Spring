package com.wanna.nacos.api.config.filter

import com.wanna.nacos.api.exception.NacosException
import kotlin.jvm.Throws

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/14
 */
interface IConfigFilterChain {

    @Throws(NacosException::class)
    fun doFilter(request: IConfigRequest, response: IConfigResponse)
}