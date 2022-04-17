package com.wanna

import com.wanna.framework.beans.annotations.Configuration
import com.wanna.framework.context.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotations.ComponentScan
import com.wanna.framework.context.processor.factory.internal.ConfigurationClassPostProcessor
import com.wanna.framework.test.AppConfiguration
import com.wanna.framework.test.User
import java.util.Scanner

@ComponentScan(["com.wanna"])
@Configuration
class App

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(App::class.java)
    val bean = applicationContext.getBean("user") as User
    println(bean.phone)
    println(bean.applicationContext)
}

fun scan() {
    val scanner = Scanner(System.`in`)
    while (true) {
        print(">")
        val nextLine = scanner.nextLine()
        println(nextLine)
    }
}