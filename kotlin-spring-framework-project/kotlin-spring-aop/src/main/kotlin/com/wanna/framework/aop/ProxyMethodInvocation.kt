package com.wanna.framework.aop

import com.wanna.framework.aop.intercept.MethodInvocation
import com.wanna.framework.lang.Nullable

/**
 * 代理方法的[MethodInvocation]
 */
interface ProxyMethodInvocation : MethodInvocation {

    /**
     * 获取代理对象
     */
    fun getProxy(): Any

    /**
     * 设置方法参数
     *
     * @param args 方法参数列表
     */
    fun setArguments(vararg args: Any?)

    /**
     * 通过MethodInvocation可以去设置用户自定义的属性
     *
     * @param key key
     * @param value value
     */
    fun setUserAttribute(key: String, @Nullable value: Any?)

    /**
     * 获取用户自定义属性
     *
     * @param key key
     * @return value
     */
    @Nullable
    fun getUserAttribute(key: String): Any?
}