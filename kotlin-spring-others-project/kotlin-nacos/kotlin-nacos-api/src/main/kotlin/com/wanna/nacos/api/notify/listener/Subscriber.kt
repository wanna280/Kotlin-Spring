package com.wanna.nacos.api.notify.listener

import com.wanna.nacos.api.notify.Event
import java.util.concurrent.Executor

/**
 * 用于去处理事件的监听器Listener(Subscriber)
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/15
 *
 * @see Event
 */
abstract class Subscriber<T : Event> {

    /**
     * 真正地接收事件,  并对该事件去进行处理的Callback方法
     *
     * @param event 需要去进行处理的事件Event
     */
    abstract fun onEvent(event: T)

    /**
     * 支持去进行处理的事件类型
     *
     * @return 支持去进行处理的事件类型
     */
    abstract fun subscribeType(): Class<out Event>

    /**
     * 获取用于执行当前Listener的线程池
     *
     * @return 执行当前Listener的线程池(默认为null, 代表同步执行)
     */
    fun getExecutor(): Executor? = null

    /**
     * 是否需要去处理过期的事件(默认为false)
     *
     * @return 如果为true, 代表需要忽略过期事件; 为false则不应该去进行忽略
     */
    fun ignoreExpireEvent(): Boolean = false

    /**
     * 检查Scope是否匹配?
     *
     * @param event event
     * @return true代表匹配; false则代表不匹配(默认实现为scope为null时就算是匹配)
     */
    fun scopeMatches(event: T): Boolean = event.scope == null
}