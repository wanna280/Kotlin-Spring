package com.wanna.framework.web.cors

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.server.HttpServerRequest
import java.net.URL

/**
 * Cors的工具类
 */
object CorsUtils {

    /**
     * 判断给定的request, 是否是一个CORS的请求, 检查server和request的ip/port/schema是否相同
     *
     * @param request request
     * @return 该请求如果是一个Cors请求, 那么return true; 否则return false
     */
    @JvmStatic
    fun isCorsRequest(request: HttpServerRequest): Boolean {
        // 如果没有Origin, 说明它都不是一个跨域请求, 那么return false
        val requestOrigin = URL(request.getHeaders().getOrigin() ?: return false)

        // 如果有Origin的话, 那么需要检查schema/ip/port, 只要其中一个不匹配, 那么就是CORS请求
        return requestOrigin.host != request.getServerName()
                || requestOrigin.protocol != request.getSchema()
                || getPost(requestOrigin.host, requestOrigin.port) !=
                getPost(request.getSchema(), request.getServerPort())
    }

    /**
     * 根据"schema", 去获取到合适的默认port
     *
     * * 1.如果给定的port不为-1, 那么return port
     * * 2.如果给定的port为-1, 那么就需要根据schema去推测合适的port
     *
     * @param schema schema
     * @param port port
     */
    private fun getPost(@Nullable schema: String?, port: Int): Int {
        if (port == -1) {
            // ws/http to 80
            if (schema == "ws" || schema == "http") {
                return 80

                // wss/https to 443
            } else if (schema == "wss" || schema == "https") {
                return 443
            }
        }
        return port
    }


    /**
     * 是否是一个CORS的预检("PreFlight")请求？
     * Note: 对于DELETE/PUT等请求, 浏览器会对服务器端发送一个预检请求, 因为没有具体的请求方式, 采用的请求的方式是"OPTIONS",
     * 并且在预检请求当中, 浏览器还会自动携带上"Origin"和"Access-Control-Request-Method"这两个请求头, 我们需要同时去进行鉴别
     *
     * @param request request
     * @return 该请求是否是一个预检请求？如果是return true; 否则return false
     */
    @JvmStatic
    fun isPreFlightRequest(request: HttpServerRequest): Boolean {
        return request.getMethod() == RequestMethod.OPTIONS
                && request.getHeader(HttpHeaders.ORIGIN) != null
                && request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null
    }
}