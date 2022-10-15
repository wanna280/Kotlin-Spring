package com.wanna.boot.listener

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ApplicationListener


@SpringBootApplication
class ListenerTest {

    @Bean
    fun listener1(): ApplicationListener<*> {
        return object : ApplicationListener<MyEvent> {
            override fun onApplicationEvent(event: MyEvent) {
                println("111")
            }
        }
    }
}

interface Event<E>

open class MyEvent : Event<ListenerTest>, ApplicationEvent(Any())


fun main() {
    val applicationContext = runSpringApplication<ListenerTest>()
    applicationContext.publishEvent(MyEvent())
}