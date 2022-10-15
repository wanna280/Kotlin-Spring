package com.wanna.boot.test.refresh

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.cloud.context.config.annotation.RefreshScope
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Scope
import com.wanna.framework.context.stereotype.Component

@SpringBootApplication
class RefreshScopeTest {

    @Bean
    @RefreshScope
    fun u(): User0 {
        return User0()
    }

    @Bean
    @Scope("prototype")
    fun up(): User0 {
        return User0()
    }
}

@Scope("prototype")
@Component("u0000")
class User0 {

}

fun main() {
    val applicationContext = runSpringApplication<RefreshScopeTest>()
    val bean1 = applicationContext.getBean("u")
    val registeredScope = applicationContext.getBeanFactory().getRegisteredScope("refresh")
    val refreshScope = registeredScope as com.wanna.cloud.context.scope.refresh.RefreshScope
    refreshScope.refreshAll()
    val bean2 = applicationContext.getBean("u")
    println("bean1==bean2?--->[${bean1 === bean2}]")

    val upBean1 = applicationContext.getBean("up")
    val upBean2 = applicationContext.getBean("up")
    println("upBean1==upBean2?--->[${upBean1 === upBean2}]")

    val u00001 = applicationContext.getBean("u0000")
    val u00002 = applicationContext.getBean("u0000")
    println("u00001==u00002?--->[${u00001 === u00002}]")
}