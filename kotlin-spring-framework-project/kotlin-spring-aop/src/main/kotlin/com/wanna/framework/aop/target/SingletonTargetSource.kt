package com.wanna.framework.aop.target

import com.wanna.framework.aop.TargetSource
import com.wanna.framework.lang.Nullable

/**
 * 单例的[TargetSource], 内部包装一个单例对象去进行实现
 *
 * @param target 要去进行包装的单例对象
 */
class SingletonTargetSource(private var target: Any) : TargetSource {

    override fun getTargetClass(): Class<*> = target::class.java

    override fun isStatic(): Boolean = false

    override fun getTarget(): Any = target

    override fun releaseTarget(@Nullable target: Any?) {}
}