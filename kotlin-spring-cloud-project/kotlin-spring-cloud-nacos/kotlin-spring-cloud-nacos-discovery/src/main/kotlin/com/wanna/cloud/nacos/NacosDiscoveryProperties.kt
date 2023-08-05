package com.wanna.cloud.nacos

import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.framework.beans.factory.annotation.Value
import java.util.*

/**
 * Nacos的服务注册/发现的使用到的相关的配置信息, 自动从配置文件当中去完成注入
 */
@ConfigurationProperties("spring.cloud.nacos.discovery")
open class NacosDiscoveryProperties {

    // serviceName, 也就是要注册到nacos的注册中心中的服务的名称...
    // (1)如果配置了spring.cloud.nacos.discovery.service的话, 那么使用
    // (2)fallback去寻找spring.application.name, 如果还没有找到, 那么只要空串可以用了...
    @Value("${'$'}{spring.cloud.nacos.discovery.service:${'$'}{spring.application.name:}}")
    var service: String = ""  // serviceName

    var serverAddr: String = "127.0.0.1:8848" // serverAddr

    var username: String = ""  // username

    var password: String = ""  // password

    var ip: String = "127.0.0.1"  // ip

    @Value("${'$'}{spring.cloud.nacos.discovery.port:${'$'}{server.port:9888}}")
    var port: Int = 9888  // port

    /**
     * 是否应该使用https去进行注册?
     */
    var secure: Boolean = false

    var ephemeral: Boolean = true  // 是否是一个临时节点, 默认为true(切换CP/AP)

    var weight: Double = 1.0  // weight

    var instanceEnabled: Boolean = true  // enabled?

    var group = "DEFAULT"  // group

    var clusterName: String = "DEFAULT"  // clusterName

    var heartBeatInterval: Int = -1  // 心跳间隔时间

    var heartBeatTimeout: Int = -1  // 心跳超时时间

    var ipDeleteTimeout: Int = -1  // 超时未响应就要删除实例的时间

    var metadata = HashMap<String, String>()  // extra metadata

    /**
     * 获取Nacos的NamingService需要用到的相关属性的Properties
     */
    open fun getNacosProperties(): Properties {
        val properties = Properties()
        properties["serverAddr"] = serverAddr
        properties["username"] = username
        properties["password"] = password
        properties["clusterName"] = clusterName
        return properties
    }
}