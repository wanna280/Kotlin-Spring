package com.wanna.debugger.bistoury.instrument.proxy

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication

/**
 * Bistoury Proxy Application
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/6/24
 */
@SpringBootApplication(proxyBeanMethods = false)
open class BistouryProxyApplication

fun main(vararg args: String) {
    var applicationContext = runSpringApplication<BistouryProxyApplication>(*args)
    println(applicationContext)
}