package com.wanna.boot.web.client

import com.wanna.framework.web.client.ClientHttpRequestInitializer
import com.wanna.framework.web.http.client.ClientHttpRequest

/**
 * 对[ClientHttpRequest]去进行初始化的初始化器实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @param requestCustomizers 要对[ClientHttpRequest]去进行自定义的自定义化器
 */
class RestTemplateBuilderClientHttpRequestInitializer(private val requestCustomizers: Set<RestTemplateRequestCustomizer<*>>) :
    ClientHttpRequestInitializer {

    override fun initialize(request: ClientHttpRequest) {
        requestCustomizers.forEach { it.customize(request) }
    }
}