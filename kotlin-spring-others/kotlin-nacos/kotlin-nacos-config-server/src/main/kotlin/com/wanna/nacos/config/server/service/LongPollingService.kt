package com.wanna.nacos.config.server.service

import com.wanna.framework.context.stereotype.Service
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.nacos.config.server.utils.ConfigExecutor
import com.wanna.nacos.config.server.utils.RequestUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * 长轮询的Service
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
@Service
open class LongPollingService {

    companion object {
        /**
         * 长轮询的Header
         */
        private const val LONG_POLLING_HEADER = "Long-Polling-Timeout"

        /**
         * 从request当中去判断是否支持长轮询?
         *
         * @param request request
         * @return 如果header当中存在有"Long-Polling-Timeout"的话, 那么return true; 否则return false
         */
        @JvmStatic
        fun isSupportLongPolling(request: HttpServerRequest): Boolean {
            return request.getHeader(LONG_POLLING_HEADER) != null
        }
    }

    /**
     * 持有的IP的列表
     */
    private val retainIps = ConcurrentHashMap<String, Long>()

    /**
     * 维护所有的Subscriber
     */
    private val allSubscribers: Queue<ClientLongPolling> = ConcurrentLinkedQueue()


    /**
     * 添加一个长轮询的客户端
     *
     * @param request request
     * @param response response
     * @param clientMd5Map clientMd5Map
     * @param probeRequestSize probeRequestSize
     */
    fun addLongPollingClient(
        request: HttpServerRequest,
        response: HttpServerResponse,
        clientMd5Map: Map<String, String>,
        probeRequestSize: Int
    ) {
        val pollingTimeout = request.getHeader(LONG_POLLING_HEADER)!!
        val clientAppName = RequestUtils.getClientAppName(request) ?: ""
        val remoteIp = RequestUtils.getRemoteIp(request)

        // 解析超时时间(单位为ms)
        val timeout = max(pollingTimeout.toLong(), 10000)

        // 往线程池当中去添加一个长轮询的任务
        ConfigExecutor
            .executeLongPolling(
                ClientLongPolling(
                    request, response, clientMd5Map, remoteIp,
                    probeRequestSize, timeout, clientAppName, ""
                )
            )
    }

    /**
     * 客户端的轮询任务Runnable
     */
    inner class ClientLongPolling(
        val request: HttpServerRequest,
        val response: HttpServerResponse,
        val clientMd5Map: Map<String, String>,
        val ip: String,
        val probeRequestSize: Int,
        val timeout: Long,
        val appName: String,
        val tag: String
    ) : Runnable {

        /**
         * 异步超时的Future
         */
        private var asyncTimeoutFuture: Future<*>? = null

        override fun run() {
            // 添加到已经持有的IP列表当中去
            retainIps[ip] = System.currentTimeMillis()

            // 将当前ClientLongPolling添加到Subscribers当中
            allSubscribers.add(this)

            // 添加一个LongPolling任务
            this.asyncTimeoutFuture = ConfigExecutor.scheduleLongPolling({
                if (allSubscribers.remove(this@ClientLongPolling)) {
                    sendResponse(null)
                }
            }, timeout, TimeUnit.MILLISECONDS)
        }

        /**
         * 发送Response
         *
         * @param changedGroups 发生变化的group
         */
        private fun sendResponse(changedGroups: List<String>?) {
            // 取消超时任务
            asyncTimeoutFuture?.cancel(true)

            response.setStatus(HttpStatus.SUCCESS)
            response.getOutputStream().write(changedGroups.toString().toByteArray())
        }
    }
}