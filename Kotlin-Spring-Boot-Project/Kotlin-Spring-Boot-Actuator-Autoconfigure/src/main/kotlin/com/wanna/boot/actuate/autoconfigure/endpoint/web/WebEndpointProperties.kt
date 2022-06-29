package com.wanna.boot.actuate.autoconfigure.endpoint.web

import com.wanna.framework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
open class WebEndpointProperties {
    var basePath = "/actuator"  // actuator base path
}