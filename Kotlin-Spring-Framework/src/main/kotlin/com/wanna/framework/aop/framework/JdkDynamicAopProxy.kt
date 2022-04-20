package com.wanna.framework.aop.framework

import com.wanna.framework.aop.ReflectiveMethodInvocation
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * 这是一个基于JDK的动态代理，AdvisedSupport是一个为Advice提供支持的组件，例如ProxyFactory
 */
open class JdkDynamicAopProxy(var adviced: AdvisedSupport) : AopProxy, InvocationHandler {

    // 要进行创建代理所需要用到的接口
    private var proxiesInterfaces: Array<out Class<*>> = adviced.getInterfaces().toTypedArray()

    /**
     * 创建JDK动态代理，在运行时会被回调到这个方法，在这个方法当中，将逻辑转交给ReflectiveMethodInvocation去进行继续执行
     * @param proxy 代理对象
     * @param method 执行的方法，不能为空
     * @param args 方法的参数列表
     */
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        val targetSource = adviced.getTargetSource()
        val target = targetSource.getTarget()
        if (method.declaringClass === Any::class.java) {
            if (args == null) {
                return ReflectionUtils.invokeMethod(method, target)
            }
            return ReflectionUtils.invokeMethod(method, target, args)
        }

        var returnVal: Any? = null

        // 获取MethodInterceptor链
        val chain = adviced.getInterceptorsAndDynamicInterceptionAdvice(method, method.declaringClass)

        // 创建一个ReflectiveMethodInvocation，去反射调用拦截器链并调用目标方法
        val invocation = ReflectiveMethodInvocation(proxy, target, method, args, null, chain)
        returnVal = invocation.proceed()

        return returnVal
    }

    override fun getProxy(): Any {
        return getProxy(ClassUtils.getDefaultClassLoader())
    }

    override fun getProxy(classLoader: ClassLoader): Any {
        return Proxy.newProxyInstance(classLoader, proxiesInterfaces, this)
    }
}