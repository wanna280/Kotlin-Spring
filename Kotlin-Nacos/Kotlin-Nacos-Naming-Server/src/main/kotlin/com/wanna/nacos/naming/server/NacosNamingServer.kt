package com.wanna.nacos.naming.server

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication

@SpringBootApplication
class NacosNamingServer

fun main(vararg args: String) {
    runSpringApplication<NacosNamingServer>(*args)
}