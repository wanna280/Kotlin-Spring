package com.wanna.cloud.nacos.test.ribbon

import com.wanna.boot.ApplicationType
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.cloud.client.discovery.DiscoveryClient
import com.wanna.cloud.client.loadbalancer.LoadBalanced
import com.wanna.cloud.client.loadbalancer.LoadBalancerClient
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.web.client.RestTemplate

@SpringBootApplication
open class RibbonTest {
    @Bean
    @LoadBalanced
    open fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

fun main() {
    val applicationContext = runSpringApplication<RibbonTest>() {
        setApplicationType(ApplicationType.NONE)
    }

    val discoveryClient = applicationContext.getBean(DiscoveryClient::class.java)
    val instances = discoveryClient.getInstances("wanna")
    val loadBalancerClient = applicationContext.getBean(LoadBalancerClient::class.java)
    val serviceInstance = loadBalancerClient.choose("wanna")
    println(applicationContext)

    val restTemplate = applicationContext.getBean(RestTemplate::class.java)
    val entity = restTemplate.postForEntity("http://wanna/user?id=1","wanna", String::class.java, emptyMap())
    println(entity)
    println(restTemplate)
}