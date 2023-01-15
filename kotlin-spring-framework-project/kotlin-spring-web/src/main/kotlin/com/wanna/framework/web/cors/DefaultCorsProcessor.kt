package com.wanna.framework.web.cors

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

/**
 * CorsProcessor的默认实现
 *
 * @see CorsProcessor
 */
open class DefaultCorsProcessor : CorsProcessor {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultCorsProcessor::class.java)
    }

    /**
     * 处理跨域请求
     *
     * @param request request
     * @param response response
     * @param config CorsConfiguration
     */
    override fun processRequest(
        request: HttpServerRequest,
        response: HttpServerResponse,
        config: CorsConfiguration?
    ): Boolean {
        val headers = request.getHeaders(HttpHeaders.VARY)
        // 添加HttpHeader当中的Vary
        if (!headers.contains(HttpHeaders.ORIGIN)) {
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN)
        }
        if (!headers.contains(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)) {
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)
        }
        if (!headers.contains(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)) {
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)
        }
        // 如果都不是跨域请求, 那么return true, 我们直接放行即可
        if (!CorsUtils.isCorsRequest(request)) {
            return true
        }
        // 如果当前请求, 确实是一个跨域请求, 那么我们确实需要去进行跨域请求的处理了...

        // 如果在这之前响应的Header当中已经添加了Access-Control-Allow-Origin, 那么我们不用去进行处理了, return true
        if (response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) != null) {
            logger.trace("跳过跨域的处理, 因为Response的Header当中已经存在了[${HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN}]")
            return true
        }

        // 尝试去检查一下当前的请求, 是否是一个浏览器发起的OPTIONS预检请求...
        val preFlightRequest = CorsUtils.isPreFlightRequest(request)
        // 如果没有CorsConfiguration, 但是它是一个预检请求, 那么reject...如果它不是一个预检请求, return true
        if (config == null) {
            if (preFlightRequest) {
                rejectRequest(response)
                return false
            } else {
                return true
            }
        }

        // 如果已经找到了合适的CorsConfiguration, 那么我们需要真正地去处理当前的跨域请求
        return handleInternal(request, response, config, preFlightRequest)
    }

    /**
     * 因为"CORS"策略的不合法, 从而去拒绝请求,
     * 我们需要去设置HttpStatus为"403", 并设置Message为"Invalid CORS request"
     *
     * @param response response
     */
    protected open fun rejectRequest(response: HttpServerResponse) {
        response.setStatus(HttpStatus.FORBIDDEN)
        response.getOutputStream().write("Invalid CORS request".toByteArray(StandardCharsets.UTF_8))
        response.flush()
    }

    /**
     * 来到这里, 必须一定是一个真正的跨域请求了, 我们应该真正地去尝试去处理该跨域请求
     *
     * @param request request
     * @param response response
     * @param config CorsConfig
     * @param isPreflightRequest 当前请求是否是一个预检请求？
     */
    protected open fun handleInternal(
        request: HttpServerRequest,
        response: HttpServerResponse,
        config: CorsConfiguration,
        isPreflightRequest: Boolean
    ): Boolean {

        // 1.检查RequestOrigin是否被服务端接受？(从"Origin"当中去进行获取)
        val requestOrigin = request.getHeaders().getOrigin()
        val allowOrigin = checkOrigin(config, requestOrigin)
        if (allowOrigin == null) {
            logger.debug("拒绝来自[$requestOrigin]的RequestOrigin, 因为它不被服务端所允许")
            rejectRequest(response)  // reject
            return false
        }

        // 2.获取RequestMethodToUse("PreFlight"请求, 需要从"Access-Control-Request-Method"当中获取)
        val requestMethod = getRequestMethodToUse(request, isPreflightRequest)
        val allowMethods = checkHttpMethods(config, requestMethod)
        if (allowMethods == null) {
            logger.debug("拒绝来自[$requestMethod]的RequestMethods, 因为它不被服务器端所允许")
            rejectRequest(response)  // reject
            return false
        }

        // 3.获取RequestHeadersToUse("PreFlight"请求, 需要从"Access-Control-Request-Headers"当中获取)
        val requestHeaders = getRequestHeadersToUse(request, isPreflightRequest)
        val allowHeaders = checkHttpHeaders(config, requestHeaders)
        if (isPreflightRequest && allowHeaders == null) {
            logger.debug("拒绝[$requestHeaders]的RequestHeaders, 因为它不被服务器端所允许")
            rejectRequest(response)  // reject
            return false
        }

        val responseHeaders = response.getHeaders()
        // set "Access-Control-Allow-Origin" to response.header
        responseHeaders.setAccessControlAllowOrigin(allowOrigin)

        // set "Access-Control-Allow-Methods" to response.header
        if (isPreflightRequest) {
            responseHeaders.setAccessControlAllowMethods(allowMethods)
        }

        // set "Access-Control-Allow-Headers" to response.header
        if (isPreflightRequest && allowHeaders != null && allowHeaders.isNotEmpty()) {
            responseHeaders.setAccessControlAllowHeaders(allowHeaders)
        }

        // set "Access-Control-Exposed-Headers" to response.header
        if (config.getExposeHeaders() != null && config.getExposeHeaders()!!.isNotEmpty()) {
            responseHeaders.setAccessControlExposeHeaders(config.getExposeHeaders()!!)
        }

        // set "Access-Control-Allow-Credentials" to response.header
        if (config.getAllowCredentials() == true) {
            responseHeaders.setAccessControlAllowCredentials(true)
        }

        // set "Access-Control-Max-Age" to response.header
        if (isPreflightRequest && config.getMaxAge() != null) {
            responseHeaders.setAccessControlMaxAge(config.getMaxAge()!!)
        }

        response.flush()  // flush
        return true
    }

    /**
     * 检查Origin, 我服务端是否允许你浏览器去访问？
     *
     * @param config CorsConfig
     * @param requestOrigin 请求的origin
     * @return 如果服务端不允许, 那么return null; 允许则, return 匹配到服务端配置的Origin
     */
    protected open fun checkOrigin(config: CorsConfiguration, @Nullable requestOrigin: String?): String? {
        return config.checkOrigin(requestOrigin)
    }

    /**
     * 检查HttpMethods, 我服务端是否允许你浏览器去进行访问？
     *
     * @param config CorsConfig
     * @param method RequestMethod(对于预检"PreFlight"请求, 需要从"Access-Control-Request-Methods"当中去进行获取)
     * @return 服务端允许的请求方式的列表(如果不为null, 代表匹配; 如果为null, 代表该方式不被服务端认可, 是一个非法的CORS请求方式)
     */
    protected open fun checkHttpMethods(
        config: CorsConfiguration,
        @Nullable method: RequestMethod?
    ): List<RequestMethod>? {
        return config.checkHttpMethods(method)
    }

    /**
     * 检查HttpHeaders, 是否存在有我服务端允许的Header？
     *
     * @param config CorsConfig
     * @param requestHeaders RequestHeaders(对于预检"PreFlight"请求, 需要从"Access-Control-Request-Headers"当中去进行获取)
     * @return 服务端对于你给的Headers当中, 我允许去进行接收的Header列表(如果为不为null, 代表匹配,
     * 结果为我服务端匹配的结果; 如果为null, 说明你浏览器根本就没有携带合适的Header给我, 是一个非法的CORS的请求)
     */
    @Nullable
    protected open fun checkHttpHeaders(
        config: CorsConfiguration,
        @Nullable requestHeaders: List<String>?
    ): List<String>? {
        return config.checkHttpHeaders(requestHeaders)
    }

    /**
     * 获取需要去进行CORS的匹配的请求方式
     *
     * * 1.如果该请求是一个预检请求, 那么需要获取到"Access-Control-Request-Methods"的字段值去返回
     * * 2.如果不是一个预检请求, 那么直接使用"requestMethod"去返回即可
     *
     * @param request request
     * @param isPreflightRequest 当前请求是否是一个预检请求？
     * @return 对于预检请求, return "Access-Control-Request-Methods"字段的值; 对于不是预检请求, return "request.method"
     */
    @Nullable
    private fun getRequestMethodToUse(request: HttpServerRequest, isPreflightRequest: Boolean): RequestMethod? =
        if (isPreflightRequest) request.getHeaders().getAccessControlRequestMethod() else request.getMethod()

    /**
     * 获取需要去进行CORS的匹配的请求头
     *
     * * 1.如果该请求是一个预检请求, 那么需要获取到"Access-Control-Request-Headers"的字段值去进行返回
     * * 2.如果该请求不是一个预检请求, 那么需要直接使用"request.headerNames"去进行返回
     *
     * @param request request
     * @param isPreflightRequest 该请求是否是一个预检请求？
     * @return 对于预检请求, return "Access-Control-Request-Headers"字段的值; 对于不是预检请求, return "request.headerNames"
     */
    private fun getRequestHeadersToUse(request: HttpServerRequest, isPreflightRequest: Boolean): List<String> =
        if (isPreflightRequest) request.getHeaders().getAccessControlRequestHeaders()
        else request.getHeaderNames().toList()
}