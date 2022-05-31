package com.wanna.framework.web.http.client

import com.wanna.framework.web.bind.RequestMethod
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import java.io.ByteArrayOutputStream
import java.net.URI

/**
 * 带有拦截功能的ClientHttpRequestFactory，内部聚合了拦截器列表，可以对请求去进行自定义
 *
 * Note: 实现基于注册发现的负载均衡时，就可以拦截request的创建，去替换掉serviceName的url成为真正的url
 *
 * @param requestFactory RequestFactory
 * @param interceptors 拦截器列表
 */
class InterceptingClientHttpRequestFactory(
    private val requestFactory: ClientHttpRequestFactory,
    private val interceptors: List<ClientHttpRequestInterceptor>
) : ClientHttpRequestFactory {

    override fun create(url: URI, method: RequestMethod): ClientHttpRequest {
        return requestFactory.create(url, method)
    }
}