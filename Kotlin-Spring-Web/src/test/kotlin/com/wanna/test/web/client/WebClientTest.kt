package com.wanna.test.web.client

import com.wanna.framework.web.client.RestTemplate

class WebClientTest {

}

fun main() {
    val restTemplate = RestTemplate()
//    val forObject = restTemplate.getForObject("http://127.0.0.1:9887/user", String::class.java, emptyMap())
//
//    println(forObject)
    val forObject2 = restTemplate.getForObject(
        "http://localhost:9966/v1/ns/instance/list?serviceName=wanna&port=9999&ip=127.0.0.1", String::class.java,
        emptyMap()
    )
    println(forObject2)
}