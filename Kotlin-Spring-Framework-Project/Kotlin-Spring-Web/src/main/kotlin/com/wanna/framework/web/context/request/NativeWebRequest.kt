package com.wanna.framework.web.context.request

/**
 * 将request和response去进行封装
 */
interface NativeWebRequest {
    /**
     * 获取request
     */
    fun getNativeRequest(): Any

    /**
     * 获取response(有可能为null)
     */
    fun getNativeResponse(): Any?

    /**
     * 按照指定类型去获取request
     */
    fun <T> getNativeRequest(type: Class<T>): T

    /**
     * 按照指定类型去获取response(有可能为null)
     */
    fun <T> getNativeResponse(type: Class<T>): T?
}