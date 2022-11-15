package com.wanna.nacos.api.notify

import com.wanna.nacos.api.notify.listener.Subscriber

/**
 * 用于去进行事件发布的[EventPublisher]事件发布器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/15
 */
interface EventPublisher : AutoCloseable {

    /**
     * 添加一个[Subscriber]到当前的[EventPublisher]当中
     *
     * @param subscriber 需要去进行添加的
     */
    fun addSubscriber(subscriber: Subscriber<*>)

    /**
     * 从当前的[EventPublisher]当中去移除一个[Subscriber]
     *
     * @param subscriber 需要去进行移除的Subscriber
     */
    fun removeSubscriber(subscriber: Subscriber<*>)

    /**
     * 发布事件
     *
     * @param event event
     * @return 发布事件是否成功?
     */
    fun publish(event: Event): Boolean

    /**
     * 通知给定的这个Subscriber
     *
     * @param subscriber Listener
     * @param event event
     */
    fun notifySubscriber(subscriber: Subscriber<*>, event: Event)
}