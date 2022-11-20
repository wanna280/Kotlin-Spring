package com.wanna.nacos.api.notify

import com.wanna.nacos.api.notify.listener.Subscriber
import java.util.concurrent.ConcurrentHashMap

/**
 * NotifyCenter, 用于去进行事件的发布
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/15
 */
class NotifyCenter {

    /**
     * 维护所有事件对应的[EventPublisher], Key-EventClassName, Value-该Event类型对应的EventPublisher
     */
    private val publisherMap = ConcurrentHashMap<String, EventPublisher>()

    companion object {

        /**
         * 单例NotifyCenter对象
         */
        @JvmStatic
        private val INSTANCE = NotifyCenter()

        /**
         * 发布事件
         *
         * @param event event
         */
        @JvmStatic
        fun publishEvent(event: Event): Boolean {
            return publishEvent(event::class.java, event)
        }

        /**
         * 发布事件
         *
         * @param eventClass eventClass
         * @param event event
         * @return 发布事件是否成功?
         */
        @JvmStatic
        fun publishEvent(eventClass: Class<*>, event: Event): Boolean {
            val eventPublisher = INSTANCE.publisherMap[eventClass.name]
            return eventPublisher?.publish(event) ?: false
        }

        @JvmStatic
        fun registerSubscriber(subscriber: Subscriber<*>) {
            addSubscriber(subscriber, subscriber.subscribeType())
        }

        /**
         * 添加给定的[Subscriber]到具体的Publisher当中
         *
         * @param subscriber subscriber
         * @param subscribeType subscribeType
         */
        @JvmStatic
        private fun addSubscriber(subscriber: Subscriber<*>, subscribeType: Class<out Event>) {
            val topic = subscribeType.name
            synchronized(NotifyCenter::class.java) {
                // TODO 这里Publisher需要支持SPI
                INSTANCE.publisherMap.putIfAbsent(topic, DefaultPublisher())
            }
            INSTANCE.publisherMap[topic]!!.addSubscriber(subscriber)
        }
    }
}