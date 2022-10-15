package com.wanna.framework.transaction.support

abstract class ResourceHolderSupport : ResourceHolder {

    private var timeout: Long = -1L

    override fun reset() {

    }

    override fun unbound() {

    }

    open fun setTimeoutInSeconds(seconds: Int) {
        this.timeout = seconds * 1000L
    }

    open fun getTimeInMillSeconds() = this.timeout

    open fun getTimeInSeconds() = this.timeout

    override fun isVoid(): Boolean {
        return false
    }

    open fun released() {

    }
}