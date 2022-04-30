package com.wanna.boot.test

import com.wanna.boot.SpringApplication
import com.wanna.framework.context.annotation.Configuration

@Configuration
class App

fun main(vararg args: String) {
    val applicationContext = SpringApplication.run(App::class.java, *args)
    println(applicationContext.getBeanDefinitionNames())
}