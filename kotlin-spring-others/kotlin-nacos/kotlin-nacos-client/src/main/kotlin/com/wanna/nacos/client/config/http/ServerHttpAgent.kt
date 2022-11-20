package com.wanna.nacos.client.config.http

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.client.RestTemplate
import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.http.param.Header
import com.wanna.nacos.api.model.HttpRestResult
import com.wanna.nacos.client.config.impl.ServerListManager
import java.net.ConnectException
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*

/**
 * Server HttpAgent
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/19
 *
 * @param properties 在ConfigClient当中的相关Properties配置信息
 */
class ServerHttpAgent(properties: Properties) : HttpAgent {

    /**
     * ServerListManager, 负责去管理Nacos的ConfigServer列表
     */
    private var serverListManager = ServerListManager(properties)

    /**
     * namespace
     */
    private var tenant: String = properties[PropertyKeyConst.NAMESPACE]?.toString() ?: ""

    /**
     * RestTemplate
     */
    private val restTemplate = RestTemplate()

    init {
        start()
    }

    override fun httpGet(
        path: String,
        headers: Map<String, String>,
        paramValues: Map<String, String>,
        encoding: String,
        readTimeout: Long
    ): HttpRestResult<String> {
        val endTime = System.currentTimeMillis() + readTimeout

        val serverAddr = serverListManager.currentServerAddr
        // 只要没超时, 那么就一直尝试...
        do {
            val uri = StringBuilder(getUrl(serverAddr, path)).append("?")
            paramValues.forEach {
                uri.append(it.key).append("=").append(URLEncoder.encode(it.value, Constants.ENCODE)).append("&")
            }
            uri.setLength(uri.length - 1)
            val url = URI(uri.toString())
            try {
                return restTemplate.execute(url, RequestMethod.GET, { request ->
                    headers.forEach(request.getHeaders()::add)
                }) {
                    HttpRestResult(
                        Header(it.getHeaders().toSingleValueMap()),
                        it.getStatusCode(),
                        String(it.getBody().readAllBytes(), Charset.forName(encoding)),
                        ""
                    )
                }!!
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } while (System.currentTimeMillis() < endTime)
        throw ConnectException("没有可用的Server URL")
    }

    override fun httpPost(
        path: String,
        headers: Map<String, String>,
        paramValues: Map<String, String>,
        encoding: String,
        readTimeout: Long
    ): HttpRestResult<String> {
        val endTime = System.currentTimeMillis() + readTimeout

        val serverAddr = serverListManager.currentServerAddr
        // 只要没超时, 那么就一直尝试...
        do {
            val uri = StringBuilder(getUrl(serverAddr, path)).append("?")
            paramValues.forEach {
                uri.append(it.key).append("=").append(URLEncoder.encode(it.value, Constants.ENCODE)).append("&")
            }
            uri.setLength(uri.length - 1)
            val url = URI(uri.toString())
            try {
                return restTemplate.execute(url, RequestMethod.POST, { request ->
                    headers.forEach(request.getHeaders()::add)
                }) {
                    HttpRestResult(
                        Header(it.getHeaders().toSingleValueMap()),
                        it.getStatusCode(),
                        String(it.getBody().readAllBytes(), Charset.forName(encoding)),
                        ""
                    )
                }!!
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } while (System.currentTimeMillis() < endTime)
        throw ConnectException("没有可用的Server URL")
    }

    override fun httpDelete(
        path: String,
        headers: Map<String, String>,
        paramValues: Map<String, String>,
        encoding: String,
        readTimeout: Long
    ): HttpRestResult<String> {
        val endTime = System.currentTimeMillis() + readTimeout

        val serverAddr = serverListManager.currentServerAddr
        // 只要没超时, 那么就一直尝试...
        do {
            val uri = StringBuilder(getUrl(serverAddr, path)).append("?")
            paramValues.forEach {
                uri.append(it.key).append("=").append(URLEncoder.encode(it.value, Constants.ENCODE)).append("&")
            }
            uri.setLength(uri.length - 1)
            val url = URI(uri.toString())
            try {
                return restTemplate.execute(url, RequestMethod.DELETE, { request ->
                    headers.forEach(request.getHeaders()::add)
                }) {
                    HttpRestResult(
                        Header(it.getHeaders().toSingleValueMap()),
                        it.getStatusCode(),
                        String(it.getBody().readAllBytes(), Charset.forName(encoding)),
                        ""
                    )
                }!!
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } while (System.currentTimeMillis() < endTime)
        throw ConnectException("没有可用的Server URL")
    }

    override fun getTenant(): String = this.tenant

    override fun getEncode(): String {
        TODO("Not yet implemented")
    }

    private fun getUrl(serverAddr: String, relativePath: String): String {
        return serverAddr + serverListManager.contextPath + relativePath
    }

    override fun start() {
        serverListManager.start()
    }

    override fun close() {

    }
}