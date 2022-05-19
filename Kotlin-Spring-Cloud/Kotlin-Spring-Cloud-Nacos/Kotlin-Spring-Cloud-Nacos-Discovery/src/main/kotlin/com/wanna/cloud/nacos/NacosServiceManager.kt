package com.wanna.cloud.nacos

import com.alibaba.nacos.api.NacosFactory
import com.alibaba.nacos.api.naming.NamingService
import java.util.Properties

/**
 * Nacos的Service管理器，它主要支持去进行NamingService的管理工作；
 * 不管是对于Nacos的服务的注册、还是去提供Nacos服务的发现，都需要用到NamingService去进行操作，这个组件的作用就是去提供NamingService的相关操作
 */
open class NacosServiceManager {

    private var namingService: NamingService? = null

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
}