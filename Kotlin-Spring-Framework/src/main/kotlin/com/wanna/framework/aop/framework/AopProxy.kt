package com.wanna.framework.aop.framework

/**
 * 这是对Spring当中的Aop代理进行的一层抽象，具体实现包括Jdk/Cglib的AopProxy
 */
interface AopProxy {
    fun getProxy(): Any

    fun getProxy(classLoader: ClassLoader): Any
}