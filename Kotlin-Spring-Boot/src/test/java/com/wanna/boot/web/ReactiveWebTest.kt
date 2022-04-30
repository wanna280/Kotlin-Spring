package com.wanna.boot.web

import com.wanna.boot.SpringApplication
import com.wanna.boot.SpringBootConfiguration
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.stereotype.Component

@ComponentScan(["com.wanna.boot.web"])
@SpringBootConfiguration
class AppTest

@Component
class MyReactiveWebServerFactory : com.wanna.boot.web.reactive.server.ReactiveWebServerFactory {
    override fun getWebServer(): WebServer {
        return object : WebServer {
            override fun start() {

            }

            override fun stop() {

            }

            override fun getPort(): Int {
                return 8080
            }
        }
    }
}

fun main(vararg args: String) {
    val applicationContext = SpringApplication.run(AppTest::class.java, *args)
}