package com.wanna.nacos.config.server.service

import com.wanna.framework.context.stereotype.Service
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.server.AsyncContext
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.nacos.api.notify.Event
import com.wanna.nacos.api.notify.NotifyCenter
import com.wanna.nacos.api.notify.listener.Subscriber
import com.wanna.nacos.config.server.model.event.LocalDataChangeEvent
import com.wanna.nacos.config.server.utils.ConfigExecutor
import com.wanna.nacos.config.server.utils.MD5Utils
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
     * 维护所有的客户端Subscriber
     */
    private val allSubscribers: Queue<ClientLongPolling> = ConcurrentLinkedQueue()

    init {

        // 注册一个监听配置文件变化的Subscriber, 去监听LocalDataChangeEvent事件,
        // 当这个事件触发时,代表ConfigServer配置文件发生了变更, 就需要执行一个DataChangeTask任务去进行通知客户端
        NotifyCenter.registerSubscriber(object : Subscriber<LocalDataChangeEvent>() {
            override fun onEvent(event: LocalDataChangeEvent) {
                ConfigExecutor.executeLongPolling(DataChangeTask(event.groupKey))
            }

            override fun subscribeType(): Class<out Event> = LocalDataChangeEvent::class.java
        })
    }


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

        // 开启异步支持, 获取到AsyncContext
        val asyncContext = request.startAsync(request, response)

        // 往线程池当中去添加一个长轮询的任务
        ConfigExecutor
            .executeLongPolling(
                ClientLongPolling(
                    asyncContext, clientMd5Map, remoteIp,
                    probeRequestSize, timeout, clientAppName, ""
                )
            )
    }

    /**
     * ConfigFile Data发生改变的任务
     *
     * @param groupKey groupKey
     */
    inner class DataChangeTask(private val groupKey: String) : Runnable {
        override fun run() {
            val iterator = allSubscribers.iterator()
            while (iterator.hasNext()) {
                val clientSubscriber = iterator.next()
                if (clientSubscriber.clientMd5Map.containsKey(groupKey)) {
                    iterator.remove()
                    clientSubscriber.sendResponse(listOf(groupKey))
                }
            }
        }
    }

    /**
     * 客户端的轮询任务Runnable
     */
    inner class ClientLongPolling(
        val asyncContext: AsyncContext,
        val clientMd5Map: Map<String, String>,
        val ip: String,
        val probeRequestSize: Int,
        val timeout: Long,
        val appName: String,
        val tag: String
    ) : Runnable {

        /**
         * 用于去处理异步超时的Future
         */
        private var asyncTimeoutFuture: Future<*>? = null

        override fun run() {
            // 添加到已经持有的IP列表当中去
            retainIps[ip] = System.currentTimeMillis()

            // 将当前ClientLongPolling添加到Subscribers当中
            allSubscribers.add(this)

            // 添加一个LongPolling任务, 到线程池当中
            this.asyncTimeoutFuture = ConfigExecutor.scheduleLongPolling({
                val request = asyncContext.getRequest()
                val response = asyncContext.getResponse()
                if (allSubscribers.remove(this@ClientLongPolling)) {
                    val changedGroups = MD5Utils.compareMd5(request!!, response, clientMd5Map)
                    if (changedGroups.isNotEmpty()) {
                        sendResponse(changedGroups)
                    } else {
                        sendResponse(null)
                    }
                }
            }, timeout, TimeUnit.MILLISECONDS)
        }

        /**
         * 发送Response
         *
         * @param changedGroups 发生变化的groupKey列表
         */
        fun sendResponse(@Nullable changedGroups: List<String>?) {
            // 取消超时任务
            if (asyncTimeoutFuture != null) {
                asyncTimeoutFuture?.cancel(false)
                return
            }


            // 生成Response并利用AsyncContext去进行消息的发送
            generateResponse(changedGroups)
        }

        /**
         * 生成ConfigResponse并返回给客户端数据
         *
         * @param changedGroups configFile已经发生变化那些的GroupKey
         */
        private fun generateResponse(@Nullable changedGroups: List<String>?) {
            if (changedGroups == null) {
                // send response
                asyncContext.complete()
                return
            }
            val response = asyncContext.getResponse()!!

            // 为changedGroups去转换成为"dataId"/"group"/"tenant"三个部分的参数, 并使用URLEncoder去进行编码
            val resultString = MD5Utils.compareMd5ResultString(changedGroups)

            response.setStatus(HttpStatus.SUCCESS)
            response.getOutputStream().write(resultString.toByteArray())

            // complete
            asyncContext.complete()
        }
    }
}