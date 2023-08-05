package com.wanna.boot.test.listener

import com.wanna.boot.SpringApplication
import com.wanna.boot.autoconfigure.SpringBootApplication
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication(proxyBeanMethods = false)
class BootstrapListenerTest

fun main() {
    val map  = ConcurrentHashMap<String,Any>()
    val put = map.put("1", 2)
    println(put)

    SpringApplication.run(BootstrapListenerTest::class.java)
}