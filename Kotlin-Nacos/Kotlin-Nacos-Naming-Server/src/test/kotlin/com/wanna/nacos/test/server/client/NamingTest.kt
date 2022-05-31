package com.wanna.nacos.test.server.client

import com.wanna.framework.web.client.RestTemplate
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NamingTest {
}

const val serviceName = "wanna"
const val ip = "127.0.0.1"
const val port = 9999

fun main() {
    val scheduledService = ScheduledThreadPoolExecutor(1)
    val restTemplate = RestTemplate()
    val params = mapOf("serviceName1" to serviceName, "ip" to ip, "port" to port.toString())
    val forObject = restTemplate.getForObject(
        "http://localhost:9966/v1/ns/instance/register", String::class.java,
        params
    )
    scheduledService.scheduleWithFixedDelay({
        val ping =
            restTemplate.getForObject("http://localhost:9966/v1/ns/instance/beat", String::class.java, params)
        println("ping->[$ping]")
    }, 5L, 5L, TimeUnit.SECONDS)

    println(forObject)

}