package com.wanna

import com.wanna.framework.aop.*
import com.wanna.framework.context.annotation.EnableAspectJAutoProxy
import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.beans.factory.annotation.Value
import com.wanna.framework.util.ClassUtils
import com.wanna.test.test.ITF
import com.wanna.test.test.User
import java.util.*

@ComponentScan(["com.wanna"])
@Configuration
@EnableAspectJAutoProxy
class App(val advisor: Advisor) {

    @Value("%{java.version}")
    val value: String? = null

    @Bean
    fun user2(advisor: Advisor): User {
        return User()
    }
}


@Component
class MyAdvisor : PointcutAdvisor {
    override fun getAdvice(): Advice {
        return object : MethodInterceptor {
            override fun invoke(invocation: MethodInvocation): Any? {
                println("mi-before")
                val returnVal = invocation.proceed()
                println("mi-after")
                return returnVal
            }
        }
    }

    override fun getPointcut(): Pointcut {
        return object : Pointcut {
            override fun getClassFilter(): ClassFilter {
                return ClassFilter { clazz -> ClassUtils.isAssignFrom(ITF::class.java, clazz) }
            }

            override fun getMethodMatcher(): MethodMatcher {
                return MethodMatcher.TRUE
            }
        }
    }
}

fun main(vararg args: String) {
    val applicationContext = AnnotationConfigApplicationContext()

    applicationContext.register(App::class.java)
    applicationContext.refresh()
    val bean = applicationContext.getBean("user") as User
    println(bean.phone)
    println(bean.applicationContext)

    val itf = applicationContext.getBean(ITF::class.java)
    val user = itf!!.getUser("wanna")
}

fun scan() {
    val scanner = Scanner(System.`in`)
    while (true) {
        print(">")
        val nextLine = scanner.nextLine()
        println(nextLine)
    }
}