package com.wanna.framework.aop.target

import com.wanna.framework.aop.TargetSource

/**
 * 这是一个空的TargetSource
 */
open class EmptyTargetSource : TargetSource {

    companion object {
        @JvmField
        val INSTANCE = EmptyTargetSource()
    }

    override fun getTargetClass(): Class<*>? {
        return null
    }

    override fun isStatic(): Boolean {
        return false
    }

    override fun getTarget(): Any? {
        return null
    }

    override fun releaseTarget(target: Any?) {

    }
}