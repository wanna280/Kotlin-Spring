package com.wanna.nacos.client.config.impl

import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.exception.NacosException
import com.wanna.nacos.api.utils.IPUtils
import com.wanna.nacos.client.utils.ParamUtils
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList

/**
 * Nacos的ServerList的管理器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/19
 */
class ServerListManager() : Closeable {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ServerListManager::class.java)

        /**
         * Http协议前缀
         */
        private const val HTTP_PROTOCOL = "http://"

        /**
         * Https协议前缀
         */
        private const val HTTPS_PROTOCOL = "https://"
    }

    /**
     * contextPath
     */
    var contextPath: String = ""

    /**
     * ServerList路径名
     */
    var serverListName = "serverlist"

    /**
     * namespace
     */
    var namespace: String = ""
        private set

    /**
     * tenant(namespace)
     */
    var tenant: String = ""
        private set

    /**
     * ServerURL的列表是否是固定的? 对于使用Endpoint的情况, 是支持从Endpoint去动态加载ServerURL的
     */
    private var fixed: Boolean = false

    /**
     * 用于去拉取ServerURL的地址, 针对给定的是Endpoint而不是直接的ServerURL的情况
     */
    private var addressServerUrl = ""

    /**
     * endpoint的端口号
     */
    private var endpointPort = ParamUtils.getDefaultServerPort()

    /**
     * 当前ServerListManger是否已经启动? 只有在使用的是Endpoint的模式下, 才需要启动, 用于启动一个线程去进行异步地使用Endpoint去拉取ServerList
     */
    private var started = false

    /**
     * 避免ServerListManager当中的方法的并发访问, 需要加锁
     */
    private val lock = ReentrantLock()

    /**
     * 用于等待的Condition阻塞队列
     */
    private val condition = lock.newCondition()

    /**
     * 使用Endpoint的模式去拉取ServerList时, 初始情况下需要尝试多少次? 当失败时, 需要去进行重试
     */
    private var initServerListRetryTimes = 5

    /**
     * 在后台不断根据Endpoint去拉取ServerList的异步线程池, 每隔30s去拉取一次
     */
    private var serverListExecutorService = ScheduledThreadPoolExecutor(1, ThreadFactory {
        val thread = Thread(it)
        thread.isDaemon = true
        thread.name = "com.wanna.nacos.client.ServerListManager"
        return@ThreadFactory thread
    })

    /**
     * Nacos ConfigServer List
     */
    @Volatile
    private var serverList: List<String> = ArrayList()

    /**
     * 用于去迭代ServerList的迭代器
     */
    private var iterator: Iterator<String> = serverList.iterator()

    /**
     * current ServerAddr, 需要多线程之间的同步, 因此需要加volatile
     */
    @Volatile
    private var currentServerAddr: String = ""

    /**
     * 根据给定的Properties当中的配置信息去进行初始化
     *
     * @param properties 关于NacosConfigClient的Properties配置信息
     */
    constructor(properties: Properties) : this() {
        val serverList = ArrayList<String>()
        val serverAddr = properties[PropertyKeyConst.SERVER_ADDR] ?: ""
        val namespace = properties[PropertyKeyConst.NAMESPACE]?.toString() ?: ""

        // 解析endpoint地址
        val endpoint = properties[PropertyKeyConst.ENDPOINT]?.toString()

        // 如果serverAddr不为空的话, 那么需要直接根据serverAddr去解析成为固定的ServerUrl列表
        if (serverAddr.toString().isNotBlank()) {
            // 1.初始化serverAddr
            val serverAddress = serverAddr.toString().split(",")
            serverAddress.forEach {
                if (it.startsWith(HTTP_PROTOCOL) || it.startsWith(HTTPS_PROTOCOL)) {
                    serverList + it
                } else {
                    val ipAndPort = it.split(IPUtils.IP_PORT_SEPARATOR)
                    if (ipAndPort.size == 1) {
                        serverList += HTTP_PROTOCOL + ipAndPort + IPUtils.IP_PORT_SEPARATOR + ParamUtils.getDefaultServerPort()
                    } else {
                        serverList += HTTP_PROTOCOL + ipAndPort[0] + IPUtils.IP_PORT_SEPARATOR + ipAndPort[1]
                    }
                }
            }
            this.serverList = serverList
            this.fixed = true

            // 如果serverAddr为空的话, 那么就需要根据Endpoint去构建出来addressServerUrl
            // 等待后续从Endpoint这个地址当中去进行ServerAddr的不断加载, 从而刷新ServerAddr
        } else {
            if (endpoint == null || endpoint.isBlank()) {
                throw IllegalArgumentException("在serverAddr为空的情况下, endpoint不能为空")
            }
            this.fixed = false
            if (namespace.isBlank()) {
                this.addressServerUrl = "http://$endpoint:$endpointPort/$contextPath/$serverListName"
            } else {
                this.addressServerUrl =
                    "http://$endpoint:$endpointPort/$contextPath/$serverListName?namespace=$namespace"
            }
        }

        this.namespace = namespace
        this.tenant = namespace
    }

    /**
     * 获取下一个ServerAddr
     *
     * @return nextServerAddr
     */
    fun getNextServerAddr(): String {
        if (!iterator.hasNext()) {
            refreshCurrentServerAddr()
            return currentServerAddr
        }
        try {
            return iterator.next()
        } catch (ex: Exception) {
            refreshCurrentServerAddr()
            return currentServerAddr
        }
    }

    /**
     * 启动当前ServerListManager, 开始去根据Endpoint去拉取ServerList到本地...
     */
    fun start() {
        lock.lock()
        try {
            // 如果ServerList是固定的, 或者是当前ServerListManager已经启动过了, 那么pass调
            if (this.fixed || this.started) {
                return
            }
            // 如果ServerList不是固定的, 那么就需要根据Endpoint去进行ServerList的加载...
            val serverListTask = GetServerListTask(this.addressServerUrl)

            // 尝试5次去拉取ServerList, 直到拉取成功为止...
            for (index in 0 until this.initServerListRetryTimes) {
                serverListTask.run()
                try {
                    condition.await((index + 1) * 100L, TimeUnit.MILLISECONDS)
                } catch (ex: Throwable) {
                    logger.error("get ServerList Failed, addressServerUrl=$addressServerUrl")
                }
                if (serverList.isNotEmpty()) {
                    break
                }
            }
            // 如果拉取了5次还是没有拉取到ServerList的话...那么算是拉取失败了...
            if (serverList.isEmpty()) {
                throw NacosException("get ServerList Failed, 无法连接服务器, addressServerUrl=$addressServerUrl")
            }

            // 异步线程池, 每隔30s去拉取一次ServerList
            this.serverListExecutorService.scheduleWithFixedDelay(serverListTask, 0, 30, TimeUnit.SECONDS)
            this.started = true
        } finally {
            lock.unlock()
        }
    }

    /**
     * 刷新currentServerAddr, 将迭代器重新恢复到初始的位置, 重新去进行ServerAddr的迭代
     */
    fun refreshCurrentServerAddr() {
        lock.lock()
        try {
            this.iterator = this.serverList.iterator()
            this.currentServerAddr = this.iterator.next()
        } finally {
            lock.unlock()
        }
    }

    /**
     * 获取当前的ServerAddr
     *
     * @return current ServerAddr
     */
    fun getCurrentServerAddr(): String {
        if (currentServerAddr.isBlank()) {
            refreshCurrentServerAddr()
        }
        return currentServerAddr
    }

    /**
     * 根据给定的serverUrl去拉取ServerList的Runnable任务
     *
     * @param addressServerUrl 拉取ServerUrl的地址
     */
    private inner class GetServerListTask(private val addressServerUrl: String) : Runnable {
        override fun run() {

        }
    }

    private fun updateIfChanged(newServerList: List<String>) {
        if (newServerList.isEmpty()) {
            logger.error("[Update-ServerList] 当前从${addressServerUrl}当中去拉取到的ServerList为空")
            return
        }
        val newServerAddrList = ArrayList<String>()
        newServerList.forEach {
            if (it.startsWith(HTTP_PROTOCOL) || it.startsWith(HTTPS_PROTOCOL)) {
                newServerAddrList += it
            } else {
                newServerAddrList += (HTTP_PROTOCOL + it)
            }
        }
        // 如果没有变化的话, 那么直接pass调
        if (serverList == newServerList) {
            return
        }
        this.serverList = newServerList

        // 刷新当前的serverAddr
        refreshCurrentServerAddr()


    }


    override fun close() {

    }
}