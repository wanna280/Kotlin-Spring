package com.wanna.cloud.nacos.test.ribbon

import com.wanna.boot.ApplicationType
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.cloud.client.discovery.DiscoveryClient
import com.wanna.cloud.client.loadbalancer.LoadBalancerClient

@SpringBootApplication
class RibbonTest

fun main() {
    val applicationContext = runSpringApplication<RibbonTest>() {
        setApplicationType(ApplicationType.NONE)
    }

    val discoveryClient = applicationContext.getBean(DiscoveryClient::class.java)!!
    val instances = discoveryClient.getInstances("wanna")
    val loadBalancerClient = applicationContext.getBean(LoadBalancerClient::class.java)!!
    val serviceInstance = loadBalancerClient.choose("wanna")


    println(applicationContext)
}