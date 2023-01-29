package com.wanna.nacos.test.server

import com.wanna.boot.runSpringApplication
import com.wanna.nacos.naming.server.NacosNamingServer

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
class NamingServerTest {
}

fun main() {
    val applicationContext = runSpringApplication<NacosNamingServer>()
    println(applicationContext)
}