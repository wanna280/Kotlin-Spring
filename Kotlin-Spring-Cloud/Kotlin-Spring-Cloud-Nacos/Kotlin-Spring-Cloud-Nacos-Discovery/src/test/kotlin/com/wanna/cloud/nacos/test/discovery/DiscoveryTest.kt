package com.wanna.cloud.nacos.test.discovery

import com.wanna.boot.ApplicationType
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.cloud.client.discovery.DiscoveryClient

@SpringBootApplication
class DiscoveryTest



fun main() {
    val applicationContext = runSpringApplication<DiscoveryTest>() {
        this.setApplicationType(ApplicationType.NONE)
    }
    val discoveryClient = applicationContext.getBean(DiscoveryClient::class.java)!!
    val instances = discoveryClient.getInstances("wanna")
    println(instances.size)
    val services = discoveryClient.getServices()
    println(services)
}