package com.wanna.framework.scheduling.support

import com.wanna.framework.core.util.ReflectionUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


/**
 * 定时调度的方法的Runnable
 *
 * @param target 目标对象
 * @param method 要执行的目标方法
 */
class ScheduledMethodRunnable(val target: Any, val method: Method) : Runnable {
    override fun run() {
        try {
            ReflectionUtils.makeAccessible(method)
            ReflectionUtils.invokeMethod(method, target)
        } catch (ex: InvocationTargetException) {
            throw ex.targetException
        }
    }
}