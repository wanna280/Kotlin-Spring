package com.wanna.boot.test

import com.wanna.boot.ApplicationType
import com.wanna.boot.SpringApplication
import com.wanna.framework.context.annotation.Configuration

@Configuration
class App

fun main(vararg args: String) {
    val springApplication = SpringApplication(App::class.java)
    springApplication.setApplicationType(ApplicationType.NONE)
    val applicationContext = springApplication.run(*args)
    println(applicationContext.getBeanDefinitionNames())
}