package com.wanna.framework.aop.framework

import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.lang.reflect.Method

/**
 * 基于Cglib的动态代理
 */
open class CglibAopProxy(private val config: AdvisedSupport) : AopProxy {
    override fun getProxy(): Any {
        return getProxy(ClassLoader.getSystemClassLoader())
    }

    override fun getProxy(classLoader: ClassLoader): Any {
        val enhancer = Enhancer()
        enhancer.setCallback(object : MethodInterceptor {
            override fun intercept(obj: Any?, method: Method?, args: Array<out Any>?, proxy: MethodProxy?): Any {
                TODO("Not yet implemented")
            }
        })
        enhancer.setSuperclass(config.getTargetClass())
        enhancer.setInterfaces(config.getInterfaces().toTypedArray())
        return enhancer.create()
    }
}