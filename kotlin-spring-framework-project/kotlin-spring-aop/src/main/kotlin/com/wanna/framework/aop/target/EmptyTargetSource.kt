package com.wanna.framework.aop.target

import com.wanna.framework.aop.TargetSource
import com.wanna.framework.lang.Nullable

/**
 * 空的TargetSource单例对象
 *
 * @see TargetSource
 */
object EmptyTargetSource : TargetSource {

    @Nullable
    override fun getTargetClass(): Class<*>? = null

    override fun isStatic(): Boolean = false

    @Nullable
    override fun getTarget(): Any? = null

    override fun releaseTarget(@Nullable target: Any?) {}
}