package com.wanna.nacos.naming.server

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
open class NacosNamingServer

fun main(vararg args: String) {
    runSpringApplication<NacosNamingServer>(*args)
}