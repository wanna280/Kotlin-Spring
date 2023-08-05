package com.wanna.cloud.nacos

import com.alibaba.nacos.api.NacosFactory
import com.alibaba.nacos.api.naming.NamingMaintainService
import com.alibaba.nacos.api.naming.NamingService
import java.util.*

/**
 * Nacos的Service管理器, 它主要支持去进行NamingService的管理工作;
 * 不管是对于Nacos的服务的注册、还是去提供Nacos服务的发现, 都需要用到NamingService去进行操作, 这个组件的作用就是去提供NamingService的相关操作
 *
 * @see NamingService
 */
open class NacosServiceManager {

    /**
     * Nacos原生API的NamingService
     */
    private var namingService: NamingService? = null

    /**
     * Nacos原生API的NamingMaintainService
     */
    private var namingMaintainService: NamingMaintainService? = null

    /**
     * 根据properties配置信息去获取(或构建)NamingService
     *
     * @param properties 配置信息
     * @return NamingService
     */
    open fun getNamingService(properties: Properties): NamingService {
        var namingService = this.namingService
        if (namingService == null) {
            namingService = buildNamingService(properties)
            this.namingService = namingService
        }
        return namingService
    }

    /**
     * 获取Nacos原生API的NamingMaintainService, 用于提供对于实例状态信息的修改功能
     *
     * @param properties 配置信息
     * @return NamingMaintainService
     */
    open fun getNamingMaintainService(properties: Properties): NamingMaintainService {
        var namingMaintainService = this.namingMaintainService
        if (namingMaintainService == null) {
            namingMaintainService = buildNamingMaintainService(properties)
            this.namingMaintainService = namingMaintainService
        }
        return namingMaintainService
    }

    /**
     * Nacos的NamingService关闭
     *
     * @see NamingService.shutDown
     */
    open fun nacosNamingServiceShutdown() {
        this.namingService?.shutDown()
        this.namingService = null
        this.namingMaintainService = null
    }

    /**
     * 构建NacosClient的NamingService
     *
     * @param properties 配置信息
     * @return Nacos NamingService
     */
    private fun buildNamingService(properties: Properties): NamingService {
        if (this.namingService == null) {
            synchronized(NacosServiceManager::class.java) {
                if (this.namingService == null) {
                    this.namingService = NacosFactory.createNamingService(properties)
                }
            }
        }
        return this.namingService!!
    }

    /**
     * 构建Nacos的NamingMaintainService
     *
     * @param properties 配置信息
     * @return NamingMaintainService
     */
    private fun buildNamingMaintainService(properties: Properties): NamingMaintainService {
        if (this.namingMaintainService == null) {
            synchronized(NacosServiceManager::class.java) {
                if (this.namingMaintainService == null) {
                    this.namingMaintainService = NacosFactory.createMaintainService(properties)
                }
            }
        }
        return this.namingMaintainService!!;
    }
}