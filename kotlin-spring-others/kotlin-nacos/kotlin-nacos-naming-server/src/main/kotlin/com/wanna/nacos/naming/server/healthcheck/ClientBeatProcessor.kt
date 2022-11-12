package com.wanna.nacos.naming.server.healthcheck

import com.wanna.nacos.naming.server.core.NamingService
import org.slf4j.LoggerFactory

/**
 * 客户端心跳的处理器，主要是处理客户端发送过来的心跳信息，将NamingInstance的最后一次心跳的时间设置为当前时间；
 *
 * Note: 它是一个Runnable，会被提交给线程池去异步执行
 *
 * @see ClientBeatInfo
 */
open class ClientBeatProcessor : Runnable {
    companion object {

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ClientBeatProcessor::class.java)
    }

    /**
     * 需要处理的NamingService
     */
    lateinit var service: NamingService

    /**
     * 收到的客户端心跳信息
     */
    lateinit var clientBeatInfo: ClientBeatInfo

    /**
     * 对于相关的参数去进行检查是否完成初始化?
     */
    open fun validate() {
        if (!this::service.isInitialized) {
            throw IllegalStateException("ClientBeatProcessor的Service未完成初始化")
        }
        if (!this::clientBeatInfo.isInitialized) {
            throw IllegalStateException("ClientBeatProcessor的ClientBeat未完成初始化")
        }
    }

    override fun run() {
        validate()
        val cluster = service.clusterMap[clientBeatInfo.clusterName] ?: return

        // 遍历所有的临时节点，去进行匹配ip和port，将该实例的最后一次心跳时间修改为当前时间...
        val allIps = cluster.allIps(true)
        allIps.forEach {
            if (it.ip == clientBeatInfo.ip && it.port == clientBeatInfo.port) {
                it.lastBeat = System.currentTimeMillis()
                if (logger.isDebugEnabled) {
                    logger.debug("[ip=${clientBeatInfo.ip}, port=${clientBeatInfo.port}]发来了心跳包，最后一次心跳时间为[lastBeat=${it.lastBeat}]")
                }
            }
        }

    }
}