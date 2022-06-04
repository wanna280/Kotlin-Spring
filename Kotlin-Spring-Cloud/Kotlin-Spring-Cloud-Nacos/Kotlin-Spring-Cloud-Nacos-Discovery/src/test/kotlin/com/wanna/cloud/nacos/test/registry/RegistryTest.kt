package com.wanna.cloud.nacos.test.registry

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.web.bind.annotation.RestController
import com.wanna.framework.web.method.annotation.RequestMapping

@SpringBootApplication
class RegistryTest

@RestController
class MyController {

    @Autowired
    var array: Array<RegistryTest>? = null

    @RequestMapping(["/user"])
    fun user(): String {
        println("request")
        return "{\"name\":\"wanna\",\"clusters\":\"DEFAULT\",\"hosts\":{}}"
    }
}

fun main() {
    runSpringApplication<RegistryTest>("--spring.application.name=wanna", "--server.port=9999")
}