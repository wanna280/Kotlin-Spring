package com.wanna.test.test

import com.wanna.framework.beans.annotations.Configuration
import com.wanna.framework.context.annotations.ComponentScan

@Configuration
@ComponentScan(["com.wanna"])
class AppConfiguration {

}

interface ITF {
    fun getUser(name: String): User
}

@Configuration
class ITFImpl : ITF {
    override fun getUser(name: String): User {
        val user = User()
        println("get User")
        user.name = name
        return user
    }
}