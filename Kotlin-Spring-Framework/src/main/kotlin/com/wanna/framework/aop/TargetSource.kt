package com.wanna.framework.aop

/**
 * 这是对Target对象的来源进行提供的方式
 */
interface TargetSource {

    fun getTargetClass(): Class<*>?

    fun isStatic(): Boolean

    fun getTarget(): Any?

    fun releaseTarget(target: Any?)
}