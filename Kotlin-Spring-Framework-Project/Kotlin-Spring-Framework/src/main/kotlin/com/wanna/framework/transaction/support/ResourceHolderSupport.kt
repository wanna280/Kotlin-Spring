package com.wanna.framework.transaction.support

/**
 * 为ResourceHolder提供支持的类，为ResourceHolder各个方法的实现提供了模板实现
 *
 * @see ResourceHolder
 */
abstract class ResourceHolderSupport : ResourceHolder {

    private var timeout: Long = -1L

    override fun reset() {}

    override fun unbound() {}

    open fun setTimeoutInSeconds(seconds: Int) {
        this.timeout = seconds * 1000L
    }

    open fun getTimeInMillSeconds() = this.timeout

    open fun getTimeInSeconds() = this.timeout

    override fun isVoid(): Boolean = false

    open fun released() {}
}