package com.wanna.nacos.api.notify

import com.wanna.nacos.api.notify.listener.Subscriber

/**
 * 默认的[EventPublisher]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/16
 */
class DefaultPublisher : EventPublisher {

    override fun addSubscriber(subscriber: Subscriber<*>) {

    }

    override fun close() {

    }

    override fun removeSubscriber(subscriber: Subscriber<*>) {

    }

    override fun publish(event: Event): Boolean {
        return true
    }

    override fun notifySubscriber(subscriber: Subscriber<*>, event: Event) {

    }
}