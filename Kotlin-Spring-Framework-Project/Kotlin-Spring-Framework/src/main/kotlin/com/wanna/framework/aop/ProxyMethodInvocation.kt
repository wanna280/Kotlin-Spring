package com.wanna.framework.aop

import com.wanna.framework.aop.intercept.MethodInvocation

/**
 * 这是一个代理方法的Invocation
 */
interface ProxyMethodInvocation : MethodInvocation {

    /**
     * 获取代理对象
     */
    fun getProxy(): Any

    /**
     * 设置方法参数
     */
    fun setArguments(vararg args: Any?)

    /**
     * 通过MethodInvocation可以去设置用户自定义的属性
     */
    fun setUserAttribute(key: String, value: Any?)

    /**
     * 获取用户自定义属性
     */
    fun getUserAttribute(key: String) : Any?
}