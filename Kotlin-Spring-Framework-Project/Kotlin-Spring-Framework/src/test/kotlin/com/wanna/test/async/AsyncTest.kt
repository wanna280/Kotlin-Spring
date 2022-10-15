package com.wanna.test.async

import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.scheduling.annotation.Async
import com.wanna.framework.scheduling.annotation.EnableAsync
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

@ComponentScan(["com.wanna.test.async"])
@EnableAsync
@Configuration(proxyBeanMethods = false)
open class AsyncTest {

    @Bean("taskExecutor")
    open fun taskExecutor(): Executor {
        return Executors.newSingleThreadExecutor()
    }

    @Bean
    open fun myExecutor(): Executor {
        return Executors.newSingleThreadExecutor(ThreadFactory {
            val thread = Thread(it)
            thread.name = "wanna-MyExecutor"
            return@ThreadFactory thread
        })
    }
}

@Async("myExecutor")
@Component
open class User {
    open fun u1(): CompletableFuture<Any?>? {
        TimeUnit.SECONDS.sleep(3L)
        println(Thread.currentThread().name)
        return null
    }

    @Async("taskExecutor")
    open fun u2(): Future<Any>? {
        TimeUnit.SECONDS.sleep(3L)
        println(Thread.currentThread().name)
        throw IllegalStateException("---")
    }
}

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(AsyncTest::class.java)
    val user = applicationContext.getBean(User::class.java)
    user.u1()
    user.u2()

}