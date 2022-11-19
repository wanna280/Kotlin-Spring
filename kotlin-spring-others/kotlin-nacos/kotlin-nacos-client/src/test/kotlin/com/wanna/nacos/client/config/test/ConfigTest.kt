package com.wanna.nacos.client.config.test

import com.wanna.nacos.api.NacosFactory
import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.config.listener.Listener
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
fun main() {
    val properties = Properties()
    properties[PropertyKeyConst.SERVER_ADDR] = "localhost:9966"
    val configService = NacosFactory.createConfigService(properties)
    configService.addListener("wanna", "wanna", object : Listener {
        override fun getExecutor(): Executor {
            return Executors.newSingleThreadExecutor()
        }

        override fun receiveConfigInfo(configInfo: String) {
            println("配置文件发生变更---$configInfo")
        }
    })
    TimeUnit.MILLISECONDS.sleep(1000000L)
}