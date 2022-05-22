package com.wanna.cloud.nacos.test.registry

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.method.annotation.RequestMapping

@SpringBootApplication
class RegistryTest

@RestController
class MyController {
    @RequestMapping(["/user"])
    fun user(): String {
        println("request")
        return "wanna"
    }
}

fun main() {
    val applicationContext = runSpringApplication<RegistryTest>()
    println(applicationContext)
}