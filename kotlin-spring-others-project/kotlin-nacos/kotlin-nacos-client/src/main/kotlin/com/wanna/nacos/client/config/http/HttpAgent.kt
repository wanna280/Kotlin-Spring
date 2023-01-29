package com.wanna.nacos.client.config.http

import com.wanna.nacos.api.exception.NacosException
import com.wanna.nacos.api.model.HttpRestResult
import java.io.Closeable
import kotlin.jvm.Throws

/**
 * 代理去进行和ConfigServer去进行通信Http客户端
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/19
 */
interface HttpAgent : Closeable {

    /**
     * 开始去获取Nacos的IP列表
     */
    @Throws(NacosException::class)
    fun start()

    /**
     * 发送HTTP GET请求
     *
     * @param path path
     * @param headers headers
     * @param paramValues params
     * @param encoding Encoding
     * @param readTimeout 超时时间
     */
    @Throws(Exception::class)
    fun httpGet(
        path: String,
        headers: Map<String, String>,
        paramValues: Map<String, String>,
        encoding: String,
        readTimeout: Long
    ): HttpRestResult<String>

    /**
     * 发送HTTP Post请求
     *
     * @param path path
     * @param headers headers
     * @param paramValues params
     * @param encoding Encoding
     * @param readTimeout 超时时间
     */
    @Throws(Exception::class)
    fun httpPost(
        path: String,
        headers: Map<String, String>,
        paramValues: Map<String, String>,
        encoding: String,
        readTimeout: Long
    ): HttpRestResult<String>

    /**
     * 发送HTTP DELETE请求
     *
     * @param path path
     * @param headers headers
     * @param paramValues params
     * @param encoding Encoding
     * @param readTimeout 超时时间
     */
    @Throws(Exception::class)
    fun httpDelete(
        path: String,
        headers: Map<String, String>,
        paramValues: Map<String, String>,
        encoding: String,
        readTimeout: Long
    ): HttpRestResult<String>

    /**
     * 获取当前HttpAgent的tenant(namespace)
     *
     * @return tenant(namespace)
     */
    fun getTenant(): String

    /**
     * 获取当前HttpAgent的encode
     *
     * @return encode
     */
    fun getEncode(): String
}