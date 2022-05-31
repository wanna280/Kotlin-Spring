package com.wanna.nacos.naming.server.core

import com.wanna.nacos.api.naming.pojo.Service
import com.wanna.nacos.naming.server.healthcheck.ClientBeatCheckTask
import com.wanna.nacos.naming.server.healthcheck.ClientBeatInfo
import com.wanna.nacos.naming.server.healthcheck.ClientBeatProcessor
import com.wanna.nacos.naming.server.healthcheck.HealthCheckReactor

/**
 * Nacos的NamingService
 */
open class NamingService : Service() {
    // Client的心跳检测任务(Runnable)
    // 可以去定时对NamingService当中的所有的NamingInstance去进行心跳的检测
    // 当到达心跳超时还未收到客户端的心跳时，自动地将实例的healthy设置为false；
    // 当到达删除实例的时间还未收到来自客户端的心跳时，自动将该实例从注册中心当中删除掉；
    private val clientBeatCheckTask = ClientBeatCheckTask(this)

    // namespaceId
    lateinit var namespaceId: String

    // 是否启用该NamingService？
    var enabled = true

    // 一个Service下的多个Cluster列表
    var clusterMap = HashMap<String, NamingCluster>()

    open fun set(namespaceId: String, groupName: String, serviceName: String) {
        super.set(groupName, serviceName)
        this.namespaceId = namespaceId
    }

    override fun validate() {
        super.validate()
        if (!this::namespaceId.isInitialized) {
            throw IllegalStateException("NamingService的namespace未完成初始化工作")
        }
    }

    /**
     * NamingService的初始化方法，添加心跳检测的任务
     */
    open fun init() {
        // 添加心跳检测任务
        HealthCheckReactor.scheduleCheck(this.clientBeatCheckTask)
    }

    /**
     * NamingService的摧毁方法
     */
    open fun destroy() {
        // 取消心跳检测任务
        HealthCheckReactor.cancelCheck(this.clientBeatCheckTask)
    }

    /**
     * 处理客户端心跳信息，将指定的NamingInstance的最后一次心跳设置为当前时间
     *
     * @param clientBeatInfo 客户端的心跳信息
     */
    open fun processClientBeat(clientBeatInfo: ClientBeatInfo) {
        val clientBeatProcessor = ClientBeatProcessor()
        clientBeatProcessor.service = this
        clientBeatProcessor.clientBeatInfo = clientBeatInfo
        HealthCheckReactor.scheduleNow(clientBeatProcessor)  // schedule now
    }

    /**
     * 统计当前NamingService下的**所有**集群下的所有的NamingInstance列表(持久/临时二选一)
     *
     * @param ephemeral 要统计的是临时节点还是所有的节点？
     * @return 所有集群下的所有(临时/持久)实例列表
     */
    open fun allIps(ephemeral: Boolean): List<NamingInstance> {
        return clusterMap.values.map { it.allIps(ephemeral) }.flatMap { it.toList() }.toList()
    }

    /**
     * 统计当前NamingService下的所有集群下的NamingInstance(持久&临时)
     *
     * @return 所有集群下的所有(临时&持久)实例列表
     */
    open fun allIps(): List<NamingInstance> {
        return clusterMap.values.map { it.allIps() }.flatMap { it.toList() }.toList()
    }

    /**
     * 给定一个集群列表，获取该集群列表下的所有实例
     *
     * @param clusterNames 要获取的集群列表
     * @return 给定的所有集群下的所有的实例列表(持久&临时)
     */
    open fun allIps(clusterNames: Collection<String>): List<NamingInstance> {
        val allIpsForClusters = ArrayList<NamingInstance>()
        clusterNames.forEach {
            if (clusterMap.containsKey(it)) {
                allIpsForClusters += clusterMap[it]!!.allIps()
            }
        }
        return allIpsForClusters
    }

    /**
     * 给出当前的NamingService下的最新的NamingInstance列表，对Cluster去进行分类，交给Cluster去进行实例列表的更新工作
     *
     * @param instances 最新的实例列表
     * @param ephemeral 更新的是否是临时节点？
     */
    open fun updateIPs(instances: Collection<NamingInstance>, ephemeral: Boolean) {
        // 为全部集群构建新的NamingInstance列表，key-clusterName，value-该cluster下要去进行更新的实例列表
        val ipsMap = HashMap<String, MutableList<NamingInstance>>()
        clusterMap.keys.forEach { ipsMap[it] = ArrayList() }

        // 统计出来所有集群下要去进行更新的所有的实例列表...
        instances.forEach {
            val clusterName = it.clusterName

            // 如果之前还没存在有这个Cluster，那么先去创建该Cluster并完成初始化工作
            if (!clusterMap.containsKey(clusterName)) {
                val cluster = NamingCluster(clusterName, this)
                cluster.init()
                clusterMap[clusterName] = cluster
            }

            // 获取该实例对应的clusterName下要进行变更的实例列表
            // 有可能该cluster是一个新的cluster，因此这里需要额外去进行一遍检测，完成currentClusterIps的初始化
            var currentClusterIps = ipsMap[clusterName]
            if (currentClusterIps == null) {
                currentClusterIps = ArrayList()
                ipsMap[clusterName] = currentClusterIps
            }

            // 该集群下要进行更新的实例+1
            currentClusterIps += it
        }

        // 遍历所有的要进行更新的NamingCluster，交给NamingCluster去进行真正的NamingInstance列表更新
        ipsMap.forEach { (clusterName, instances) -> clusterMap[clusterName]!!.updateIps(instances, ephemeral) }
    }
}