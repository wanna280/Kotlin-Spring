package com.wanna.framework.beans.factory.support

import java.lang.reflect.Method

/**
 * 这是一个运行时的方法替代
 */
fun interface MethodReplacer {

    /**
     * 对目标方法去进行重新实现
     */
    fun reimplement(obj: Any?, method: Method, args: Array<out Any?>?)
}