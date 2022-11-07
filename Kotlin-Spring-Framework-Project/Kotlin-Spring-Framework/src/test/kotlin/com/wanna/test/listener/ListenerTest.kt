package com.wanna.test.listener

import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
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
    fun onEvent(event: Event) {
        println(event)
    }
}

class Event

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(ListenerTest::class.java)
    applicationContext.publishEvent(PayloadApplicationEvent<Any>(applicationContext, Event()))

    applicationContext.publishEvent(Event())
}