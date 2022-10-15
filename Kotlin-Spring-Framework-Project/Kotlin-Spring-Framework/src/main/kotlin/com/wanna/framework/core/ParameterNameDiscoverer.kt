package com.wanna.framework.core

import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * 这是一个参数名的发现器，用于发现一个方法/构造器当中的所有的参数的参数名列表
 *
 * @see DefaultParameterNameDiscoverer
 * @see LocalVariableTableParameterNameDiscoverer
 * @see StandardReflectionParameterNameDiscoverer
 * @see LocalVariableTableParameterNameDiscoverer
 */
interface ParameterNameDiscoverer {
    /**
     * 获取指定的构造器的参数名列表
     *
     * @param constructor 要去寻找的目标构造器
     * @return 如果匹配，return 参数名数组(Array<String>)；不匹配return null
     */
    fun getParameterNames(constructor: Constructor<*>): Array<String>?

    /**
     * 获取指定的方法的参数名列表
     *
     * @param method 要去寻找的目标方法
     * @return 如果匹配，return 参数名数组(Array<String>)；不匹配return null
     */
    fun getParameterNames(method: Method): Array<String>?
}