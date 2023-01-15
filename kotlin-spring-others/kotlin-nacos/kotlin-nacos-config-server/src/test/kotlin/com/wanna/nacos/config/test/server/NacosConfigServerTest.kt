package com.wanna.nacos.config.test.server

import com.wanna.boot.runSpringApplication
import com.wanna.nacos.config.server.NacosConfigServer

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/16
 */
fun main() {
    runSpringApplication<NacosConfigServer>()
}