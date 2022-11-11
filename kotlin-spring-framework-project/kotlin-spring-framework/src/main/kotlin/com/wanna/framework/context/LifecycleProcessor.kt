package com.wanna.framework.context

/**
 * 这是一个生命周期(Lifecycle)的处理器，它本质上也是一个Lifecycle组件；
 * * 1.在Spring容器刷新完成，会自动回调onRefresh方法完成LifecycleBean的启动；
 * * 2.在Spring容器关闭是时，会自动回调onClose方法回调LifecycleBean完成收尾工作
 *
 * @see Lifecycle
 * @see com.wanna.framework.context.support.DefaultLifecycleProcessor
 */
interface LifecycleProcessor : Lifecycle {
    /**
     * 刷新的回调，在Spring容器刷新完成时，会自动回调
     */
    fun onRefresh()

    /**
     * 关闭的回调，在Spring容器关闭时，会自动回调
     */
    fun onClose()
}