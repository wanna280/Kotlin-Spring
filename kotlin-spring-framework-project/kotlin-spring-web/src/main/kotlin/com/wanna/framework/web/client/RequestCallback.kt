package com.wanna.framework.web.client

import com.wanna.framework.web.http.client.ClientHttpRequest

/**
 * 它是一个客户端请求的Callback;
 * 对于RestTemplate的客户端Http请求来说, 具体的流程如下：
 * * 1.创建支持处理拦截器的处理的ClientHttpClient：
 * * 2.使用RequestCallback去进行干预;
 * * 3.交给拦截器拦截, 并执行目标请求, 获取ClientHttpResponse
 * * 4.MessageConverter处理返回值类型
 *
 * 而RequestCallback, 会在第二步当中作为一个回调, 支持在执行目标请求之前, 对request再去进行一次干预;
 * 比如可以在这里去添加RequestBody到request的输出流当中
 *
 * @see ClientHttpRequest
 * @see RestTemplate
 */
@FunctionalInterface
fun interface RequestCallback {
    fun doWithRequest(request: ClientHttpRequest)
}