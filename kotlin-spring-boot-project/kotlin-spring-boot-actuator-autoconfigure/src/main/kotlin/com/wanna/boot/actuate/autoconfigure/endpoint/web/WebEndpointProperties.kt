package com.wanna.boot.actuate.autoconfigure.endpoint.web

import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.boot.context.properties.NestedConfigurationProperty

/**
 * 对Endpoint(Actuator)的配置信息去进行配置
 */
@ConfigurationProperties("management.endpoints.web")
open class WebEndpointProperties {
    var basePath = "/actuator"  // actuator base path

    // 要去进行暴露的endpoint
    @NestedConfigurationProperty
    var exposure = Exposure()

    // 是否要暴露发现页
    @NestedConfigurationProperty
    var discovery = Discovery()

    // 要去进行暴露的endpoint信息
    class Exposure {
        // 要去进行暴露的endpoint的id, 可以为"*"
        val include: Set<String> = emptySet()

        // 不去进行暴露的endpoint的id, 可以为"*"
        val exclude: Set<String> = emptySet()
    }

    // 是否要暴露发现页? 在发现页当中, 需要提供暴露的所有的endpoint的相关信息
    class Discovery {
        // 是否要开启DiscoveryPage? 
        var enabled = true
    }
}