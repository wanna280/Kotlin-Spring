package com.wanna.nacos.client.naming

import com.wanna.nacos.api.naming.NamingService
import java.util.*

/**
 * Nacos的[NamingService]的实现, 提供对于服务的管理
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
open class NacosNamingService() : NamingService {

    var properties: Properties? = null

    constructor(properties: Properties) : this() {
        this.properties = properties
    }

    constructor(serverList: String) : this() {

    }

    override fun registerInstance(serviceName: String, ip: String, port: Int) {
        TODO("Not yet implemented")
    }

    override fun registerInstance(serviceName: String, groupName: String, ip: String, port: Int) {
        TODO("Not yet implemented")
    }
}