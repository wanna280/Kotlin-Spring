package com.wanna.boot.test

import com.wanna.boot.SpringApplication
import com.wanna.boot.SpringBootConfiguration
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.stereotype.Component

@ComponentScan(["com.wanna.boot.web"])
@SpringBootConfiguration(proxyBeanMethods = false)
class AppTest

@Component
class MyReactiveNettyWebServerFactoryFactory : com.wanna.boot.web.mvc.server.NettyWebServerFactory {
    override fun getWebServer(): WebServer {
        return object : WebServer {
            override fun start() {

            }

            override fun stop() {

            }

            override fun getPort(): Int {
                return 8080
            }

            override fun setPort(port: Int) {

            }
        }
    }
}

fun main(vararg args: String) {
    val applicationContext = SpringApplication.run(AppTest::class.java, *args)
}