package com.wanna.nacos.naming.server.core

import com.fasterxml.jackson.databind.node.ObjectNode
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.naming.CommonParams
import com.wanna.nacos.api.naming.pojo.Instance
import com.wanna.nacos.naming.server.util.JacksonUtils

/**
 * Nacos的NamingInstance
 */
open class NamingInstance : Instance() {
    var lastBeat: Long = 0L  // 上次心跳的时间
    var instanceHeartBeatTimeOut: Long = 10L * 1000  // 实例的心跳超时时间(超时健康状态变为false)
    var ipDeleteTimeout: Long = 30L * 1000  // 超时就移除该节点的时间

    companion object {
        /**
         * 从请求当中去解析到的NamingInstance相关属性
         *
         * @param request request
         * @param updateLastBeat 是否更新最后一次心跳的时间? 
         */
        @JvmStatic
        fun fromRequest(request: HttpServerRequest, updateLastBeat: Boolean = true): NamingInstance {
            val namingInstance = NamingInstance()
            namingInstance.ip = request.getParam("ip") ?: throw IllegalStateException("ip不能为空")
            namingInstance.port = request.getParam("port")?.toInt() ?: throw NullPointerException("port不能为空")
            namingInstance.clusterName =
                request.getParam(CommonParams.CLUSTER_NAME) ?: Constants.DEFAULT_CLUSTER_NAME
            namingInstance.serviceName =
                request.getParam(CommonParams.SERVICE_NAME) ?: throw NullPointerException("serviceName不能为null")
            if (updateLastBeat) {
                // 设置上次心跳的时间为当前时间
                namingInstance.lastBeat = System.currentTimeMillis()
            }
            return namingInstance
        }
    }

    /**
     * 将NamingInstance转换为JacksonObjectNode
     *
     * @return 转换之后的ObjectNode
     */
    open fun asObjectNode(): ObjectNode {
        val ipNode = JacksonUtils.createEmptyObjectNode()
        ipNode.put("ip", ip)
        ipNode.put("port", port)
        ipNode.put("instanceId", instanceId)
        ipNode.put("clusterName", clusterName)
        ipNode.put("weight", weight)
        ipNode.put("serviceName", serviceName)
        ipNode.put("healthy", healthy)
        ipNode.put("ephemeral", ephemeral)
        ipNode.put("enabled", enabled)
        ipNode.put("metadata", JacksonUtils.toJson(metadata))
        return ipNode
    }
}