package com.wanna.nacos.naming.server.push

import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.context.stereotype.Component


/**
 * 推送服务，告知所有的客户端，服务的状态已经发生了改变
 */
@Component
class PushService : SmartApplicationListener {
    override fun onApplicationEvent(event: ApplicationEvent) {

    }
    override fun supportEventType(eventType: Class<out ApplicationEvent>) = eventType == ServiceChangeEvent::class.java
}