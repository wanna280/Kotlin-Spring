package com.wanna.framework.context

/**
 * 这是一个声明周期的处理器
 */
interface LifecycleProcessor : Lifecycle {
    /**
     * 刷新的回调
     */
    fun onRefresh()

    /**
     * 关闭的回调
     */
    fun onClose()
}