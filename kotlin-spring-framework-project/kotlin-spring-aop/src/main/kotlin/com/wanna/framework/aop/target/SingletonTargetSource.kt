package com.wanna.framework.aop.target

import com.wanna.framework.aop.TargetSource

/**
 * 这是一个单例的TargetSource
 */
class SingletonTargetSource(private var target: Any) : TargetSource {
    override fun getTargetClass(): Class<*>? {
        return target::class.java
    }

    override fun isStatic(): Boolean {
        return false
    }

    override fun getTarget(): Any? {
        return target
    }

    override fun releaseTarget(target: Any?) {

    }
}