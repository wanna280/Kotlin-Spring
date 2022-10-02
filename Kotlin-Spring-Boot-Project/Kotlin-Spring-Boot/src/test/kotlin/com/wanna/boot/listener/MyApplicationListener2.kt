package com.wanna.boot.listener

import com.wanna.boot.context.event.ApplicationContextInitializedEvent
import com.wanna.boot.context.event.ApplicationEnvironmentPreparedEvent
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.util.ClassUtils

class MyApplicationListener2 : SmartApplicationListener {
    override fun onApplicationEvent(event: ApplicationEvent) {
        println("MyApplicationListener2-->$event")
    }

    override fun supportEventType(eventType: Class<out ApplicationEvent>): Boolean {
        return ClassUtils.isAssignFrom(ApplicationEnvironmentPreparedEvent::class.java, eventType) ||
                ClassUtils.isAssignFrom(ApplicationContextInitializedEvent::class.java, eventType)
    }
}