package com.wanna.framework.web.client

import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.ResponseEntity
import com.wanna.framework.web.http.client.ClientHttpRequest
import com.wanna.framework.web.http.client.ClientHttpResponse
import com.wanna.framework.web.http.client.InterceptingClientHttpRequestFactory
import com.wanna.framework.web.http.client.support.InterceptingHttpAccessor
import com.wanna.framework.web.http.converter.ByteArrayHttpMessageConverter
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.http.converter.StringHttpMessageConverter
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

    companion object {
        private val jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper")
    }


    private val messageConverters = ArrayList<HttpMessageConverter<*>>()

    init {
        // add StringHttpMessageConverter
        this.messageConverters.add(StringHttpMessageConverter())
        // add ByteArrayHttpMessageConverter
        this.messageConverters.add(ByteArrayHttpMessageConverter())
        if (jackson2Present) {
            messageConverters.add(MappingJackson2HttpMessageConverter())
        }
    }

    override fun <T> getForObject(
        url: String, responseType: Class<T>, uriVariables: Map<String, String>
    ): T? {
        val requestCallback = acceptHeaderRequestCallback(responseType)
        val responseExtractor = HttpMessageConverterExtractor(this.messageConverters, responseType)
        return execute(url, RequestMethod.GET, responseExtractor, uriVariables, requestCallback)
    }

    override fun <T> getForEntity(
        url: String, responseType: Class<T>, uriVariables: Map<String, String>
    ): ResponseEntity<T>? {
        val requestCallback = acceptHeaderRequestCallback(responseType)
        val responseExtractor = ResponseEntityResponseExtractor(responseType)
        return execute(url, RequestMethod.GET, responseExtractor, uriVariables, requestCallback)
    }

    override fun <T> postForObject(
        url: String, responseType: Class<T>, uriVariables: Map<String, String>
    ): T? {
        val requestCallback = acceptHeaderRequestCallback(responseType)
        val responseExtractor = HttpMessageConverterExtractor(this.messageConverters, responseType)
        return execute(url, RequestMethod.POST, responseExtractor, uriVariables, requestCallback)
    }

    override fun <T> postForEntity(
        url: String, responseType: Class<T>, uriVariables: Map<String, String>
    ): ResponseEntity<T>? {
        val requestCallback = acceptHeaderRequestCallback(responseType)
        val responseExtractor = ResponseEntityResponseExtractor(responseType)
        return execute(url, RequestMethod.POST, responseExtractor, uriVariables, requestCallback)
    }

    override fun <T> postForObject(
        url: String,
        requestBody: Any?,
        responseType: Class<T>,
        uriVariables: Map<String, String>
    ): T? {
        val requestCallback = httpEntityRequestCallback(requestBody)
        val responseExtractor = HttpMessageConverterExtractor(this.messageConverters, responseType)
        return execute(url, RequestMethod.POST, responseExtractor, uriVariables, requestCallback)
    }

    override fun <T> postForEntity(
        url: String,
        requestBody: Any?,
        responseType: Class<T>,
        uriVariables: Map<String, String>
    ): ResponseEntity<T>? {
        val requestCallback = httpEntityRequestCallback(requestBody)
        val responseExtractor = ResponseEntityResponseExtractor(responseType)
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
        return URI(if (uriVariables.isNotEmpty()) builder.substring(0, builder.length - 1) else builder.toString())
    }


    override fun <T> execute(
        url: URI, method: RequestMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?
    ): T? {
        // 使用ClientHttpRequestFactory创建ClientHttpRequest
        // 如果当前RestTemplate当中存在有拦截器的话，那么创建的将会是InterceptingClientHttpRequest；
        // 如果当前RestTemplate当中不存在有拦截器的话，那么创建的将会是来自于各个HttpClient(比如ApacheHttpClient)的ClientHttpRequest
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
     * 含有HttpEntity的RequestCallback，用来将HttpEntity写出到Request当中
     *
     * @param requestBody requestBody
     */
    private fun httpEntityRequestCallback(requestBody: Any?): RequestCallback {
        return HttpEntityRequestCallback(requestBody)
    }

    /**
     * 将响应转换为Entity的Response提取器
     */
    private inner class ResponseEntityResponseExtractor<T>(responseType: Class<T>) :
        ResponseExtractor<ResponseEntity<T>> {
        private val delegate = HttpMessageConverterExtractor(messageConverters, responseType)

        override fun extractData(response: ClientHttpResponse): ResponseEntity<T>? {
            return ResponseEntity(response.getStatusCode(), response.getHeaders(), delegate.extractData(response))
        }
    }

    /**
     * 将HttpEntity使用MessageConverter以合适的格式写出到Request当中的RequestCallback
     *
     * @param requestBody requestBody的Entity数据
     */
    private inner class HttpEntityRequestCallback(private val requestBody: Any?) : RequestCallback {
        override fun doWithRequest(request: ClientHttpRequest) {
            if (requestBody != null) {
                messageConverters.forEach {
                    if (it.canWrite(requestBody::class.java, MediaType.APPLICATION_JSON)) {
                        @Suppress("UNCHECKED_CAST")
                        (it as HttpMessageConverter<Any>).write(requestBody, MediaType.APPLICATION_JSON, request)
                    }
                }
            }
        }
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