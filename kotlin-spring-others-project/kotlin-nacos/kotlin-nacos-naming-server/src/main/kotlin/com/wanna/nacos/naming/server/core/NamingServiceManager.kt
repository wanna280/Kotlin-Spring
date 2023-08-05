package com.wanna.nacos.naming.server.core

import com.wanna.framework.context.stereotype.Component
import com.wanna.nacos.naming.server.core.NamingServiceManager.NamingInstanceAction.ADD
import com.wanna.nacos.naming.server.core.NamingServiceManager.NamingInstanceAction.REMOVE
import com.wanna.common.logging.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * NamingService的Manager, 负责管理Nacos的NamingService以及NamingInstance
 */
@Component
open class NamingServiceManager {
    companion object {
        private val logger = LoggerFactory.getLogger(NamingServiceManager::class.java)
    }

    // NamingInstance的操作类型, ADD(添加), REMOVE(移除)
    enum class NamingInstanceAction { ADD, REMOVE }

    // serviceMap  (namespaceId-->(groupName::serviceName-->NamingService))
    // 根据namespaceId可以去获取到该namespace下的NamingService列表
    // 在获取到NamingService列表之后, 可以根据groupName::serviceName去获取到具体的NamingService
    private val serviceMap = ConcurrentHashMap<String, MutableMap<String, NamingService>>()

    //-------------------------------for obtain NamingInstance-------------------------------------------------------------------

    /**
     * 给定namespaceId, serviceName, clusterName, ip和port, 去获取一个实例
     * * 1.通过namespaceId和serviceName可以去获取到NamingService
     * * 2.根据clusterName可以去NamingService下去获取到该Cluster下的全部实例
     * * 3.根据ip和port可以从该集群下的所有实例当中去进行匹配
     *
     * @param namespaceId namespaceId
     * @param serviceName serviceName
     * @param clusterName clusterName
     * @param ip ip
     * @param port port
     * @return 如果根据全部的属性去找到了合适的NamingInstance, return; 如果没有找到, return null
     */
    open fun getInstance(
        namespaceId: String,
        serviceName: String,
        clusterName: String,
        ip: String,
        port: Int
    ): NamingInstance? {
        val service = getService(namespaceId, serviceName) ?: return null
        val allIps = service.allIps(listOf(clusterName))
        allIps.forEach {
            if (it.ip == ip && it.port == port) {
                return it
            }
        }
        return null
    }

    //-------------------------------for register NamingInstance-------------------------------------------------------------------

    /**
     * 注册一个Nacos的NamingInstance, 根据namespaceId和serviceName可以寻找到NamingService;
     * 在找到NamingService之后, 可以通过NamingService去完成NamingInstance的注册
     *
     * @param namespaceId namespaceId
     * @param serviceName serviceName
     * @param instance 要去进行注册的NamingInstance
     */
    open fun registerInstance(namespaceId: String, serviceName: String, instance: NamingInstance) {
        // 如果之前不存在当前Service的话, 创建一个空的NamingService
        createEmptyServiceIfNecessary(namespaceId, serviceName)

        // 检验一下Service是否已经创建成功? 获取Service失败时抛出异常
        getService(namespaceId, serviceName)
            ?: throw IllegalStateException("NamingService没有找到[namespaceId=$namespaceId, serviceName=$serviceName]")

        // 添加一个NamingInstance到NamingService当中
        addInstance(namespaceId, serviceName, instance.ephemeral, instance)
    }

    /**
     * 注册一个Nacos的NamingInstance, 根据namespaceId和serviceName可以寻找到NamingService;
     * 在寻找到NamingService之后, 可以通过NamingService去完成给定的NamingInstance列表的注册
     *
     * @param namespaceId namespaceId
     * @param serviceName serviceName
     * @param ips 要去进行注册的NamingInstance列表
     */
    open fun addInstance(namespaceId: String, serviceName: String, ephemeral: Boolean, vararg ips: NamingInstance) {
        val service = getService(namespaceId, serviceName) ?: return
        synchronized(service) {
            // 获取到在添加ips完成的实例之后的NamingInstance列表
            val instanceList = addIpAddresses(service, ephemeral, *ips)
            val instances = NamingInstances(ArrayList(instanceList))
            // 更新实例列表(instancesList当中为该NamingService下的最新NamingInstance列表)
            service.updateIPs(instances.instanceList, ephemeral)
        }
    }

    /**
     * 从指定的Service当中获取到所有(临时/持久)的NamingInstance列表, 并添加ips当中指定的所有NamingInstance到其中去进行返回
     *
     * @param service NamingService
     * @param ephemeral 要获取的是临时节点还是持久节点
     * @param ips 要添加的NamingInstance列表
     * @return Service当中的NamingInstance, 合并完ips当中的NamingInstance之后的结果
     */
    private fun addIpAddresses(
        service: NamingService, ephemeral: Boolean, vararg ips: NamingInstance
    ): List<NamingInstance> {
        return updateIpAddresses(service, ephemeral, ADD, *ips)
    }

    //-------------------------------for remove NamingInstance-------------------------------------------------------------------

    /**
     * 注册一个Nacos的NamingInstance, 根据namespaceId和serviceName可以寻找到NamingService;
     * 在找到NamingService之后, 可以通过NamingService去完成NamingInstance的移除...
     *
     * @param namespaceId namespaceId
     * @param serviceName serviceName
     * @param ips 要去进行移除的NamingInstance列表
     */
    open fun removeInstance(namespaceId: String, serviceName: String, ephemeral: Boolean, vararg ips: NamingInstance) {
        val service = getService(namespaceId, serviceName) ?: return
        synchronized(service) {
            removeInstance(service, ephemeral, *ips)
        }
    }

    /**
     * 从当前给定的NamingService当中去移除指定的NamingInstance列表
     *
     * @param service 要进行操作的NamingService
     * @param ephemeral 要操作的是临时节点? 还是持久节点? 
     * @param ips 要从NamingService当中移除的实例列表
     */
    private fun removeInstance(service: NamingService, ephemeral: Boolean, vararg ips: NamingInstance) {
        // 将ips当中的全部NamingInstance从Service的NamingInstance列表当中移除掉...
        val instanceList = removeIpAddresses(service, ephemeral, *ips)
        val instances = NamingInstances(instanceList)
        // 更新Service当中的NamingInstance列表(instanceList是最新的NamingInstance列表)
        service.updateIPs(instances.instanceList, ephemeral)
    }

    /**
     * 获取到指定的NamingService当中移除了指定的实例列表(ips)之后的NamingInstance列表
     *
     * @param service 要去进行操作的NamingService
     * @param ephemeral 要操作的是临时节点? 还是持久节点
     * @param ips 要从NamingService当中移除的实例列表
     * @return 从NamingService当中的所有实例列表当中, 移除了指定的实例列表(ips)之后的结果
     */
    private fun removeIpAddresses(
        service: NamingService, ephemeral: Boolean, vararg ips: NamingInstance
    ): List<NamingInstance> {
        return updateIpAddresses(service, ephemeral, REMOVE, *ips)
    }


    /**
     * 更新当前NamingService下的NamingInstance列表, 可以是ADD/REMOVE两种类型的操作
     *
     * @param service NamingService
     * @param ephemeral 要更新临时节点还是非临时节点? 
     * @param action 操作类型(ADD/REMOVE)
     * @param ips 要进行操作的NamingInstance列表(可以是要进行删除的NamingInstance列表, 也可以是要进行添加的NamingInstance列表)
     * @return ADD/REMOVE之后的NamingInstance列表
     */
    open fun updateIpAddresses(
        service: NamingService, ephemeral: Boolean, action: NamingInstanceAction, vararg ips: NamingInstance
    ): List<NamingInstance> {
        service.validate()

        // 查询到当前Service下的所有Cluster下的所有的NamingInstance列表
        val allIps = service.allIps(ephemeral)

        // 根据所有的实例列表, 去构建InstanceMap, key-ip&port, value-NamingInstance, 方便去进行判断之前是否已经存在有该实例? 
        val currentNamingInstancesMap = HashMap<String, NamingInstance>()
        allIps.forEach { currentNamingInstancesMap[it.toIpAddress()] = it }

        // 遍历所有要去进行操作的实例列表
        // 如果操作是REMOVE, 那么从InstanceMap当中移除掉
        // 如果操作是ADD, 那么将它添加到InstanceMap当中(如果已经存在了, 那么替换)
        ips.forEach {
            // 如果之前还没存在有这个Cluster, 那么先去创建该Cluster
            if (!service.clusterMap.containsKey(it.clusterName)) {
                val cluster = NamingCluster(it.clusterName, service)
                cluster.init()
                service.clusterMap[it.clusterName] = cluster
            }
            if (action == REMOVE) {
                currentNamingInstancesMap -= it.toIpAddress()
            } else {
                val old = currentNamingInstancesMap[it.toIpAddress()]
                // 如果之前已经存在过该实例的话, 沿用之前的instanceId
                if (old != null) {
                    it.instanceId = old.instanceId
                }

                // 如果之前已经存在有该实例, 那么替换掉实例...
                // 如果之前没有该实例, 那么直接添加该实例...
                currentNamingInstancesMap[it.toIpAddress()] = it
            }
        }
        // 返回新的NamingInstance列表(ADD/REMOVE操作之后)
        return ArrayList(currentNamingInstancesMap.values)
    }

    //-------------------------------for operating NamingService-------------------------------------------------------------------

    /**
     * 如果必要的话, 创建一个NamingService并完成Service的初始化, 加入到serviceMap当中
     *
     * @param namespaceId namespaceId
     * @param serviceName serviceName
     */
    open fun createEmptyServiceIfNecessary(namespaceId: String, serviceName: String) {
        createServiceIfNecessary(namespaceId, serviceName, null)
    }

    /**
     * 如果必要的话, 创建一个NamingService并完成Service的初始化, 加入到serviceMap当中
     *
     * @param namespaceId namespaceId
     * @param serviceName serviceName
     * @param cluster 想要添加到新创建的NamingService当中的Cluster
     */
    open fun createServiceIfNecessary(namespaceId: String, serviceName: String, cluster: NamingCluster?) {
        var service = getService(namespaceId, serviceName)
        if (service == null) {
            if (logger.isDebugEnabled) {
                logger.debug("创建一个空的NamingService[name=$serviceName]")
            }
            service = NamingService()
            service.set(namespaceId, serviceName, serviceName)
            // 如果给定的NamingCluster的话, 将NamingCluster注册到当前给定的NamingService下
            if (cluster != null) {
                cluster.service = service
                service.clusterMap[cluster.clusterName] = cluster
            }
            // putService, 完成init的初始化工作
            putServiceAndInit(service)
        }
    }


    /**
     * 根据namespaceId和serviceName去获取到对应的NamingService
     *
     * @param namespaceId namespaceId
     * @param serviceName serviceName
     * @return NamingService(如果不存在的话, return null)
     */
    open fun getService(namespaceId: String, serviceName: String): NamingService? {
        return chooseServiceMap(namespaceId)?.get(serviceName)
    }

    /**
     * 根据namespaceId去获取到该namespace下的所有的NamingService
     *
     * @param namespaceId namespaceId
     * @return ServiceMap(key-serviceName,value-NamingService), 如果不存在的话, return null
     */
    open fun chooseServiceMap(namespaceId: String): Map<String, NamingService>? {
        return serviceMap[namespaceId]
    }

    /**
     * 在创建完一个NamingService之后, 需要将它添加到serviceMap当中
     *
     * @param service 你想要添加的NamingService
     */
    open fun putService(service: NamingService) {
        // putService之前, 先去验证Service的数据的合法性
        service.validate()
        // 使用DCL的方式去添加一个Namespace对应Service列表的ConcurrentHashMap
        if (!serviceMap.containsKey(service.namespaceId)) {
            synchronized(serviceMap) {
                if (!serviceMap.containsKey(service.serviceName)) {
                    serviceMap[service.namespaceId] = ConcurrentHashMap()
                }
            }
        }
        // 添加NamingService到serviceMap当中...
        serviceMap[service.namespaceId]!![service.serviceName] = service
    }

    /**
     * 添加一个NamingService到ServiceMap当中, 并完成NamingService的初始化
     *
     * @param service NamingService
     */
    open fun putServiceAndInit(service: NamingService) {
        // 验证Service的数据合法性
        service.validate()

        // 注册一个Service到ServiceMap当中
        putService(service)

        // 完成Service的初始化工作, 并添加Service下的心跳检测机制
        service.init()
    }
}