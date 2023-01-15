package com.wanna.framework.aop.intercept

import java.lang.reflect.Constructor

/**
 * 这是一个构造器的Invocation, 支持获取构造器对象
 */
interface ConstructorInvocation : Invocation {
    fun getConstructor(): Constructor<*>
}