package com.wanna.nacos.api.notify

import com.wanna.nacos.api.notify.listener.Subscriber
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 默认的[EventPublisher]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/16
 */
class DefaultPublisher : EventPublisher {

    private val subscribers = CopyOnWriteArrayList<Subscriber<*>>()


    override fun addSubscriber(subscriber: Subscriber<*>) {
        subscribers += subscriber
    }

    override fun close() {
        subscribers.clear()
    }

    override fun removeSubscriber(subscriber: Subscriber<*>) {
        subscribers.remove(subscriber)
    }

    override fun publish(event: Event): Boolean {
        subscribers.forEach { notifySubscriber(it, event) }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun notifySubscriber(subscriber: Subscriber<*>, event: Event) {
        val subscriber = subscriber as Subscriber<Event>
        if (subscriber.scopeMatches(event) && subscriber.subscribeType() == event::class.java) {
            val executor = subscriber.getExecutor()
            if (executor != null) {
                executor.execute { subscriber.onEvent(event) }
            } else {
                subscriber.onEvent(event)
            }
        }
    }
}