package com.wanna.test.event

import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.util.ClassUtils

class MyApplicationEvent(source: Any?) : ApplicationEvent(source)

class MyApplicationListener : SmartApplicationListener {
    override fun onApplicationEvent(event: ApplicationEvent) = println("event=$event")

    override fun supportEventType(eventType: Class<out ApplicationEvent>): Boolean {
        if (ClassUtils.isAssignFrom(MyApplicationEvent::class.java, eventType)) {
            return true
        }
        return false
    }
}

fun main() {
    val applicationContext = AnnotationConfigApplicationContext()
    applicationContext.refresh()
    applicationContext.addApplicationListener(MyApplicationListener())
    applicationContext.publishEvent(MyApplicationEvent(applicationContext))
}