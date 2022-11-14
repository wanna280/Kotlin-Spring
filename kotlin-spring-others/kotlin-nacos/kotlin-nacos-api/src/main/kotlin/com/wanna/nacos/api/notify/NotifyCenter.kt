package com.wanna.nacos.api.notify

/**
 * NotifyCenter, 用于去进行事件的发布
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/15
 */
class NotifyCenter {
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
        fun publishEvent(event: Event) {
            publishEvent(event::class.java, event)
        }

        @JvmStatic
        fun publishEvent(eventClass: Class<*>, event: Event) {

        }
    }
}