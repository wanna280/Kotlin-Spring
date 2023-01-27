package com.wanna.boot.web.client

import com.wanna.framework.web.http.client.ClientHttpRequest

/**
 * 对[ClientHttpRequest]去进行自定义的自定义化器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 */
@FunctionalInterface
fun interface RestTemplateRequestCustomizer<T : ClientHttpRequest> {

    /**
     * 执行对于[ClientHttpRequest]的自定义
     *
     * @param request request
     */
    fun customize(request: ClientHttpRequest)
}