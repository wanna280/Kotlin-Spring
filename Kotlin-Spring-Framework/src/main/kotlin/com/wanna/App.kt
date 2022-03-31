package com.wanna

import com.wanna.framework.context.AnnotationConfigApplicationContext
import com.wanna.framework.context.processor.factory.internal.ConfigurationClassPostProcessor
import com.wanna.framework.context.util.ConfigurationClass
import com.wanna.framework.test.AppConfiguration
import com.wanna.framework.test.User

class App

fun main() {
    val applicationContext = AnnotationConfigApplicationContext()
    applicationContext.addBeanFactoryPostProcessor(ConfigurationClassPostProcessor())
    applicationContext.register(AppConfiguration::class.java)
    applicationContext.refresh()

    var bean = applicationContext.getBean("")
    println(bean)
}