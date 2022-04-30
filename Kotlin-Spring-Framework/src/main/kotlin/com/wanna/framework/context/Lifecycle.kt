package com.wanna.framework.context

/**
 * 这是一个声明周期的回调
 */
interface Lifecycle {

    /**
     * 开始运行
     */
    fun start()

    /**
     * 停止运行
     */
    fun stop()

    /**
     * 是否正在运行当中？
     */
    fun isRunning() : Boolean
}