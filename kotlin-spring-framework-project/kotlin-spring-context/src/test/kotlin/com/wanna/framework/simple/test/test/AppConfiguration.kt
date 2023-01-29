package com.wanna.framework.simple.test.test

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.ComponentScan

@Configuration(proxyBeanMethods = false)
@ComponentScan(["com.wanna.framework.simple.test.test"])
class AppConfiguration {

}

interface ITF {
    fun getUser(name: String): User
}

@Configuration(proxyBeanMethods = false)
class ITFImpl : ITF {
    override fun getUser(name: String): User {
        val user = User()
        println("get User")
        user.name = name
        return user
    }
}