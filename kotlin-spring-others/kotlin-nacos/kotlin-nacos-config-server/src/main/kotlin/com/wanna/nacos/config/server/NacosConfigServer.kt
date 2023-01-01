package com.wanna.nacos.config.server

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.scheduling.annotation.EnableScheduling

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
@EnableScheduling
@SpringBootApplication
class NacosConfigServer

fun main(vararg args: String) {
    val applicationContext = runSpringApplication<NacosConfigServer>(*args)
    println(applicationContext)
}