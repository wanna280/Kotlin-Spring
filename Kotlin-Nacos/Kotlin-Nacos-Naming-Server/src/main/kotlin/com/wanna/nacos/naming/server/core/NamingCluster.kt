package com.wanna.nacos.naming.server.core

import com.wanna.nacos.api.naming.pojo.Cluster

/**
 * Nacos的NamingCluster
 */
open class NamingCluster(clusterName: String, var service: NamingService) : Cluster(clusterName) {

    init {
        this.serviceName = service.serviceName
    }

    // 该NamingCluster是否已经完成了初始化工作
    var inited = false

    // 当前Cluster下的持久实例列表
    private var persistentInstances = HashSet<NamingInstance>()

    // 当前Cluster下的临时实例列表
    private var ephemeralInstances = HashSet<NamingInstance>()

    /**
     * 初始化当前的Cluster
     */
    open fun init() {
        if (inited) return
    }

    /**
     * 获取一个Cluster下的所有的实例(不管是持久节点还是临时节点)
     *
     * @return 一个Cluster下的全部实例列表
     */
    open fun allIps(): List<NamingInstance> {
        val allInstances = ArrayList<NamingInstance>()
        allInstances += persistentInstances
        allInstances += ephemeralInstances
        return allInstances
    }

    /**
     * 获取一个Cluster下的临时/持久节点的全部实例
     *
     * @param ephemeral 是否要获取持久节点？
     * @return 如果ephemeral=true，返回所有的临时节点；如果ephemeral=false，返回所有的持久节点
     */
    open fun allIps(ephemeral: Boolean): List<NamingInstance> {
        return if (ephemeral) ArrayList(ephemeralInstances) else ArrayList(persistentInstances)
    }

    /**
     * 当前Cluster下是否没有任何一个实例？
     *
     * @return 如果没有持久节点，也没有临时节点，return true；不然return false
     */
    open fun isEmpty(): Boolean {
        return ephemeralInstances.isEmpty() && persistentInstances.isEmpty()
    }

    /**
     * 更新实例列表，统计的是当前节点对比之前的节点的变化，ips给出的是当前的实例列表
     *
     * @param ips 当前的实例列表
     * @param ephemeral 要更新的是临时节点?还是持久节点?
     */
    open fun updateIps(ips: List<NamingInstance>, ephemeral: Boolean) {
        var toUpdateInstances = if (ephemeral) ephemeralInstances else persistentInstances

        // 1.统计ip的状态变化 TODO

        // 2.统计新增的ip
        val newIps = subtract(ips, toUpdateInstances)
        // 3.统计已经死亡的ip
        val deadIps = subtract(toUpdateInstances, ips)

        // 更新实例列表...将新的实例变更为给定的ips列表
        toUpdateInstances = HashSet(ips)
        if (ephemeral) {
            ephemeralInstances = toUpdateInstances
        } else {
            persistentInstances = toUpdateInstances
        }
    }

    /**
     * subtract(Note: 计算减法)，计算"oldIps-newIps"的结果(也就是计算oldIps当中比newIps当中多了哪些实例？)
     *
     * @param oldIps oldIps
     * @param newIps newIps
     * @return 所有oldIps相比与newIps当中新增的节点列表
     */
    private fun subtract(oldIps: Collection<NamingInstance>, newIps: Collection<NamingInstance>): List<NamingInstance> {
        // 先统计一下newIps当中的的ip和port作为key
        val newIpsMap = HashMap<String, NamingInstance>()
        newIps.forEach { newIpsMap["${it.ip}:${it.port}"] = it }
        // 统计所有oldIps比newIps当中多的实例...(也就是过滤出来所有不在newIps当中的实例)
        return oldIps.filter { !newIpsMap.containsKey("${it.ip}:${it.port}") }.toList()
    }
}