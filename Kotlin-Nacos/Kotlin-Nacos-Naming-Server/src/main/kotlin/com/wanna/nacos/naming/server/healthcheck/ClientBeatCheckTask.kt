package com.wanna.nacos.naming.server.healthcheck

import com.wanna.framework.web.client.RestTemplate
import com.wanna.nacos.naming.server.core.NamingInstance
import com.wanna.nacos.naming.server.core.NamingService
import org.slf4j.LoggerFactory

/**
 * 这是一个客户端心跳检测的任务
 */
open class ClientBeatCheckTask(private val service: NamingService) : Runnable {
    private val restTemplate = RestTemplate()

    companion object {
        private val logger = LoggerFactory.getLogger(ClientBeatCheckTask::class.java)
    }

    override fun run() {
        // 获取所有的临时节点
        val allIps = service.allIps(true)

        // 如果NamingInstance心跳超时的话，将健康状态标识为false
        allIps.forEach {
            if (System.currentTimeMillis() - it.lastBeat > it.instanceHeartBeatTimeOut) {
                if (it.healthy) {
                    // 健康状态标记为false
                    it.healthy = false
                    logger.info("NamingInstance[namespaceId=${service.namespaceId},groupName=${service.groupName}, serviceName=${it.serviceName}]健康状态变为false")
                }
            }
        }

        // 如果到达删除NamingInstance的时间了，该节点都没有心跳包的反馈，那么直接删除ip...
        allIps.forEach {
            if (System.currentTimeMillis() - it.lastBeat > it.ipDeleteTimeout) {
                deleteIp(it)
                logger.info("NamingInstance[namespaceId=${service.namespaceId},groupName=${service.groupName}, serviceName=${it.serviceName}]被删除")
            }
        }
    }

    open fun deleteIp(instance: NamingInstance) {
        val params = mapOf(
            "ip" to instance.ip,
            "port" to instance.port.toString(),
            "serviceName" to instance.serviceName,
            "clusterName" to instance.clusterName
        )
        val delete = restTemplate.getForObject("/v1/ns/instance/deregister", String::class.java, params)
    }

    fun getTaskKey(): String {
        return ""
    }
}