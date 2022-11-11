package com.wanna.boot.properties

import com.wanna.boot.SpringBootConfiguration
import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.context.properties.NestedConfigurationProperty
import com.wanna.boot.runSpringApplication

@EnableConfigurationProperties([Properties::class])
@SpringBootConfiguration(proxyBeanMethods = false)
class PropertiesTest {

}

@ConfigurationProperties(prefix = "com.wanna")
class Properties {
    var prop: String? = null

    @NestedConfigurationProperty
    var user: User? = null
}

class User {
    var id: String? = null
    var name: String? = null
}

fun main() {
    val application =
        runSpringApplication<PropertiesTest>("--com.wanna.prop=123","--com.wanna.user.id=1","--com.wanna.user.name=wanna")
    val properties = application.getBean(Properties::class.java)
    println(properties)
}