package com.wanna.framework.web.http.client

import com.wanna.framework.util.StreamUtils
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import java.net.URI


/**
 * 支持拦截目标请求的ClientHttpRequest
 *
 * @param requestFactory 原始的RequestFactory
 * @param uri uri
 * @param method 请求方式(GET/POST等)
 * @param interceptors 要apply的拦截器列表
 */
class InterceptingClientHttpRequest(
    private val requestFactory: ClientHttpRequestFactory,
    private val uri: URI,
    private val method: RequestMethod,
    private val interceptors: List<ClientHttpRequestInterceptor>
) : AbstractBufferingClientHttpRequest() {
    override fun getMethod() = method
    override fun getUri() = uri

    /**
     * 真正执行目标方法, 创建一个拦截器链去执行目标方法, 先去apply所有的拦截器, 再去发送目标请求
     *
     * @param headers headers
     * @param bufferedOutput byteArray数组(要去进行写出的requestBody)
     */
    override fun executeInternal(headers: HttpHeaders, bufferedOutput: ByteArray): ClientHttpResponse {
        return InterceptingRequestExecution().execute(this, bufferedOutput)
    }

    /**
     * 拦截器的执行器链条, 它负责控制拦截器链的向下执行的过程的流转...
     */
    inner class InterceptingRequestExecution : ClientHttpRequestExecution {
        // 通常拦截器的链条是做成一个interceptorIndex去控制拦截器链条的流转的方式
        // Note: 拦截器的链条的实现, 也可以使用Iterator的实现方式, 使用hasNext去进行判断是否是最后一个拦截器
        private val iterator = interceptors.iterator()

        override fun execute(request: ClientHttpRequest, body: ByteArray): ClientHttpResponse {
            return if (iterator.hasNext()) {
                iterator.next().intercept(request, body, this)
            } else {
                // 这里因为往下传递的request其实是InterceptingClientHttpRequest对象, 因此, 我们必须将request当中的数据全部合并到delegate当中
                val delegate = requestFactory.createRequest(request.getUri(), request.getMethod())

                // copy headers to delegate
                request.getHeaders().forEach { delegate.getHeaders().addAll(it.key, it.value) }

                // copy RequestBody to delegate
                if (body.isNotEmpty()) {
                    StreamUtils.copy(body, delegate.getBody())
                }

                // execute and return Response
                delegate.execute()
            }
        }
    }
}