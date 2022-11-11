package com.wanna.nacos.naming.server.controller

import com.fasterxml.jackson.databind.node.ObjectNode
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.bind.annotation.RequestParam
import com.wanna.nacos.api.common.NamingConstants.DEFAULT_CLUSTER_NAME
import com.wanna.nacos.api.common.NamingConstants.DEFAULT_NAMESPACE_ID
import com.wanna.nacos.naming.server.core.NamingInstance
import com.wanna.nacos.naming.server.core.NamingServiceManager
import com.wanna.nacos.naming.server.healthcheck.ClientBeatInfo
import com.wanna.nacos.naming.server.push.PushService
import com.wanna.nacos.naming.server.util.JacksonUtils

/**
 * 提供了Nacos的实例的相关操作，包括注册/删除/心跳等操作
 */
@RequestMapping(["/v1/ns/instance"])
@RestController
open class InstanceController {

    @Autowired
    private lateinit var serviceManager: NamingServiceManager

    @Autowired
    private lateinit var pushService: PushService

    /**
     * 注册一个NamingInstance到注册中心
     *
     * @param namespaceId namespaceId
     * @param instance 要去进行注册的实例信息(自动从请求参数当中去进行获取和封装)
     * @return 如果正常注册完成，那么return "ok"
     */
    @RequestMapping(["/register"])
    open fun register(
        @RequestParam(required = false, defaultValue = DEFAULT_NAMESPACE_ID) namespaceId: String,
        instance: NamingInstance,
    ): String {
        instance.validate()
        instance.lastBeat = System.currentTimeMillis()
        serviceManager.registerInstance(namespaceId, instance.serviceName, instance)
        return "ok"
    }

    /**
     * 从注册中心当中去取消注册一个NamingInstance
     *
     * @param namespaceId namespaceId
     * @param instance 要去进行取消注册的实例信息(自动从请求参数当中去进行获取和封装)
     * @return 如果正常注册完成，那么return "ok"
     */
    @RequestMapping(["/deregister"])
    open fun deregister(
        @RequestParam(required = false, defaultValue = DEFAULT_NAMESPACE_ID) namespaceId: String,
        instance: NamingInstance,
    ): String {
        instance.validate()
        serviceManager.removeInstance(namespaceId, instance.serviceName, instance.ephemeral, instance)
        return "ok"
    }

    /**
     * 处理客户端发送的心跳信息
     */
    @RequestMapping(["/beat"])
    open fun beat(
        @RequestParam(required = false, defaultValue = DEFAULT_NAMESPACE_ID) namespaceId: String,
        @RequestParam serviceName: String,
        @RequestParam(required = false, defaultValue = DEFAULT_CLUSTER_NAME) clusterName: String,
        @RequestParam ip: String,
        @RequestParam(required = false, defaultValue = "0") port: Int,
    ): String {
        val clientBeatInfo = ClientBeatInfo(clusterName, ip, port)
        val service = serviceManager.getService(namespaceId, serviceName)
            ?: throw IllegalStateException("该NamingService[serviceName=$serviceName]还未存在")
        var instance = serviceManager.getInstance(namespaceId, serviceName, clusterName, ip, port)
        // 如果该实例还没存在的话，那么先去进行注册...
        if (instance == null) {
            instance = NamingInstance()
            instance.ip = ip
            instance.port = port
            instance.clusterName = clusterName
            instance.lastBeat = System.currentTimeMillis()
            serviceManager.registerInstance(namespaceId, serviceName, instance)
        }
        // 交给NamingService去处理一次客户端的心跳信息...
        service.processClientBeat(clientBeatInfo)
        return "ok"
    }

    /**
     * 列出当前注册中心当中的所有的NamingInstance列表
     */
    @RequestMapping(["/list"])
    open fun list(
        @RequestParam(required = false, defaultValue = DEFAULT_NAMESPACE_ID) namespaceId: String,
        @RequestParam serviceName: String,
        @RequestParam(required = false, defaultValue = DEFAULT_CLUSTER_NAME) clusters: String,
        @RequestParam ip: String,
        @RequestParam(required = false, defaultValue = "0") port: Int
    ): Any {
        // bug? 如果直接使用ObjectNode去进行写出，客户端使用Json去进行反序列化不能成果？
        return getNamingInstances(namespaceId, serviceName, clusters, ip, port, isCheckRequest = true, true).toString()
    }

    /**
     * 根据ip和port，获取指定的实例的具体信息
     */
    @RequestMapping(["/detail"])
    open fun detail(
        @RequestParam(required = false, defaultValue = DEFAULT_NAMESPACE_ID) namespaceId: String,
        @RequestParam serviceName: String,
        @RequestParam(required = false, defaultValue = DEFAULT_CLUSTER_NAME) clusterName: String,
        @RequestParam ip: String,
        @RequestParam(required = false, defaultValue = "0") port: Int
    ): Any {
        val service =
            serviceManager.getService(namespaceId, serviceName)
                ?: throw IllegalStateException("无法获取到指定的Service[namespaceId=$namespaceId,serviceName=$serviceName]")
        val allIps = service.allIps(listOf(clusterName))

        // 匹配集群下的所有NamingInstance，找到ip和port都匹配的NamingInstance
        allIps.forEach {
            if (it.ip == ip && it.port == port) {
                return it.asObjectNode().toString()
            }
        }
        throw IllegalStateException("无法找到目标实例[namespaceId=$namespaceId, serviceName=$serviceName, clusterName=$clusterName, ip=$ip, port=$port]")
    }

    /**
     * 获取NamingInstance列表
     *
     * @param namespaceId namespaceId
     * @param serviceName serviceName
     * @param clusters clusters
     * @param clientIp clientIp
     * @param port clientPort
     * @param isCheckRequest 它是否只是一个检查的请求
     * @param healthyOnly 是否只获取健康的实例列表？
     */
    open fun getNamingInstances(
        namespaceId: String,
        serviceName: String,
        clusters: String,
        clientIp: String,
        port: Int,
        isCheckRequest: Boolean,
        healthyOnly: Boolean
    ): ObjectNode {
        val objectNode = JacksonUtils.createEmptyObjectNode()
        val service = serviceManager.getService(namespaceId, serviceName)

        // 如果Service不存在的话，那么返回一个空的信息即可...
        if (service == null) {
            objectNode.put("name", serviceName)
            objectNode.put("clusters", clusters)
            objectNode.set<ObjectNode>("hosts", JacksonUtils.createEmptyObjectNode())
            return objectNode
        }

        val arrayNode = JacksonUtils.createEmptyArrayNode()

        // 获取给定的集群下的全部实例列表
        val allIps = service.allIps(clusters.split(","))

        // 将该NamingService下的所有的NamingInstance实例划分成为健康和不健康两类
        val healthyIps = ArrayList<NamingInstance>()
        val unHealthyIps = ArrayList<NamingInstance>()
        allIps.forEach { (if (it.healthy) healthyIps else unHealthyIps) += it }

        healthyIps.forEach { arrayNode.add(it.asObjectNode()) }
        // 如果需要获取不健康的，那么添加不健康的实例列表...不然的话，只要健康的实例列表
        if (!healthyOnly) {
            unHealthyIps.forEach { arrayNode.add(it.asObjectNode()) }
        }
        objectNode.set<ObjectNode>("hosts", arrayNode)
        objectNode.put("serviceName", serviceName)
        objectNode.put("enabled", service.enabled)
        objectNode.put("clusters", clusters)
        objectNode.put("metadata", JacksonUtils.toJson(service.metadata))
        return objectNode
    }
}