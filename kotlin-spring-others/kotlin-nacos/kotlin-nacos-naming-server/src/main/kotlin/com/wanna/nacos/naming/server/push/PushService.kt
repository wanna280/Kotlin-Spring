package com.wanna.nacos.naming.server.push

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.stereotype.Component


/**
 * 用于完成推送的Service, 是一个SpringApplication的[ApplicationListener],
 * 当[ServiceChangeEvent]事件触发时需要告知所有的客户端，服务的状态已经发生了改变
 *
 * @see ServiceChangeEvent
 */
@Component
class PushService : ApplicationListener<ServiceChangeEvent>, ApplicationContextAware {

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    /**
     * 设置[ApplicationContext]
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * 处理[ServiceChangeEvent]事件
     *
     * @param event event
     */
    override fun onApplicationEvent(event: ServiceChangeEvent) {
        val service = event.service
    }
}