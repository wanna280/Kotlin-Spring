package com.wanna.cloud.context.environment

import com.wanna.framework.context.event.ApplicationEvent

/**
 * SpringApplication的环境(Environment)发生改变的事件, 负责用来去通知所有的监听器, 当前的环境已经发生了改变, 可以对其去进行相关的后置处理回调
 *
 * @see com.wanna.framework.core.environment.Environment
 *
 * @param keys keys 环境当中发生改变的keys
 * @param context ApplicationContext(有可能是keys)
 */
open class EnvironmentChangeEvent(context: Any, val keys: Set<String>) : ApplicationEvent(context) {
    constructor(keys: Set<String>) : this(keys, keys)
}