package com.wanna.framework.simple.test.listener

import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.EventListener
import com.wanna.framework.context.event.PayloadApplicationEvent
import com.wanna.framework.context.stereotype.Component

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/8
 */
@Component
class ListenerTest {

    @EventListener
    fun onMyEvent(event: MyEvent) {
        println("On MyEvent---$event")
    }

    @EventListener
    fun onEvent(event: Event) {
        println("On Event---$event")
    }

    @EventListener
    fun onApplicationEvent(event: ApplicationEvent) {
        println("On ApplicationEvent---$event")
    }
}

open class Event

open class MyEvent : Event()

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(ListenerTest::class.java)
    applicationContext.publishEvent(PayloadApplicationEvent<Any>(applicationContext, Event()))

    applicationContext.publishEvent(Event())

    applicationContext.publishEvent(PayloadApplicationEvent<Any>(applicationContext, MyEvent()))

    applicationContext.publishEvent(MyEvent())
}