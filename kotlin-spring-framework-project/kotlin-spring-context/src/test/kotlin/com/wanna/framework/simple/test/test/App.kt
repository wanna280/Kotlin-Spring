package com.wanna

import com.wanna.framework.beans.factory.annotation.BeanFactoryAnnotationUtils
import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.beans.factory.annotation.Value
import com.wanna.framework.context.annotation.*
import com.wanna.framework.simple.test.test.ITF
import com.wanna.framework.simple.test.test.User
import java.util.*

@ComponentScan(["com.wanna.framework.simple.test.test"])
@Configuration
@EnableAspectJAutoProxy
class App() {

    @Value("${'$'}{java.version}")
    val value: String? = null

    @Bean
    fun user2(): User {
        return User()
    }

    @Bean
    @Qualifier("user4")
    fun user3() : User {
        return User()
    }
}

fun main(vararg args: String) {
    val applicationContext = AnnotationConfigApplicationContext()

    applicationContext.register(App::class.java)
    applicationContext.refresh()
    val bean = applicationContext.getBean("user") as User
    println(bean.phone)
    println(bean.applicationContext)

    val beanOfType =
        BeanFactoryAnnotationUtils.qualifiedBeanOfType(applicationContext.getBeanFactory(), User::class.java, "user4")
    println(beanOfType)

    val itf = applicationContext.getBean(ITF::class.java)
    val user = itf.getUser("wanna")
}

fun scan() {
    val scanner = Scanner(System.`in`)
    while (true) {
        print(">")
        val nextLine = scanner.nextLine()
        println(nextLine)
    }
}