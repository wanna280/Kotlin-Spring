package com.wanna.boot.autoconfigure

import com.wanna.boot.SpringApplication
import com.wanna.boot.autoconfigure.condition.ConditionOnMissingClass
import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.boot.context.properties.ConstructorBinding
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.web.server.WebServer
import com.wanna.framework.context.stereotype.Component

@ConditionOnMissingClass(value = ["com.wanna.boot.autoconfigure.MyReactiveWebServerFactory1"])
@SpringBootApplication
@EnableConfigurationProperties([ConfigurationPropertiesConstructorBinding::class])
class ConditionTest

@ConfigurationProperties
class ConfigurationPropertiesConstructorBinding() {

    @ConstructorBinding
    constructor(name:String) : this()
}

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