package com.wanna.nacos.config.server.utils

import com.wanna.framework.web.server.HttpServerRequest

/**
 * Request的相关工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
object RequestUtils {

    /**
     * ClientAppName的HeaderName
     */
    private const val CLIENT_APP_NAME_HEADER = "Client-AppName"

    /**
     * Nginx的真实Ip的Header
     */
    private const val X_REAL_IP = "X-Real-IP"

    /**
     * 当发生转发时, 需要在这个字段当中存放真实的IP
     */
    private const val X_FORWARDED_FOR = "X-Forwarded-For"

    /**
     * 发生多次转发时, 多个IP之间的分隔符
     */
    private const val X_FORWARDED_FOR_SPLIT_SYMBOL = ","

    /**
     * 从request的header当中去解析到ClientAppName
     *
     * @param request request
     * @return clientAppName
     */
    @JvmStatic
    fun getClientAppName(request: HttpServerRequest): String? {
        return request.getHeader(CLIENT_APP_NAME_HEADER)
    }

    /**
     * 从request当中去解析出来远程的Ip
     *
     * @param request request
     * @return remoteIp
     */
    @JvmStatic
    fun getRemoteIp(request: HttpServerRequest): String {

        // 1.首先检查"X-Forwarded-For"
        val xForwardedFor = request.getHeader(X_FORWARDED_FOR)
        if (xForwardedFor != null && xForwardedFor.isNotBlank()) {
            return xForwardedFor.split(X_FORWARDED_FOR_SPLIT_SYMBOL)[0]
        }
        // 2.检查Nginx的"X_Real_IP"字段
        val nginxHeader = request.getHeader(X_REAL_IP)

        // 3.如果还是无法解析的话, 直接获取request的remoteIp
        return if (nginxHeader != null && nginxHeader.isNotBlank()) nginxHeader else request.getRemoteHost()
    }
}