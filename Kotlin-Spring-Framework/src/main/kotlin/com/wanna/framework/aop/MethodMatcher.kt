package com.wanna.framework.aop

import java.lang.reflect.Method

interface MethodMatcher {

    companion object {
        @JvmField
        val TRUE = TrueMethodMatcher.INSTANCE
    }

    /**
     * 匹配一个方法
     * @param method 要进行匹配的方法
     * @param targetClass 要进行匹配的目标类
     */
    fun matches(method: Method, targetClass: Class<*>): Boolean

    /**
     * 是否需要在运行时去进行匹配？
     */
    fun isRuntime(): Boolean

    /**
     * 在运行时匹配一个方法，会将运行时的参数列表一起进行匹配
     * @param method 要进行匹配的方法
     * @param targetClass 要进行匹配的目标类
     * @param args 方法的参数列表
     */
    fun matches(method: Method, targetClass: Class<*>, vararg args: Any?) : Boolean
}