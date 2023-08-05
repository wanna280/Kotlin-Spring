package com.wanna.nacos.api.notify

import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

/**
 * 用于[NotifyCenter]和[EventPublisher]去进行发布的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/15
 */
abstract class Event : Serializable {
    companion object {

        /**
         * AtomicInteger维护全局事件唯一的序号
         */
        @JvmStatic
        private val SEQUENCE = AtomicInteger(0)
    }

    /**
     * 当前事件的序号
     */
    val sequence = SEQUENCE.getAndIncrement()

    /**
     * 事件的作用域
     */
    var scope: String? = null
}