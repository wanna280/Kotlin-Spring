package com.wanna.framework.web.client

import com.wanna.framework.web.http.client.ClientHttpRequest

/**
 * 它是一个客户端请求的Callback；
 * 对于RestTemplate的客户端Http请求来说，具体的流程如下：
 * * 1.创建ClientHttpClient，并交给拦截器去拦截：
 * * 2.使用RequestCallback去进行干预；
 * * 3.执行目标请求，获取ClientHttpResponse
 * * 4.MessageConverter处理返回值类型
 *
 * 而RequestCallback，会在第二步当中作为一个回调，支持在执行目标请求之前，对request再去进行一次干预
 *
 * @see ClientHttpRequest
 * @see RestTemplate
 */
@FunctionalInterface
interface RequestCallback {
    fun doWithRequest(request: ClientHttpRequest)
}