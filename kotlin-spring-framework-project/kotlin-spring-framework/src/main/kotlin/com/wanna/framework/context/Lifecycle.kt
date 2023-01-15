package com.wanna.framework.context

/**
 * 这是一个SpringBean生命周期的回调, 对于一个LifecycleBean, 生命周期全权交给Spring去管理启动和关闭
 *
 * @see LifecycleProcessor
 */
interface Lifecycle {

    /**
     * Lifecycle的开始运行的回调
     */
    fun start()

    /**
     * Lifecycle的停止运行的回调
     */
    fun stop()

    /**
     * 当前的LifecycleBean是否正在运行当中？
     *
     * @return 如果正在运行中, return true; 如果已经关闭了那么return false
     */
    fun isRunning(): Boolean
}