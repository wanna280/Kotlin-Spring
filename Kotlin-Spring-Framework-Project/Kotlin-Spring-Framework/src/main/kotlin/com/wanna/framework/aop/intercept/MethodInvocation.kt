package com.wanna.framework.aop.intercept

import java.lang.reflect.Method

/**
 * 这是一个MethodInvocation，支持获取构造的方法
 */
interface MethodInvocation : Invocation {
    fun getMethod(): Method
}