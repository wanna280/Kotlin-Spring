package com.wanna.cloud.openfeign.test.client

import com.wanna.boot.ApplicationType
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.cloud.openfeign.EnableFeignClients
import com.wanna.cloud.openfeign.FeignClient
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.bind.annotation.RequestBody
import com.wanna.framework.web.bind.annotation.RequestHeader
import com.wanna.framework.web.bind.annotation.RequestMapping

@EnableFeignClients(value = ["com.wanna.cloud.openfeign.test"])
@SpringBootApplication
open class FeignTest

@FeignClient(contextId = "wanna", path = "user")
interface MyFeignClient {
    @RequestMapping(method = [RequestMethod.POST], path = ["/"])
    fun ut(@RequestHeader("name") name: String, @RequestBody user: User): String
}

data class User(val id: String, val name: String)

fun main() {
    val applicationContext = runSpringApplication<FeignTest> {
        setApplicationType(ApplicationType.NONE)
    }
    val feignClient = applicationContext.getBean(MyFeignClient::class.java)
    println("FeignClient调用的结果为:[${feignClient.ut("wanna", User("1", "wanna"))}]")
}