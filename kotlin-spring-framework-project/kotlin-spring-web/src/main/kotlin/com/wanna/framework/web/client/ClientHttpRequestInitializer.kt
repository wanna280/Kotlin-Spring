package com.wanna.framework.web.client

import com.wanna.framework.web.http.client.ClientHttpRequest

/**
 * 对[ClientHttpRequest]去进行初始化的初始化器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 */
fun interface ClientHttpRequestInitializer {

    /**
     * 对[ClientHttpRequest]去进行自定义
     *
     * @param request request
     */
    fun initialize(request: ClientHttpRequest)
}