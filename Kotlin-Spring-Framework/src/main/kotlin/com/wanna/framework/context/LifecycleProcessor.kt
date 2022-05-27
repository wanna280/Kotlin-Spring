package com.wanna.framework.context

/**
 * 这是一个生命周期(Lifecycle)的处理器，它本质上也是一个Lifecycle组件
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