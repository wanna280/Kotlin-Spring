package com.wanna.framework.aop

import com.wanna.framework.aop.framework.InterceptorAndDynamicMethodMatcher
import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.core.util.ReflectionUtils
import java.lang.reflect.Method

/**
 * 这是一个反射执行目标方法的MethodInvocation，可以完成MethodInterceptor的链式调用，完成AOP的代理逻辑
 */
open class ReflectiveMethodInvocation(
    private val proxy: Any,
    private val target: Any?,
    private val method: Method,
    private var args: Array<out Any?>?,
    private val targetClass: Class<*>?,
    private val interceptorsAndDynamicMethodMatchers: List<Any>
) : ProxyMethodInvocation {

    // 用户自定义属性，为了减少不必要的内存占用，不进行初始化
    private var userAttributes: MutableMap<String, Any>? = null

    // 当前的拦截器索引index，初始化为-1，每执行一次，index++
    private var currentInterceptorIndex = -1

    override fun getProxy(): Any {
        return this.proxy
    }

    override fun setArguments(vararg args: Any?) {
        this.args = args
    }

    override fun getArguments(): Array<out Any?>? {
        return this.args
    }

    override fun proceed(): Any? {
        // 如果执行到拦截器的最后了，那么需要执行目标方法
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size - 1) {
            return invokeJoinpoint()
        }
        val interceptorOrInterceptionAdvice = interceptorsAndDynamicMethodMatchers[++currentInterceptorIndex]

        // 如果需要进行动态的方法匹配，那么在这里完成匹配之后，再进行执行
        return if (interceptorOrInterceptionAdvice is InterceptorAndDynamicMethodMatcher) {
            val targetClass = this.targetClass ?: method.declaringClass
            if (interceptorOrInterceptionAdvice.methodMatcher.matches(this.method, targetClass, args)) {
                interceptorOrInterceptionAdvice.interceptor.invoke(this)
            } else {
                proceed()
            }

            // 如果不需要进行动态方法匹配，那么向下传递，让下一个MethodInterceptor去处理
        } else {
            (interceptorOrInterceptionAdvice as MethodInterceptor).invoke(this)
        }
    }

    override fun getThis(): Any? {
        return target
    }

    override fun getMethod(): Method {
        return method
    }

    override fun setUserAttribute(key: String, value: Any?) {
        if (value == null) {
            if (this.userAttributes != null) {
                this.userAttributes!!.remove(key)
            }
        } else {
            if (this.userAttributes == null) {
                this.userAttributes = HashMap()
            }
            this.userAttributes!![key] = value
        }
    }

    override fun getUserAttribute(key: String): Any? = this.userAttributes?.get(key)

    /**
     * 执行Joinpoint，也就是执行目标方法
     */
    protected fun invokeJoinpoint(): Any? {
        ReflectionUtils.makeAccessiable(this.method)
        // 如果args=null，那么
        if (args == null) {
            return ReflectionUtils.invokeMethod(method, target, *emptyArray())
        }
        return ReflectionUtils.invokeMethod(method, target, *args!!)
    }
}