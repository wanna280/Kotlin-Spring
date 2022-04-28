package com.wanna

import com.wanna.framework.aop.*
import com.wanna.framework.aop.annotaions.EnableAspectJAutoProxy
import com.wanna.framework.aop.creator.AnnotationAwareAspectJAutoProxyCreator
import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation
import com.wanna.framework.beans.annotations.Bean
import com.wanna.framework.beans.annotations.Component
import com.wanna.framework.beans.annotations.Configuration
import com.wanna.framework.context.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotations.ComponentScan
import com.wanna.framework.test.ITF
import com.wanna.framework.test.User
import com.wanna.framework.util.ClassUtils
import java.util.*
import kotlin.collections.HashMap

@ComponentScan(["com.wanna"])
@Configuration
@EnableAspectJAutoProxy
class App(val advisor: Advisor) {

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
                return object : ClassFilter {
                    override fun matches(clazz: Class<*>): Boolean {
                        return ClassUtils.isAssginFrom(ITF::class.java, clazz)
                    }
                }
            }

            override fun getMethodMatcher(): MethodMatcher {
                return MethodMatcher.TRUE
            }
        }
    }
}

fun main() {
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