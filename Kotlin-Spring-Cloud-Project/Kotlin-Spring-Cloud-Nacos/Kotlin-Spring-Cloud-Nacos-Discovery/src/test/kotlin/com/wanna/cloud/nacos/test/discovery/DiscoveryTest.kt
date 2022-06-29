package com.wanna.cloud.nacos.test.discovery

import com.wanna.boot.ApplicationType
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.cloud.client.discovery.DiscoveryClient
import com.wanna.cloud.client.loadbalancer.LoadBalanced
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.web.client.RestTemplate

@SpringBootApplication
open class DiscoveryTest {

    @Bean
    @LoadBalanced
    open fun restTemplate() : RestTemplate {
        return RestTemplate()
    }
}



fun main() {
    val applicationContext = runSpringApplication<DiscoveryTest>() {
        this.setApplicationType(ApplicationType.NONE)
    }
    val discoveryClient = applicationContext.getBean(DiscoveryClient::class.java)
    val instances = discoveryClient.getInstances("wanna")
    println(instances.size)
    val services = discoveryClient.getServices()
    println(services)
}