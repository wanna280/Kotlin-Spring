package com.wanna.test.async

import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.scheduling.annotation.AsyncConfigurer
import com.wanna.framework.scheduling.annotation.EnableScheduling
import com.wanna.framework.scheduling.annotation.Scheduled
import com.wanna.framework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@EnableScheduling
@Configuration(proxyBeanMethods = false)
open class ScheduledTest {

    @Scheduled(initialDelay = 5000L, fixedDelay = 5000L)
    open fun schedule() {
        println("----" + Date(System.currentTimeMillis()))
        TimeUnit.SECONDS.sleep(10L)
    }

    @Scheduled(initialDelay = 5000L, fixedRate = 5000L)
    open fun schedule2() {
        println("[${Thread.currentThread().name}]" + Date(System.currentTimeMillis()))
        TimeUnit.SECONDS.sleep(10L)
    }

    @Bean(ScheduledAnnotationBeanPostProcessor.DEFAULT_TASK_SCHEDULER_BEAN_NAME)
    open fun taskScheduler(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(4)
    }

    @Bean
    open fun configurer() : AsyncConfigurer {
        return object :AsyncConfigurer {

        }
    }
}

fun main() {
    AnnotationConfigApplicationContext(ScheduledTest::class.java)
}