package com.wanna.cloud.nacos.config.test

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.runSpringApplication
import com.wanna.cloud.context.config.annotation.RefreshScope
import com.wanna.framework.context.stereotype.Component
import java.util.concurrent.TimeUnit

@EnableConfigurationProperties
@SpringBootApplication
open class NacosConfigTest

@RefreshScope
@Component
@ConfigurationProperties("my.user")
class User {
    var name: String? = null
}

fun main() {
    val applicationContext = runSpringApplication<NacosConfigTest>()
    while (true) {
        val user = applicationContext.getBean(User::class.java)
        println(user.name)
        TimeUnit.SECONDS.sleep(5)
    }
}