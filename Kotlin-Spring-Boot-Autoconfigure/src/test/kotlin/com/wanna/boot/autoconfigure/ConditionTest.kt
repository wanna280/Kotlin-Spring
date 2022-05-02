package com.wanna.boot.autoconfigure

import com.wanna.boot.SpringApplication
import com.wanna.boot.autoconfigure.condition.ConditionOnBean
import com.wanna.boot.autoconfigure.condition.ConditionOnMissingClass
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.stereotype.Component

@ConditionOnMissingClass(value = ["com.wanna.boot.autoconfigure.MyReactiveWebServerFactory1"])
@ComponentScan(["com.wanna.boot.autoconfigure"])
@SpringBootApplication
@ConditionOnBean
class ConditionTest

class ConditionAutoConfiguration

class ConditionAutoConfiguration2

class ConditionAutoConfiguration3

@ConditionOnBean(value = [Bean1::class, Bean2::class])
@Configuration
class Bean1

@Configuration
class Bean2

class Bean3

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
    val applicationContext = SpringApplication.run(ConditionTest::class.java, *args)
    applicationContext.getBeansForType(Object::class.java).forEach(::println)
}