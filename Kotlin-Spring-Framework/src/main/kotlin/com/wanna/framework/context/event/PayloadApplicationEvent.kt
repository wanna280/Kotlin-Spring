package com.wanna.framework.context.event

/**
 * 这是一个支持存放payload的ApplicationEvent，可以发布某些对象，但是对象不是ApplicationEvent类型的，这是用来做一层适配器的转换
 */
open class PayloadApplicationEvent<T>(source: Any?, private val payload: T) : ApplicationEvent(source) {
    /**
     * 获取payload
     */
    open fun getPayload(): T = this.payload
}