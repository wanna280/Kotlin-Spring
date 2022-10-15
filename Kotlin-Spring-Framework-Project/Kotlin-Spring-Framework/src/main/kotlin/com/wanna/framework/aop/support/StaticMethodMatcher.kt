package com.wanna.framework.aop.support

import com.wanna.framework.aop.MethodMatcher
import java.lang.reflect.Method

/**
 * 静态的方法匹配器，不需要在运行时去进行方法的匹配，只需要根据方法/类去进行匹配，不用去匹配一个方法需要传递的参数
 *
 * @see MethodMatcher
 */
abstract class StaticMethodMatcher : MethodMatcher {
    override fun isRuntime() = false

    override fun matches(method: Method, targetClass: Class<*>, vararg args: Any?) =
        throw UnsupportedOperationException("不合法的MethodMatcher的使用")
}