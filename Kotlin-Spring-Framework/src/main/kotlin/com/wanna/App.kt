package com.wanna

import com.wanna.framework.aop.creator.AnnotationAwareAspectJAutoProxyCreator
import com.wanna.framework.beans.annotations.Configuration
import com.wanna.framework.context.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotations.ComponentScan
import com.wanna.framework.test.ITF
import com.wanna.framework.test.User
import java.util.*

@ComponentScan(["com.wanna"])
@Configuration
class App

fun main() {
    val applicationContext = AnnotationConfigApplicationContext()
    applicationContext.addBeanPostProcessor(AnnotationAwareAspectJAutoProxyCreator())
    applicationContext.register(App::class.java)
    applicationContext.refresh()
    val bean = applicationContext.getBean("user") as User
    println(bean.phone)
    println(bean.applicationContext)

    val itf = applicationContext.getBean(ITF::class.java)
    val user = itf!!.getUser("wanna")
    println(user)
}

fun scan() {
    val scanner = Scanner(System.`in`)
    while (true) {
        print(">")
        val nextLine = scanner.nextLine()
        println(nextLine)
    }
}