package com.wanna.nacos.client.config.test

import com.wanna.nacos.api.NacosFactory
import com.wanna.nacos.api.config.listener.Listener
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
class ConfigTest {
}

fun main() {
    val configService = NacosFactory.createConfigService(Properties())
    configService.addListener("test", "wanna", object : Listener {
        override fun getExecutor(): Executor {
            return Executors.newSingleThreadExecutor()
        }

        override fun receiveConfigInfo(configInfo: String) {
            println(configInfo)
        }
    })
    println(configService)
}