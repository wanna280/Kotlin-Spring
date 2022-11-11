package com.wanna.boot.listener

import com.wanna.boot.context.event.ApplicationReadyEvent
import com.wanna.boot.context.event.ApplicationStartingEvent
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.util.ClassUtils

class MyApplicationListener : SmartApplicationListener {
    override fun onApplicationEvent(event: ApplicationEvent) {
        println("MyApplicationListener-->$event")
    }

    override fun supportEventType(eventType: Class<out ApplicationEvent>): Boolean {
        return ClassUtils.isAssignFrom(ApplicationStartingEvent::class.java, eventType) ||
                ClassUtils.isAssignFrom(ApplicationReadyEvent::class.java, eventType)
    }
}