package com.wanna.framework.web.client

import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.InterceptingClientHttpRequestFactory
import com.wanna.framework.web.http.client.support.InterceptingHttpAccessor
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.http.converter.json.MappingJackson2HttpMessageConverter
import java.net.URI

/**
 * RestTemplate，它是Spring-Web当中提供的一个Http请求的客户端；
 *
 * 它继承了拦截器的功能，允许拦截器去对RestTemplate去进行request的干预，比如修改request的url去实现负载均衡的相关功能
 *
 * @see HttpMessageConverter
 * @see InterceptingClientHttpRequestFactory
 * @see InterceptingHttpAccessor
 */
open class RestTemplate : RestOperations, InterceptingHttpAccessor() {

    private val messageConverters = ArrayList<HttpMessageConverter<*>>()

    init {
        messageConverters.add(MappingJackson2HttpMessageConverter())
    }

    override fun <T> getForObject(
        url: String, responseType: Class<T>, uriVariables: Map<String, String>
    ): T? {
        val requestCallback = acceptHeaderRequestCallback(responseType)
        val responseExtractor = HttpMessageConverterExtractor<T>(this.messageConverters, responseType)
        return execute(url, RequestMethod.GET, responseExtractor, uriVariables, requestCallback)
    }

    override fun <T> postForObject(
        url: String, responseType: Class<T>, uriVariables: Map<String, String>
    ): T? {
        val requestCallback = acceptHeaderRequestCallback(responseType)
        val responseExtractor = HttpMessageConverterExtractor<T>(this.messageConverters, responseType)
        return execute(url, RequestMethod.POST, responseExtractor, uriVariables, requestCallback)
    }

    protected open fun <T> execute(
        url: String,
        method: RequestMethod,
        responseExtractor: ResponseExtractor<T>,
        uriVariables: Map<String, String>,
        requestCallback: RequestCallback
    ): T? {
        val uri = createUri(url, uriVariables)
        return execute(uri, method, requestCallback, responseExtractor)
    }

    private fun createUri(url: String, uriVariables: Map<String, String>): URI {
        val builder = StringBuilder(url)
        if (uriVariables.isNotEmpty()) {
            builder.append("?")
        }
        uriVariables.forEach { (name, value) -> builder.append(name).append("=").append(value).append("&") }
        return URI(if(uriVariables.isNotEmpty()) builder.substring(0, builder.length - 1) else builder.toString())
    }


    override fun <T> execute(
        url: URI, method: RequestMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?
    ): T? {
        // 使用ClientHttpRequestFactory创建ClientHttpRequest
        val clientRequest: ClientHttpRequest = createRequest(url, method)

        // 如果必要的话，使用给定的RequestCallback对ClientHttpRequest去进行处理
        requestCallback?.doWithRequest(clientRequest)

        // 交给ClientHttpRequest去执行目标请求，从而得到ClientHttpResponse
        val clientResponse = clientRequest.execute()

        // 如果必要的话使用ResponseExtractor，去将ResponseBody当中的数据去进行提取出来转成T类型
        return responseExtractor?.extractData(clientResponse)
    }

    open fun <T> acceptHeaderRequestCallback(responseType: Class<T>): RequestCallback {
        return AcceptHeaderRequestCallback(responseType)
    }

    /**
     * 使用MessageConverter对request去进行干涉
     *
     * @param responseType response类型
     */
    inner class AcceptHeaderRequestCallback(private val responseType: Class<*>) : RequestCallback {
        override fun doWithRequest(request: ClientHttpRequest) {
            val messageConverters = this@RestTemplate.messageConverters

        }
    }
}