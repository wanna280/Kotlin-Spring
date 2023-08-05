package com.wanna.framework.aop

import java.lang.reflect.Method

/**
 * 不对方法去进行匹配, 永远返回true进行放行, 作为单例对象使用
 */
object TrueMethodMatcher : MethodMatcher {

    override fun matches(method: Method, targetClass: Class<*>): Boolean = true

    override fun isRuntime(): Boolean = false

    /**
     * 运行时去匹配方法, 不支持这种方式, 既然isRuntime=false, 那么这个方法不应该被调用
     */
    override fun matches(method: Method, targetClass: Class<*>, vararg args: Any?): Boolean {
        throw java.lang.UnsupportedOperationException()
    }
}